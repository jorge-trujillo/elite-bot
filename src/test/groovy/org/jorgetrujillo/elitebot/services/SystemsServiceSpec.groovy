package org.jorgetrujillo.elitebot.services

import org.jorgetrujillo.elitebot.clients.SystemsClient
import org.jorgetrujillo.elitebot.domain.SystemsSearchRequest
import org.jorgetrujillo.elitebot.domain.elite.SecurityLevel
import org.jorgetrujillo.elitebot.domain.elite.System
import spock.lang.Specification

class SystemsServiceSpec extends Specification {

  SystemsService systemsService

  void setup() {
    systemsService = new SystemsService(
        systemsClient: Mock(SystemsClient)
    )
  }

  void 'GetSystemByName'() {

    given:
    String name = 'maya'
    System expected = new System()

    when:
    System actual = systemsService.getSystemByName(name)

    then:
    1 * systemsService.systemsClient.findSystemsByName(name) >> [expected]
    0 * _
    actual.is(expected)
  }

  void 'GetNearestInterstellarFactors'() {

    given:
    String referenceId = 'id'
    List<System> expected = [new System()]

    when:
    List<System> actual = systemsService.getNearestInterstellarFactors(referenceId)

    then:
    1 * systemsService.systemsClient.findSystems({ SystemsSearchRequest searchRequest ->
      assert searchRequest.referenceSystemId == referenceId
      assert searchRequest.securityLevel == SecurityLevel.LOW
      assert searchRequest.sortType == SystemsSearchRequest.SortType.DISTANCE_TO_REF
      return true
    } as SystemsSearchRequest) >> expected
    0 * _

    actual == expected
  }
}