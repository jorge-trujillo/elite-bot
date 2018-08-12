package org.jorgetrujillo.elitebot.services

import org.jorgetrujillo.elitebot.clients.SystemsClient
import org.jorgetrujillo.elitebot.domain.SystemsSearchRequest
import org.springframework.beans.factory.annotation.Autowired

class SystemsService {

  @Autowired
  SystemsClient systemsClient

  List<System> getNearestInterstellarFactors(String referenceSystem, boolean largePad) {

    // Get the system ID first

    SystemsSearchRequest systemsSearchRequest = new SystemsSearchRequest(
    )

  }
}
