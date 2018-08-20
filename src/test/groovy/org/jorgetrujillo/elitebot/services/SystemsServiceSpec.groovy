package org.jorgetrujillo.elitebot.services

import org.jorgetrujillo.elitebot.clients.SystemsClient
import org.jorgetrujillo.elitebot.domain.elite.System
import spock.lang.Specification
import spock.lang.Unroll

class SystemsServiceSpec extends Specification {

  SystemsService systemsService

  void setup() {
    systemsService = new SystemsService(
        systemsClient: Mock(SystemsClient)
    )
  }

  @Unroll
  void 'GetSystemByName when includeLocation is #includeLocation'() {

    given:
    String name = 'maya'
    System expected = new System(id: '1')
    System location = new System(id: '1', x: 1, y: 1, z: 1)

    when:
    System actual = systemsService.getSystemByName(name, includeLocation)

    then:
    1 * systemsService.systemsClient.findSystemsByName(name) >> [expected]
    calls * systemsService.systemsClient.getSystemById('1') >> location
    0 * _

    actual.is(expected)
    actual.x == (includeLocation ? 1 : null)
    actual.y == (includeLocation ? 1 : null)
    actual.z == (includeLocation ? 1 : null)

    where:
    includeLocation | calls
    false           | 0
    true            | 1
  }

}
