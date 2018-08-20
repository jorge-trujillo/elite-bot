package org.jorgetrujillo.elitebot.clients.eddb

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.StringUtils
import org.jorgetrujillo.elitebot.clients.GenericClient
import org.jorgetrujillo.elitebot.clients.GenericRequest
import org.jorgetrujillo.elitebot.clients.GenericResponse
import org.jorgetrujillo.elitebot.clients.StationsClient
import org.jorgetrujillo.elitebot.clients.eddb.domain.EddbStationResult
import org.jorgetrujillo.elitebot.domain.ServiceRequest
import org.jorgetrujillo.elitebot.domain.StationCriteria
import org.jorgetrujillo.elitebot.domain.elite.Allegiance
import org.jorgetrujillo.elitebot.domain.elite.PadSize
import org.jorgetrujillo.elitebot.domain.elite.Station
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component

import java.util.regex.Matcher

import static org.jorgetrujillo.elitebot.domain.StationCriteria.SortType.ARRIVAL_DISTANCE
import static org.jorgetrujillo.elitebot.domain.StationCriteria.SortType.DISTANCE_TO_REF

@Component
@Slf4j
class EddbStationsClient implements StationsClient {

  public static final String EDDB_HOST = 'https://eddb.io'
  public static final String STATIONS_URI = 'station'

  @Autowired
  EddbWebClient eddbWebClient

  @Autowired
  GenericClient genericClient

  List<Station> findStationsByName(String name) {

    List<Station> stations = []
    GenericRequest<Object, List<EddbStationResult>> genericRequest =
        new GenericRequest<Object, List<EddbStationResult>>(
            method: HttpMethod.GET,
            url: "${EDDB_HOST}/station/search",
            parameters: [
                ('station[name]'): name,
                ('expand')       : 'system'
            ],
            typeReference: new ParameterizedTypeReference<List<EddbStationResult>>() {
            }
        )

    GenericResponse<List<EddbStationResult>> response = genericClient.getHttpResponse(genericRequest)

    if (response.result) {
      stations = response.result.collect { EddbStationResult stationResult ->

        Station station = new Station(
            id: stationResult.id as String,
            name: stationResult.name,
            url: "${EDDB_HOST}/station/${stationResult.id}",
            landingPad: PadSize.valueOf(stationResult.maxLandingPadSize),
            distanceFromStarLs: stationResult.distanceToStar,
            planetary: stationResult.isPlanetary,
            systemName: stationResult.system.name
        )

        return station
      }
    }

    return stations
  }

  List<Station> findStations(StationCriteria searchRequest) {

    // Convert request into a form
    Map<String, String> formParams = buildFormParams(searchRequest)

    log.info("Querying EDDB Stations with params: ${formParams}")
    Document docCustomConn = eddbWebClient.getWebPage("${EDDB_HOST}/${STATIONS_URI}", formParams)

    // Construct response and return
    Elements stationRows = docCustomConn.select('table > tbody > tr')
    List<Station> stations = stationRows.collect { Element element ->
      Station station = new Station()
      station.id = element.attr('data-key')

      List<Element> columns = element.select('td')

      station.url = "${EDDB_HOST}${columns[0].selectFirst('a').attr('href')}"
      station.name = columns[0].selectFirst('a').text()

      if (columns[0].selectFirst('i[title]') &&
          columns[0].selectFirst('i[title]').attr('title')?.contains('planetary')) {
        station.planetary = true
      }

      station.systemName = columns[1].selectFirst('a').text()
      // Extract the system id
      String systemLink = columns[1].selectFirst('a').attr('href')
      Matcher matcher
      if ((matcher = systemLink =~ /([0-9]+)$/)) {
        station.systemId = matcher.group(1)
      }

      // Match allegiance
      String allegiance = columns[2].text()
      if (allegiance) {
        Allegiance foundAllegiance = Allegiance.values().find {
          return StringUtils.getLevenshteinDistance(it.toString().toLowerCase(), allegiance.toLowerCase()) <= 2
        }
        if (foundAllegiance) {
          station.allegiance = foundAllegiance
        }
      }

      // Match pad size
      String padSize = columns[3].text()
      if (padSize == 'L') {
        station.landingPad == PadSize.L
      } else {
        station.landingPad == PadSize.M
      }

      // Get distance from star
      String distanceFromStar = columns[4].text()
      if (distanceFromStar) {
        station.distanceFromStarLs = EddbSystemsClient.cleanNumericValue(distanceFromStar) as Long
      }

      // Get distance from reference
      String distance = columns[6].text()
      if (distance) {
        station.distanceFromRefLy = EddbSystemsClient.cleanNumericValue(distance)
      }

      return station
    }

    return stations
  }


  private Map<String, String> buildFormParams(StationCriteria searchRequest) {
    Map<String, String> formParams = [:]

    // Add form params
    if (searchRequest.referenceSystemId) {
      formParams.put('station[referenceSystemId]', searchRequest.referenceSystemId)
    }

    // Add powers
    if (searchRequest.powerEffect) {
      formParams.put('station[powerEffectTypes]',
          EddbSystemsClient.getPowerEffectType(searchRequest.powerEffect) as String)
    }
    if (searchRequest.powerType) {
      formParams.put('station[powerIds]', EddbSystemsClient.getPowerId(searchRequest.powerType) as String)
    }

    // Request orbital if a pad size is specified
    if (searchRequest.minPadSize) {
      formParams.put('station[max_landing_pad_size]', getLandingPadSizeId(searchRequest.minPadSize) as String)
    }

    // Allegiance
    if (searchRequest.allegiance) {
      formParams.put('station[allegiance_id]', EddbSystemsClient.getAllegianceId(searchRequest.allegiance) as String)
    }

    // Add sorting
    if (searchRequest.sortType) {
      formParams.put('station[sort]', getSortParameter(searchRequest.sortType))
    }

    return formParams
  }

  static int getLandingPadSizeId(PadSize padSize) {
    switch (padSize) {
      case PadSize.L:
        return 30
      case PadSize.M:
        return 20
      case PadSize.S:
        return 5
      default:
        return 5
    }
  }

  static String getSortParameter(StationCriteria.SortType sortType) {

    switch (sortType) {
      case DISTANCE_TO_REF:
        return 'referenceDistance'
      case ARRIVAL_DISTANCE:
        return 'distance_to_star'
      default:
        return ''
    }

  }
}
