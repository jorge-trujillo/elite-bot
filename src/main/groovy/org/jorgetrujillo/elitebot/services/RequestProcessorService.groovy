package org.jorgetrujillo.elitebot.services

import groovy.util.logging.Slf4j
import org.jorgetrujillo.elitebot.domain.ServiceRequest
import org.jorgetrujillo.elitebot.domain.elite.Station
import org.jorgetrujillo.elitebot.domain.elite.System
import org.jorgetrujillo.elitebot.exceptions.LanguageRequestParseException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Slf4j
class RequestProcessorService {

  @Autowired
  SystemsService systemsService

  @Autowired
  SimpleRequestService simpleRequestService

  String processMessage(String message) {

    try {
      // Parse the request
      ServiceRequest serviceRequest = simpleRequestService.parseRequest(message)

      // Execute it
      String response = null
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
          response = "I couldn't understand what you are trying to do!"
      }

      return response
    }
    catch (LanguageRequestParseException e) {
      log.error("Exception parsing ${message}", e)
    }
    catch (Exception e) {
      log.error("Unexepected error: ${e.message}", e)
    }

    return 'I cannot understand this request! Ask me for help to see what I can do.'
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
            response.append(" * **${it.name}** - ${it.distanceFromRef} from you and ${it.allegiance}-controlled\n")
          }

          response.append("You should try ${systems[0].name}! It is ${systems[0].distanceFromRef} from your location")
        }
        break
      case ServiceRequest.ResourceType.INTERSTELLAR_FACTORS:
        List<System> systems = systemsService.getNearestInterstellarFactors(referenceSystem.id,
            serviceRequest.systemCriteria.minPadSize)

        if (systems) {
          response.append("Here are 3 good options:\n")
          systems.subList(0, Math.min(3, systems.size())).each {
            response.append(" * **${it.name}** - ${it.distanceFromRef} from you and ${it.allegiance}-controlled\n")
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
    referenceSystem.stations?.each { Station station ->
      response.append(" * **${station.name}** - ${station.distanceFromStarLs} ls from the star and has a ${station.landingPad} pad\n")
    }
    response.append("You can also check out ${referenceSystem.url} to see more")

    return response.toString()
  }

  private String computeDistance(ServiceRequest serviceRequest) {

    List<System> systems = [
        systemsService.getSystemByName(serviceRequest.systemPair.first),
        systemsService.getSystemByName(serviceRequest.systemPair.second)
    ]

    systems.eachWithIndex { System system, int index ->
      if (!system) {
        return "I could not locate the system ${serviceRequest.systemPair[index]}!"
      }
    }

    double distance = Math.sqrt(Math.pow(systems[0].x - systems[1].x, 2) + Math.pow(systems[0].y - systems[1].y, 2) +
        Math.pow(systems[0].z - systems[1].z, 2))

    return "The distance between **${systems[0].name}** and **${systems[1].name}** is *${distance} ly*"
  }

}
