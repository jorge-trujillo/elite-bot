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
      String response
      switch (serviceRequest.actionType) {
        case ServiceRequest.ActionType.FIND:
          return executeFindRequest(serviceRequest)
          break
        case ServiceRequest.ActionType.SYSTEM_DETAILS:
          return getSystemInfo(serviceRequest)
          break
        case ServiceRequest.ActionType.COMPUTE_DISTANCE:
          return computeDistance(serviceRequest)
          break
        default:
          response = generateHelpMessage()
      }

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

    StringBuilder response = new StringBuilder()
    switch (serviceRequest.resourceType) {

      case ServiceRequest.ResourceType.SYSTEM:
        List<System> systems = systemsService.findSystems(serviceRequest.systemCriteria)
        if (systems) {
          response.append("Here are 5 matching systems:\n")
          systems.subList(0, Math.min(5, systems.size())).each {

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
        }
        break
      case ServiceRequest.ResourceType.INTERSTELLAR_FACTORS:
        List<Station> stations = stationsService.getNearestInterstellarFactors(referenceSystem.id,
            serviceRequest.systemCriteria.minPadSize)

        if (stations) {
          // Get the stations that are within 2000 ls first
          List<Station> filteredList = stations.findAll {
            it.distanceFromStarLs <= 2000
          }
          filteredList.addAll(stations.findAll {
            !(it in filteredList)
          })

          response.append("Here are 3 good options:\n")
          filteredList.subList(0, Math.min(3, filteredList.size())).each {
            response.append(" * **${it.name}** in the **${it.systemName}** system - ${it.distanceFromRefLy} ly from you " +
                "and ${it.distanceFromStarLs} ls from the star\n")
          }
        }
        break
      default:
        response.append("I can't really help yet with finding a ${serviceRequest.resourceType.name()}!")
    }

    return response.toString()
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

      response.append(" * **${station.name}** - ${station.distanceFromStarLs} ls from the star and has a ${station.landingPad} pad\n")
    }
    response.append("You can also check out ${referenceSystem.url} to see more")

    return response.toString()
  }

  private String computeDistance(ServiceRequest serviceRequest) {

    List<System> systems = [
        systemsService.getSystemByName(serviceRequest.systemPair.first, true),
        systemsService.getSystemByName(serviceRequest.systemPair.second, true)
    ]

    systems.eachWithIndex { System system, int index ->
      if (!system) {
        return "I could not locate the system ${serviceRequest.systemPair[index]}!"
      }
    }

    double distance = Math.sqrt(Math.pow(systems[0].x - systems[1].x, 2) + Math.pow(systems[0].y - systems[1].y, 2) +
        Math.pow(systems[0].z - systems[1].z, 2))

    return "The distance between **${systems[0].name}** and **${systems[1].name}** is " +
        "*${String.format("%.2f", distance)} ly*"
  }

  private String generateHelpMessage(SimpleRequestField field = null) {

    StringBuilder stringBuilder = new StringBuilder()

    if (!field) {
      stringBuilder.append('You need help and you got it! Here is what I can do... ' +
          'quick note is that **bold** fields are required: \n')
      stringBuilder.append(' - *Find systems*: **find**: system **near**: hurukuntak ' +
          '*pad*: L *allegiance*: empire *security*: high *power*: aisling C\n')
      stringBuilder.append(' - *System details*: **system**: sol\n')
      stringBuilder.append(' - *Find interstellar factors*: **find**: interstellar factors **near**: sol *pad*: L\n')
      stringBuilder.append(' - *Find distance*: **distance**: sol **to**: maya\n')
      stringBuilder.append('More coming soon!\n')
    } else {
      stringBuilder.append(field.helpText)
    }

    return stringBuilder.toString()
  }

}
