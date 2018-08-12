package org.jorgetrujillo.elitebot.clients

import org.jorgetrujillo.elitebot.IntegrationTestBase
import org.jorgetrujillo.elitebot.clients.eddb.EddbSystemsClient
import org.jorgetrujillo.elitebot.domain.SystemsSearchRequest
import org.jorgetrujillo.elitebot.domain.elite.SecurityLevel
import org.jorgetrujillo.elitebot.domain.elite.System
import org.springframework.beans.factory.annotation.Autowired

class EddbSystemsClientIntegrationSpec extends IntegrationTestBase {

  @Autowired
  EddbSystemsClient eddbSystemsClient

  void setup() {
  }

  void 'Find low security systems near another'() {

    given:
    String hurukuntakId = '9608'
    SystemsSearchRequest systemsSearchRequest = new SystemsSearchRequest(
        referenceSystemId: hurukuntakId,
        sortType: SystemsSearchRequest.SortType.DISTANCE_TO_REF,
        securityLevel: SecurityLevel.LOW
    )

    when:
    List<System> systems = eddbSystemsClient.findSystems(systemsSearchRequest)

    then:
    systems
    systems[0].name == 'Anareldt'
    systems[0].allegiance == 'Empire'
    systems[0].population > 0
    systems[0].distanceFromRef == '4.44 ly'
  }
}
