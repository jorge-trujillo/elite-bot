package org.jorgetrujillo.elitebot.clients.eddb

import groovy.util.logging.Slf4j
import org.jorgetrujillo.elitebot.clients.GenericClient
import org.jorgetrujillo.elitebot.clients.GenericRequest
import org.jorgetrujillo.elitebot.clients.GenericResponse
import org.jorgetrujillo.elitebot.clients.SystemsClient
import org.jorgetrujillo.elitebot.clients.eddb.domain.EddbStationResult
import org.jorgetrujillo.elitebot.clients.eddb.domain.EddbSystemResult
import org.jorgetrujillo.elitebot.domain.SystemsSearchRequest
import org.jorgetrujillo.elitebot.domain.SystemsSearchRequest.SortType
import org.jorgetrujillo.elitebot.domain.elite.SecurityLevel
import org.jorgetrujillo.elitebot.domain.elite.Station
import org.jorgetrujillo.elitebot.domain.elite.System
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

import static org.jorgetrujillo.elitebot.domain.elite.SecurityLevel.ANARCHY
import static org.jorgetrujillo.elitebot.domain.elite.SecurityLevel.HIGH
import static org.jorgetrujillo.elitebot.domain.elite.SecurityLevel.LAWLESS
import static org.jorgetrujillo.elitebot.domain.elite.SecurityLevel.LOW
import static org.jorgetrujillo.elitebot.domain.elite.SecurityLevel.MEDIUM

@Component
@Slf4j
class EddbSystemsClient implements SystemsClient {

  private static final String EDDB_HOST = 'https://eddb.io'

  @Autowired
  EddbWebClient eddbWebClient

  @Autowired
  GenericClient genericClient

  System findSystemByName(String name) {

    List<System> systems = []
    GenericRequest<Object, List<EddbStationResult>> genericRequest = new GenericRequest<>(
        method: HttpMethod.GET,
        url: "${EDDB_HOST}/system/search",
        parameters: [
            ('system[name]')   : name,
            ('expand')         : 'station',
            ('system[version]'): '2'
        ],
        typeReference: new ParameterizedTypeReference<List<EddbStationResult>>() {
        }
    )

    GenericResponse<List<EddbStationResult>> response = genericClient.getHttpResponse(genericRequest)
    if (response.result) {
      systems = response.result.collect { EddbSystemResult systemResult ->

        List<Station> stations = systemResult.stations?.collect {
          return new Station(
              id: it.id,
              name: it.name,
              landingPad: it.maxLandingPadSize,
              distanceFromStarLs: it.distanceToStar
          )
        }

        System system = new System(
            id: systemResult.id,
            name: systemResult.name,
            stations: stations
        )

        return system
      }

    }

    return systems
  }

  List<System> findSystems(SystemsSearchRequest searchRequest) {

    // Convert request into a form
    Map<String, String> formParams = [:]

    // Add form params
    if (searchRequest.securityLevel) {
      formParams.put('system[security_id]', getSecurityId(searchRequest.securityLevel) as String)
    }

    if (searchRequest.referenceSystemId) {
      formParams.put('system[referenceSystemId]', searchRequest.referenceSystemId)
    }
    // Add sorting
    if (searchRequest.sortType) {
      formParams.put('system[sort]', getSortParameter(searchRequest.sortType))
    }

    // Start session
    EddbSession eddbSession = new EddbSession()
    eddbWebClient.getWebPage(eddbSession)

    log.info("Querying EDDB with params: ${formParams}")
    Document docCustomConn = eddbWebClient.getWebPage(eddbSession, formParams)

    // Construct response and return
    println docCustomConn.text()

    Elements systemRows = docCustomConn.select('table > tbody > tr')
    List<System> systems = systemRows.collect { Element element ->
      System system = new System()
      system.id = element.attr('data-key')

      List<Element> columns = element.select('td')

      system.url = "${EDDB_HOST}${columns[0].selectFirst('a').attr('href')}"
      system.name = columns[0].selectFirst('a').text()
      system.allegiance = columns[2].text()
      String pop = columns[3].text()?.replaceAll(',', '')
      if (pop && pop.isNumber()) {
        system.population = Long.valueOf(pop)
      }
      system.powerPlay = columns[4].text()
      system.distanceFromRef = columns[5].text()

      return system
    }

    return systems

  }

  static int getSecurityId(SecurityLevel securityLevel) {

    switch (securityLevel) {
      case LAWLESS:
        return 80
      case ANARCHY:
        return 64
      case LOW:
        return 16
      case MEDIUM:
        return 32
      case HIGH:
        return 48
      default:
        return 16
    }
  }

  static String getSortParameter(SortType sortType) {

    switch (sortType) {
      case SortType.DISTANCE_TO_REF:
        return 'referenceDistance'
      case SortType.POPULATION:
        return 'population'
      default:
        return ''
    }

  }
}
