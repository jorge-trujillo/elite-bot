package org.jorgetrujillo.elitebot.clients

import org.jorgetrujillo.elitebot.domain.SystemsSearchRequest
import org.jorgetrujillo.elitebot.domain.elite.System

interface SystemsClient {

  System findSystemByName(String name)

  List<System> findSystems(SystemsSearchRequest searchRequest)
}
