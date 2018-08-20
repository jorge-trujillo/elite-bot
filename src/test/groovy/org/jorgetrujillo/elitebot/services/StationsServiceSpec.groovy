package org.jorgetrujillo.elitebot.services

import org.jorgetrujillo.elitebot.clients.StationsClient
import org.jorgetrujillo.elitebot.clients.SystemsClient
import org.jorgetrujillo.elitebot.domain.StationCriteria
import org.jorgetrujillo.elitebot.domain.SystemCriteria
import org.jorgetrujillo.elitebot.domain.elite.PadSize
import org.jorgetrujillo.elitebot.domain.elite.SecurityLevel
import org.jorgetrujillo.elitebot.domain.elite.Station
import org.jorgetrujillo.elitebot.domain.elite.System
import spock.lang.Specification

class StationsServiceSpec extends Specification {

  StationsService stationsService

  void setup() {
    stationsService = new StationsService(
        systemsClient: Mock(SystemsClient),
        stationsClient: Mock(StationsClient)
    )
  }

  void 'GetStationByName'() {

    given:
    String query = 'kanwar'
    Station expected = new Station()

    when:
    Station actual = stationsService.getStationByName(query)

    then:
    1 * stationsService.stationsClient.findStationsByName(query) >> [expected]
    0 * _

    actual.is(expected)
  }

  void 'FindStations'() {
    given:
    StationCriteria stationCriteria = new StationCriteria(
        minPadSize: PadSize.L
    )
    Station expected = new Station()

    when:
    List<Station> actual = stationsService.findStations(stationCriteria)

    then:
    1 * stationsService.stationsClient.findStations(stationCriteria) >> [expected]
    0 * _

    actual.size() == 1
    actual[0].is(expected)

  }

  void 'GetNearestInterstellarFactors'() {

    given:
    String referenceId = '100'
    PadSize minSize = PadSize.L

    List<System> systems = [
        new System(id: '1'),
        new System(id: '2'),
        new System(id: '3'),
        new System(id: '4')
    ]
    List<Station> stations = [
        new Station(id: '100a', systemId: '100'),
        new Station(id: '100b', systemId: '100'),
        new Station(id: '1a', systemId: '1'),
        new Station(id: '2a', systemId: '2'),
        new Station(id: '200a', systemId: '200'),
        new Station(id: '400a', systemId: '400')
    ]

    when:
    List<Station> actual = stationsService.getNearestInterstellarFactors(referenceId, minSize)

    then:
    1 * stationsService.systemsClient.findSystems({ SystemCriteria systemCriteria ->
      assert  systemCriteria.referenceSystemId == referenceId
      assert systemCriteria.securityLevel == SecurityLevel.LOW
      assert systemCriteria.minPadSize == minSize
      return true
    } as SystemCriteria) >> systems
    1 * stationsService.stationsClient.findStations({ StationCriteria stationCriteria ->
      assert stationCriteria.minPadSize == minSize
      assert stationCriteria.referenceSystemId == referenceId
      return true
    } as StationCriteria) >> stations

    0 * _

    actual*.id == ['1a', '2a']

  }
}
