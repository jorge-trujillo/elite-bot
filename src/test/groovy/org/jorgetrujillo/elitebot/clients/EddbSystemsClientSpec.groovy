package org.jorgetrujillo.elitebot.clients

import org.jorgetrujillo.elitebot.clients.eddb.EddbSystemsClient
import org.jorgetrujillo.elitebot.clients.eddb.EddbWebClient
import org.jorgetrujillo.elitebot.clients.eddb.domain.EddbStationResult
import org.jorgetrujillo.elitebot.clients.eddb.domain.EddbSystemResult
import org.jorgetrujillo.elitebot.domain.elite.System
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import spock.lang.Specification

class EddbSystemsClientSpec extends Specification {

  EddbSystemsClient eddbSystemsClient

  void setup() {
    eddbSystemsClient = new EddbSystemsClient(
        eddbWebClient: Mock(EddbWebClient),
        genericClient: Mock(GenericClient)
    )
  }

  void 'Find a system by name'() {
    given:
    String name = 'maya'
    GenericResponse<List<EddbSystemResult>> expected = new GenericResponse<>(
        result: [
            new EddbSystemResult(
                id: 100,
                name: 'Maya',
                stations: [
                    new EddbStationResult(
                        id: 101,
                        name: 'Station 1'
                    )
                ]
            )
        ],
        statusCode: HttpStatus.OK
    )

    when:
    List<System> actual = eddbSystemsClient.findSystemsByName(name)

    then:
    1 * eddbSystemsClient.genericClient.getHttpResponse({ GenericRequest genericRequest ->
      assert genericRequest.method == HttpMethod.GET
      assert genericRequest.parameters['system[name]'] == name

      return true
    } as GenericRequest) >> expected
    0 * _

    actual[0].id == '100'
    actual[0].name == 'Maya'
    actual[0].stations[0].id == '101'
    actual[0].stations[0].name == 'Station 1'
  }
}
