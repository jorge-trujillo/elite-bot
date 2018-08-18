package org.jorgetrujillo.elitebot.clients

import org.jorgetrujillo.elitebot.domain.SystemCriteria
import org.jorgetrujillo.elitebot.domain.elite.System

interface SystemsClient {

  List<System> findSystemsByName(String name)

  List<System> findSystems(SystemCriteria systemCriteria)
}
