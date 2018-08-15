package org.jorgetrujillo.elitebot.services

import groovy.util.logging.Slf4j
import org.jorgetrujillo.elitebot.clients.SystemsClient
import org.jorgetrujillo.elitebot.domain.SystemsSearchRequest
import org.jorgetrujillo.elitebot.domain.elite.SecurityLevel
import org.jorgetrujillo.elitebot.domain.elite.System
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Slf4j
class SystemsService {

  @Autowired
  SystemsClient systemsClient

  System getSystemByName(String name) {
    List<System> systems = systemsClient.findSystemsByName(name)
    return systems ? systems.first() : null
  }

  List<System> getNearestInterstellarFactors(String referenceSystemId) {

    // Now find the right system and station
    SystemsSearchRequest systemsSearchRequest = new SystemsSearchRequest(
        referenceSystemId: referenceSystemId,
        securityLevel: SecurityLevel.LOW,
        sortType: SystemsSearchRequest.SortType.DISTANCE_TO_REF
    )
    List<System> systems = systemsClient.findSystems(systemsSearchRequest)
    return systems

  }
}
