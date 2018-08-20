package org.jorgetrujillo.elitebot.services

import groovy.util.logging.Slf4j
import org.jorgetrujillo.elitebot.clients.SystemsClient
import org.jorgetrujillo.elitebot.domain.SystemCriteria
import org.jorgetrujillo.elitebot.domain.elite.System
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Slf4j
class SystemsService {

  @Autowired
  SystemsClient systemsClient

  System getSystemByName(String name, boolean includeLocation = false) {
    List<System> systems = systemsClient.findSystemsByName(name)

    System result = systems ? systems.first() : null
    if (result && includeLocation) {
      System locationData = systemsClient.getSystemById(result.id)
      if (locationData) {
        result.x = locationData.x
        result.y = locationData.y
        result.z = locationData.z

        result.distanceFromRefLy = result.distanceFromRefLy ?: locationData.distanceFromRefLy
      }
    }

    return result
  }

  List<System> findSystems(SystemCriteria systemCriteria) {
    return systemsClient.findSystems(systemCriteria)
  }
}
