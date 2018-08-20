package org.jorgetrujillo.elitebot.services

import groovy.util.logging.Slf4j
import org.jorgetrujillo.elitebot.clients.StationsClient
import org.jorgetrujillo.elitebot.clients.SystemsClient
import org.jorgetrujillo.elitebot.domain.StationCriteria
import org.jorgetrujillo.elitebot.domain.SystemCriteria
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

  Station getStationByName(String name) {
    List<Station> stations = stationsClient.findStationsByName(name)
    return stations ? stations.first() : null
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
}
