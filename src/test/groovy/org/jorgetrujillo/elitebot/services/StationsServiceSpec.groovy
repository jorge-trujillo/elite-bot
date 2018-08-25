package org.jorgetrujillo.elitebot.services

import org.jorgetrujillo.elitebot.clients.StationsClient
import org.jorgetrujillo.elitebot.clients.SystemsClient
import org.jorgetrujillo.elitebot.clients.inara.InaraMaterialTradersClient
import org.jorgetrujillo.elitebot.clients.inara.domain.MaterialTraderResult
import org.jorgetrujillo.elitebot.domain.StationCriteria
import org.jorgetrujillo.elitebot.domain.SystemCriteria
import org.jorgetrujillo.elitebot.domain.elite.MaterialTraderType
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
        stationsClient: Mock(StationsClient),
        materialTradersClient: Mock(InaraMaterialTradersClient)
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
      assert systemCriteria.referenceSystemId == referenceId
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

  void 'Get material traders'() {
    given:
    String systemName = 'sierra'

    List<MaterialTraderResult> results = [
        new MaterialTraderResult(
            stationName: 'Alpha',
            systemName: 's1',
            materialTraderType: MaterialTraderType.ENCODED,
            distanceFromStarLs: 3000,
            distanceFromRefSystemLy: 5
        ),
        new MaterialTraderResult(
            stationName: 'Bravo',
            systemName: 's2',
            materialTraderType: MaterialTraderType.RAW,
            distanceFromStarLs: 6000,
            distanceFromRefSystemLy: 10
        ),
        new MaterialTraderResult(
            stationName: 'Charlie',
            systemName: 's3',
            materialTraderType: MaterialTraderType.MANUFACTURED,
            distanceFromStarLs: 50,
            distanceFromRefSystemLy: 22
        ),
        new MaterialTraderResult(
            stationName: 'Delta',
            systemName: 's4',
            materialTraderType: MaterialTraderType.ENCODED,
            distanceFromStarLs: 200,
            distanceFromRefSystemLy: 40
        )
    ]

    when:
    List<Station> actual = stationsService.getNearestMaterialTraders(systemName)

    then:
    1 * stationsService.materialTradersClient.getMaterialTradersNear(systemName) >> results
    1 * stationsService.stationsClient.findStationsByName('Delta') >> [
        new Station(id: 'd1', name: 'delta', systemName: 'other'),
        new Station(id: 'd', name: 'delta', systemName: 's4')
    ]
    1 * stationsService.stationsClient.findStationsByName('Bravo') >> [new Station(id: 'b')]
    1 * stationsService.stationsClient.findStationsByName('Charlie') >> [new Station(id: 'c')]
    0 * _

    and: 'Right systems returned'
    actual*.id == ['b', 'c', 'd']

    actual[0].id == 'b'
    actual[0].distanceFromRefLy == 10
    actual[0].distanceFromStarLs == 6000
    !actual.any { !it.materialTrader }

  }
}
