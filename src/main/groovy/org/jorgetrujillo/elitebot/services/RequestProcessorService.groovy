package org.jorgetrujillo.elitebot.services

import groovy.util.logging.Slf4j
import org.jorgetrujillo.elitebot.domain.ServiceRequest
import org.jorgetrujillo.elitebot.domain.elite.PowerEffect
import org.jorgetrujillo.elitebot.domain.elite.Station
import org.jorgetrujillo.elitebot.domain.elite.System
import org.jorgetrujillo.elitebot.exceptions.SimpleRequestField
import org.jorgetrujillo.elitebot.exceptions.SimpleRequestParseException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Slf4j
class RequestProcessorService {

  @Autowired
  SystemsService systemsService

  @Autowired
  StationsService stationsService

  @Autowired
  SimpleRequestService simpleRequestService

  @Autowired
  ServiceRequestCacheService cacheService

  String processMessage(String message) {

    try {
      // Parse the request
      ServiceRequest serviceRequest
      try {
        serviceRequest = simpleRequestService.parseRequest(message)
      } catch (SimpleRequestParseException e) {
        log.error("Error parsing ${e.field} from: ${message}")
        return generateHelpMessage(e.field)
      }

      // At this point, the request should be solid. Execute
      String response = cacheService.getEntry(serviceRequest)
      if (response) {
        return response
      }

      switch (serviceRequest.actionType) {
        case ServiceRequest.ActionType.FIND:
          response = executeFindRequest(serviceRequest)
          break
        case ServiceRequest.ActionType.SYSTEM_DETAILS:
          response = getSystemInfo(serviceRequest)
          break
        case ServiceRequest.ActionType.COMPUTE_DISTANCE:
          response = computeDistance(serviceRequest)
          break
        default:
          response = generateHelpMessage()
      }

      cacheService.saveEntry(serviceRequest, response)
      return response
    }
    catch (Exception e) {
      log.error("Unexepected error: ${e.message}", e)
    }

    return 'Interesting... you have an awesome capacity to break things. I need to show this to my master...'
  }

  private String executeFindRequest(ServiceRequest serviceRequest) {

    // Get the reference system
    System referenceSystem = systemsService.getSystemByName(serviceRequest.systemCriteria.referenceSystemName)
    if (!referenceSystem) {
      return "I could not locate the system ${serviceRequest.systemCriteria.referenceSystemName}, sorry"
    }
    serviceRequest.systemCriteria.referenceSystemId = referenceSystem.id

    String response
    switch (serviceRequest.resourceType) {

      case ServiceRequest.ResourceType.SYSTEM:
        List<System> systems = systemsService.findSystems(serviceRequest.systemCriteria)
        if (systems) {
          response = createFindSystemsResponse(systems)
        }
        break

      case ServiceRequest.ResourceType.INTERSTELLAR_FACTORS:
        List<Station> stations = stationsService.getNearestInterstellarFactors(referenceSystem.id,
            serviceRequest.systemCriteria.minPadSize)

        if (stations) {
          response = createFindStationsResponse(stations)
        }
        break

      case ServiceRequest.ResourceType.MATERIAL_TRADER:
        List<Station> stations = stationsService.getNearestMaterialTraders(referenceSystem.name)

        if (stations) {
          response = createFindStationsResponse(stations)
        }
        break

      default:
        response = "I can't really help yet with finding a ${serviceRequest.resourceType.name()}!"
    }

    return response
  }

  private String createFindSystemsResponse(List<System> systems) {
    StringBuilder response = new StringBuilder()

    int numResults = Math.min(5, systems.size())
    response.append("Here are ${numResults} matching systems:\n")
    systems.subList(0, numResults).each {

      String powerDesc = ''
      if (it.powerType && it.powerEffect) {
        switch (it.powerEffect) {
          case PowerEffect.CONTROL:
            powerDesc = ", and under control of ${it.powerType.powerName}"
            break
          case PowerEffect.EXPLOITED:
            powerDesc = ", and exploited by ${it.powerType.powerName}"
            break
          case PowerEffect.EXPANSION:
            powerDesc = ", and expanded to by ${it.powerType.powerName}"
            break
        }
      }

      response.append(" * **${it.name}** - ${it.distanceFromRefLy} ly from you, " +
          "allegiance is ${it.allegiance}${powerDesc}\n")
    }

    return response.toString()
  }

  private String createFindStationsResponse(List<Station> stations) {
    StringBuilder response = new StringBuilder()

    // Get the stations that are within 2000 ls first
    List<Station> filteredList = stations.findAll { Station station ->
      station.distanceFromStarLs <= 2000
    }
    filteredList.addAll(stations.findAll {
      !(it in filteredList)
    })

    int numResults = Math.min(5, filteredList.size())
    response.append("Here are ${numResults} matching stations:\n")
    filteredList.subList(0, numResults).each {
      response.append(" * ${renderStation(it, true)}\n")
    }

    return response.toString()
  }

  private String renderStation(Station station,
                               boolean relativeDistance) {
    String stationType = station.planetary ? 'planetary' : 'orbital'
    String padSize = "${station.landingPad} pads, "
    String matTrader = station.materialTrader ?
        "**${station.materialTrader.toString().toLowerCase()}** mat trader, " : ''
    String distance = station.distanceFromRefLy ?
        " and ${station.distanceFromRefLy} ly from ${relativeDistance ? 'you' : 'sol'}" : ''

    return "**${station.name}** in **${station.systemName}** - ${stationType} station, " + padSize + matTrader +
        "${station.distanceFromStarLs} ls from the star" + distance
  }

  private String getSystemInfo(ServiceRequest serviceRequest) {

    System referenceSystem = systemsService.getSystemByName(serviceRequest.systemCriteria.referenceSystemName)
    if (!referenceSystem) {
      return "I could not locate the system ${serviceRequest.systemCriteria.referenceSystemName}, sorry"
    }

    StringBuilder response = new StringBuilder()
    response.append("I found the **${referenceSystem.name}** system. It has the following stations: \n")
    referenceSystem.stations
        ?.sort { a, b -> a.distanceFromStarLs <=> b.distanceFromStarLs }
        ?.each { Station station ->

      response.append(" * ${renderStation(station, false)}\n")
    }
    response.append("You can also check out ${referenceSystem.url} to see more.")

    return response.toString()
  }

  private String computeDistance(ServiceRequest serviceRequest) {

    List<System> systems = serviceRequest.systemPair.collect { String value ->
      return systemsService.getSystemByName(value, true)
    }

    int badIndex = systems.findIndexOf { System system ->
      return !system
    }
    if (badIndex >= 0) {
      return "I could not locate the requested system **${serviceRequest.systemPair[badIndex]}**! " +
          'Are you sure you typed it correctly?'
    }

    double distance = Math.sqrt(Math.pow(systems[0].x - systems[1].x, 2) + Math.pow(systems[0].y - systems[1].y, 2) +
        Math.pow(systems[0].z - systems[1].z, 2))

    return "The distance between **${systems[0].name}** and **${systems[1].name}** is " +
        "*${String.format('%.2f', distance)} ly*"
  }

  private String generateHelpMessage(SimpleRequestField field = null) {

    StringBuilder stringBuilder = new StringBuilder()

    if (field) {
      stringBuilder.append(field.helpText)
    } else {
      stringBuilder.append('You need help and you got it! Here is what I can do... ' +
          'quick note is that **bold** fields are required: \n')
      stringBuilder.append(' * **Find systems** -> **find**: system **near**: hurukuntak ' +
          '*pad*: L *allegiance*: empire *security*: high *power*: aisling C\n')
      stringBuilder.append(' * **System details** -> **system**: sol\n')
      stringBuilder.append(' * **Find interstellar factors** -> ' +
          '**find**: interstellar factors **near**: sol *pad*: L\n')
      stringBuilder.append(' * **Find material traders** -> ' +
          '**find**: mat traders **near**: sol\n')
      stringBuilder.append(' * **Find distance** -> **distance**: sol **to**: maya\n')
      stringBuilder.append('More coming soon!\n')
    }

    return stringBuilder.toString()
  }
}
