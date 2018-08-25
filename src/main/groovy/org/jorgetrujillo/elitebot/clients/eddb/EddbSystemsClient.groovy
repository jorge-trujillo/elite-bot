package org.jorgetrujillo.elitebot.clients.eddb

import groovy.util.logging.Slf4j
import org.jorgetrujillo.elitebot.clients.GenericClient
import org.jorgetrujillo.elitebot.clients.GenericRequest
import org.jorgetrujillo.elitebot.clients.GenericResponse
import org.jorgetrujillo.elitebot.clients.SystemsClient
import org.jorgetrujillo.elitebot.clients.eddb.domain.EddbSystemResult
import org.jorgetrujillo.elitebot.domain.SystemCriteria
import org.jorgetrujillo.elitebot.domain.elite.Allegiance
import org.jorgetrujillo.elitebot.domain.elite.PadSize
import org.jorgetrujillo.elitebot.domain.elite.PowerEffect
import org.jorgetrujillo.elitebot.domain.elite.PowerType
import org.jorgetrujillo.elitebot.domain.elite.SecurityLevel
import org.jorgetrujillo.elitebot.domain.elite.Station
import org.jorgetrujillo.elitebot.domain.elite.System
import org.jorgetrujillo.elitebot.utils.TextUtils
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

import static org.jorgetrujillo.elitebot.domain.SystemCriteria.SortType.DISTANCE_TO_REF
import static org.jorgetrujillo.elitebot.domain.SystemCriteria.SortType.POPULATION
import static org.jorgetrujillo.elitebot.domain.elite.SecurityLevel.ANARCHY
import static org.jorgetrujillo.elitebot.domain.elite.SecurityLevel.HIGH
import static org.jorgetrujillo.elitebot.domain.elite.SecurityLevel.LAWLESS
import static org.jorgetrujillo.elitebot.domain.elite.SecurityLevel.LOW
import static org.jorgetrujillo.elitebot.domain.elite.SecurityLevel.MEDIUM

@Component
@Slf4j
class EddbSystemsClient implements SystemsClient {

  public static final String EDDB_HOST = 'https://eddb.io'
  public static final String SYSTEMS_URI = 'system'

  @Autowired
  EddbWebClient eddbWebClient

  @Autowired
  GenericClient genericClient

  List<System> findSystemsByName(String name) {

    List<System> systems = []
    GenericRequest<Object, List<EddbSystemResult>> genericRequest =
        new GenericRequest<Object, List<EddbSystemResult>>(
            method: HttpMethod.GET,
            url: "${EDDB_HOST}/system/search",
            parameters: [
                ('system[name]')   : name,
                ('expand')         : 'station',
                ('system[version]'): '2'
            ],
            typeReference: new ParameterizedTypeReference<List<EddbSystemResult>>() {
            }
        )

    GenericResponse<List<EddbSystemResult>> response = genericClient.getHttpResponse(genericRequest)

    if (response.result) {
      systems = response.result.collect { EddbSystemResult systemResult ->

        List<Station> stations = systemResult.stations?.collect {
          return new Station(
              id: it.id as String,
              name: it.name,
              url: "${EDDB_HOST}/station/${it.id}",
              landingPad: it.maxLandingPadSize ? PadSize.valueOf(it.maxLandingPadSize) : null,
              distanceFromStarLs: it.distanceToStar,
              allegiance: it.allegiance ? parseAllegiance(it.allegiance) : null,
              planetary: it.planetary,
              systemName: systemResult.name,
              systemId: systemResult.id as String,
          )
        }

        System system = new System(
            id: systemResult.id as String,
            name: systemResult.name,
            url: "${EDDB_HOST}/system/${systemResult.id}",
            stations: stations
        )

        return system
      }
    }

    return systems
  }

  System getSystemById(String systemId) {
    log.info("Querying EDDB Systems for ID: ${systemId}")

    Document docCustomConn = eddbWebClient.getWebPage("${EDDB_HOST}/${SYSTEMS_URI}/${systemId}")

    System system = new System()
    // Construct response and return
    system.name = docCustomConn.select('ul.breadcrumb > li.active').text()
    system.id = systemId

    Element panelBody = docCustomConn.selectFirst('div.panel-body')
    Elements rows = panelBody.select('div.row')

    String coords = rows[0].select('div.label-value')[0].text()
    if (coords) {
      String[] vals = coords.split(/[\s]+[\/][\s]+/)
      system.x = Double.valueOf(vals[0])
      system.y = Double.valueOf(vals[1])
      system.z = Double.valueOf(vals[2])
    }

    String distance = rows[0].select('div.label-value')[1].text()
    if (distance) {
      system.distanceFromRefLy = cleanNumericValue(distance)
    }

    String allegiance = rows[1].select('div.label-value')[1].text()
    if (allegiance) {
      system.allegiance = parseAllegiance(allegiance)
    }

    String security = rows[3].select('div.label-value')[1].text()
    if (security) {
      system.securityLevel = parseSecurityLevel(security)
    }

    String population = rows[4].select('div.label-value')[0].text()
    if (security) {
      system.population = Long.valueOf(population.replaceAll(/[^0-9]/, ''))
    }

    return system
  }

  List<System> findSystems(SystemCriteria searchRequest) {

    // Convert request into a form
    Map<String, String> formParams = buildFormParams(searchRequest)

    log.info("Querying EDDB Systems with params: ${formParams}")
    Document docCustomConn = eddbWebClient.getWebPage("${EDDB_HOST}/${SYSTEMS_URI}", formParams)

    // Construct response and return
    Elements systemRows = docCustomConn.select('table > tbody > tr')
    List<System> systems = systemRows.collect { Element element ->
      System system = new System()
      system.id = element.attr('data-key')

      List<Element> columns = element.select('td')

      system.url = "${EDDB_HOST}${columns[0].selectFirst('a').attr('href')}"
      system.name = columns[0].selectFirst('a').text()
      String pop = columns[3].text()?.replaceAll(',', '')
      if (pop && pop.isNumber()) {
        system.population = Long.valueOf(pop)
      }

      // Match powerplay
      String powerPlayString = columns[4].text()
      if (powerPlayString) {
        powerPlayString.split(/[\s]*:[\s]*/).each { String powerPlayToken ->
          if (powerPlayToken == 'C') {
            system.powerEffect = PowerEffect.CONTROL
          }
          if (powerPlayToken == 'E') {
            system.powerEffect = PowerEffect.EXPLOITED
          }

          PowerType foundType = PowerType.values().find { PowerType powerVal ->
            powerVal.powerName.equalsIgnoreCase(powerPlayToken)
          }
          if (foundType) {
            system.powerType = foundType
          }
        }
      }

      // Match allegiance
      String allegiance = columns[2].text()
      if (allegiance) {
        system.allegiance = parseAllegiance(allegiance)
      }

      system.distanceFromRefLy = cleanNumericValue(columns[5].text())

      return system
    }

    return systems
  }

  private Map<String, String> buildFormParams(SystemCriteria searchRequest) {
    Map<String, String> formParams = [:]

    // Add form params
    if (searchRequest.securityLevel) {
      formParams.put('system[security_id]', getSecurityId(searchRequest.securityLevel) as String)
    }

    if (searchRequest.referenceSystemId) {
      formParams.put('system[referenceSystemId]', searchRequest.referenceSystemId)
    }

    // Add powers
    if (searchRequest.powerEffect) {
      formParams.put('system[powerEffectTypes]', getPowerEffectType(searchRequest.powerEffect) as String)
    }
    if (searchRequest.powerType) {
      formParams.put('system[powerIds]', getPowerId(searchRequest.powerType) as String)
    }

    // Request orbital if a pad size is specified
    if (searchRequest.minPadSize) {
      formParams.put('system[stationFilter]', '2')
    }

    // Allegiance
    if (searchRequest.allegiance) {
      formParams.put('system[allegiance_id]', getAllegianceId(searchRequest.allegiance) as String)
    }

    // Add sorting
    if (searchRequest.sortType) {
      formParams.put('system[sort]', getSortParameter(searchRequest.sortType))
    }

    return formParams
  }

  static Allegiance parseAllegiance(String text) {
    Allegiance foundAllegiance = Allegiance.values().find { Allegiance allegiance ->
      return TextUtils.getLevenshteinDistance(allegiance.toString().toLowerCase(), text.toLowerCase()) <= 2
    }

    return foundAllegiance
  }

  static SecurityLevel parseSecurityLevel(String text) {
    SecurityLevel foundLevel = SecurityLevel.values().find { SecurityLevel securityLevel ->
      return TextUtils.getLevenshteinDistance(securityLevel.toString().toLowerCase(), text.toLowerCase()) <= 2
    }

    return foundLevel
  }

  static Double cleanNumericValue(String value) {
    String distanceNoUnits = value.replaceAll(/[^0-9.]+/, '')
    if (distanceNoUnits.number) {
      return Double.valueOf(distanceNoUnits)
    }

    return null
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

  static int getAllegianceId(Allegiance allegiance) {
    switch (allegiance) {
      case Allegiance.ALLIANCE:
        return 1
      case Allegiance.EMPIRE:
        return 2
      case Allegiance.FEDERATION:
        return 3
      case Allegiance.GUARDIAN:
        return 8
      case Allegiance.INDEPENDENT:
        return 4
      case Allegiance.PILOTS_FEDERATION:
        return 7
      case Allegiance.PIRATE:
        return 6
      default:
        return 5
    }
  }

  static int getPowerEffectType(PowerEffect powerEffect) {
    switch (powerEffect) {
      case PowerEffect.CONTROL:
        return 16
      case PowerEffect.EXPLOITED:
        return 32
      case PowerEffect.EXPANSION:
        return 64
      default:
        return 16
    }
  }

  static Integer getPowerId(PowerType powerType) {
    switch (powerType.powerName) {

      case 'Aisling Duval':
        return 6
      case 'Archon Delaine':
        return 8
      case 'Arissa Lavigny-Duval':
        return 5
      case 'Denton Patreus':
        return 7
      case 'Edmund Mahon':
        return 1
      case 'Felicia Winters':
        return 2
      case 'Li Yong-Rui':
        return 9
      case 'Pranav Antal':
        return 10
      case 'Yuri Grom':
        return 11
      case 'Zachary Hudson':
        return 3
      case 'Zemina Torval':
        return 4
      default:
        return null
    }
  }

  static String getSortParameter(SystemCriteria.SortType sortType) {

    switch (sortType) {
      case DISTANCE_TO_REF:
        return 'referenceDistance'
      case POPULATION:
        return 'population'
      default:
        return ''
    }

  }
}
