package org.jorgetrujillo.elitebot.clients

import org.jorgetrujillo.elitebot.IntegrationTestBase
import org.jorgetrujillo.elitebot.clients.eddb.EddbSystemsClient
import org.jorgetrujillo.elitebot.domain.SystemCriteria
import org.jorgetrujillo.elitebot.domain.elite.SecurityLevel
import org.jorgetrujillo.elitebot.domain.elite.Station
import org.jorgetrujillo.elitebot.domain.elite.System
import org.springframework.beans.factory.annotation.Autowired

class EddbSystemsClientIntegrationSpec extends IntegrationTestBase {

  @Autowired
  EddbSystemsClient eddbSystemsClient

  void 'Find a system by name'() {

    given:
    String systemName = 'maya'

    when:
    List<System> systems = eddbSystemsClient.findSystemsByName(systemName)

    then:
    systems

    and: 'First system is Maya'
    systems[0].name == 'Maya'
    systems[0].id == '13432'

    systems[0].stations.size() == 11
    Station station = systems[0].stations.find { it.id == '45711' }
    station.name == 'Barnes Enterprise'
    station.landingPad == 'L'
    station.distanceFromStarLs == 109

    and: 'Second system is Mayaco'
    systems[1].name == 'Mayaco'
    systems[1].id == '13433'
  }

  void 'Find low security systems near another'() {

    given:
    String hurukuntakId = '9608'
    SystemCriteria systemCriteria = new SystemCriteria(
        referenceSystemId: hurukuntakId,
        sortType: SystemCriteria.SortType.DISTANCE_TO_REF,
        securityLevel: SecurityLevel.LOW
    )

    when:
    List<System> systems = eddbSystemsClient.findSystems(systemCriteria)

    then:
    systems
    systems[0].name == 'Anareldt'
    systems[0].allegiance == 'Empire'
    systems[0].population > 0
    systems[0].distanceFromRef == '4.44 ly'
  }
}
