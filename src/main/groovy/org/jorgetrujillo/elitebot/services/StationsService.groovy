package org.jorgetrujillo.elitebot.services

import groovy.util.logging.Slf4j
import org.jorgetrujillo.elitebot.clients.StationsClient
import org.jorgetrujillo.elitebot.clients.SystemsClient
import org.jorgetrujillo.elitebot.clients.inara.InaraMaterialTradersClient
import org.jorgetrujillo.elitebot.clients.inara.domain.MaterialTraderResult
import org.jorgetrujillo.elitebot.domain.StationCriteria
import org.jorgetrujillo.elitebot.domain.SystemCriteria
import org.jorgetrujillo.elitebot.domain.elite.MaterialTraderType
import org.jorgetrujillo.elitebot.domain.elite.PadSize
import org.jorgetrujillo.elitebot.domain.elite.SecurityLevel
import org.jorgetrujillo.elitebot.domain.elite.Station
import org.jorgetrujillo.elitebot.domain.elite.System
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Slf4j
class StationsService {

  @Autowired
  SystemsClient systemsClient

  @Autowired
  StationsClient stationsClient

  @Autowired
  InaraMaterialTradersClient materialTradersClient

  Station getStationByName(String name, String systemName = null) {
    List<Station> stations = stationsClient.findStationsByName(name)
    if (!stations) {
      return null
    }

    Station selectedStation = stations.find {
      if (systemName && it.systemName) {
        return it.name.equalsIgnoreCase(name) && it.systemName.equalsIgnoreCase(systemName)
      }

      // Return the first one
      return true
    }

    return selectedStation
  }

  List<Station> findStations(StationCriteria stationCriteria) {
    return stationsClient.findStations(stationCriteria)
  }

  List<Station> getNearestInterstellarFactors(String referenceSystemId, PadSize minPadSize = null) {

    // Get all the nearest low sec systems
    SystemCriteria systemCriteria = new SystemCriteria(
        referenceSystemId: referenceSystemId,
        securityLevel: SecurityLevel.LOW,
        sortType: SystemCriteria.SortType.DISTANCE_TO_REF,
        minPadSize: minPadSize ?: null
    )
    List<System> systems = systemsClient.findSystems(systemCriteria)
    Map<String, System> systemMap = systems.collectEntries { System systemVal ->
      return [(systemVal.id): systemVal]
    }

    // Get all the nearest stations with the right pad size
    StationCriteria stationCriteria = new StationCriteria(
        referenceSystemId: referenceSystemId,
        minPadSize: minPadSize,
        sortType: StationCriteria.SortType.DISTANCE_TO_REF
    )
    List<Station> stations = stationsClient.findStations(stationCriteria)

    // Cross reference nearest stations with low sec systems
    List<Station> compliantStations = stations.findAll {
      return systemMap.containsKey(it.systemId)
    }

    return compliantStations
  }

  List<Station> getNearestMaterialTraders(String referenceSystemName) {

    List<Station> matTraderStations

    // Get mat traders
    List<MaterialTraderResult> matTraders = materialTradersClient.getMaterialTradersNear(referenceSystemName)

    // Get the closest stations that are within 2kls
    List<MaterialTraderResult> bestResults = MaterialTraderType.values().collect {
      MaterialTraderType materialTraderType ->
        MaterialTraderResult materialTraderResult = matTraders.find {
          it.distanceFromStarLs <= 2000 && it.materialTraderType == materialTraderType
        }
        return materialTraderResult ?: matTraders.find { it.materialTraderType == materialTraderType }

    }.findAll()

    // Fill in remaining details and return them
    matTraderStations = bestResults.collect {
      Station station = getStationByName(it.stationName, it.systemName)

      station?.materialTrader = it.materialTraderType
      station?.distanceFromStarLs = it.distanceFromStarLs
      station?.distanceFromRefLy = it.distanceFromRefSystemLy

      return station
    }.findAll()

    return matTraderStations
  }
}
