package org.jorgetrujillo.elitebot.clients.eddb

import org.jorgetrujillo.elitebot.clients.GenericClient
import org.jorgetrujillo.elitebot.clients.GenericRequest
import org.jorgetrujillo.elitebot.clients.GenericResponse
import org.jorgetrujillo.elitebot.clients.eddb.domain.EddbStationResult
import org.jorgetrujillo.elitebot.domain.elite.PadSize
import org.jorgetrujillo.elitebot.domain.elite.Station
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import spock.lang.Specification

class EddbStationsClientSpec extends Specification {

  EddbStationsClient eddbStationsClient

  void setup() {
    eddbStationsClient = new EddbStationsClient(
        eddbWebClient: Mock(EddbWebClient),
        genericClient: Mock(GenericClient)
    )
  }

  void 'Find a station by name'() {
    given:
    String name = 'baus'
    GenericResponse<List<EddbStationResult>> expected = new GenericResponse<>(
        result: [
            new EddbStationResult(
                id: 100,
                name: 'Bauschinger',
                maxLandingPadSize: 'L',
                distanceToStar: 100,
                faction: 'Faction',
                typeId: 16,
                allegiance: 'Empire',
                system: new EddbStationResult.SystemSummary(
                    name: 'Hurukuntak',
                    id: 1001
                )
            )
        ],
        statusCode: HttpStatus.OK
    )

    when:
    List<Station> actual = eddbStationsClient.findStationsByName(name)

    then:
    1 * eddbStationsClient.genericClient.getHttpResponse({ GenericRequest genericRequest ->
      assert genericRequest.method == HttpMethod.GET
      assert genericRequest.parameters['station[name]'] == name

      return true
    } as GenericRequest) >> expected
    0 * _

    actual[0].id == '100'
    actual[0].name == 'Bauschinger'
    actual[0].url
    actual[0].distanceFromStarLs == 100
    actual[0].landingPad == PadSize.L
    actual[0].planetary

    actual[0].systemName == 'Hurukuntak'

  }
}
