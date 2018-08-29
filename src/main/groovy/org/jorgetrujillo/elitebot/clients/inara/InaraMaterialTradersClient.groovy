package org.jorgetrujillo.elitebot.clients.inara

import groovy.util.logging.Slf4j
import org.jorgetrujillo.elitebot.clients.inara.domain.MaterialTraderResult
import org.jorgetrujillo.elitebot.domain.elite.MaterialTraderType
import org.jorgetrujillo.elitebot.utils.TextUtils
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.stereotype.Component

@Slf4j
@Component
class InaraMaterialTradersClient {

  public static final String HOST = 'https://inara.cz'
  private static final String USER_AGENT = 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6)'

  List<MaterialTraderResult> getMaterialTradersNear(String systemName) {

    Map<String, String> params = [
        'location'    : 'galaxy-nearest',
        'formact'     : 'SEARCH_GALAXY_STAR',
        'searchgalaxy': systemName
    ]
    Document document = getPage("${HOST}/galaxy-nearest/25/4767/", params)
    Elements rows = document.select('div.mainblock > table > tbody > tr')

    /**
     * Get all mat traders nearby the station, as a list of MaterialTraderResult
     */
    List<MaterialTraderResult> matTraders = rows.collect { Element row ->

      MaterialTraderResult materialTraderResult = new MaterialTraderResult()

      Elements columns = row.select('td')

      // Match mat trader
      MaterialTraderType foundMatTrader
      String matTrader = columns[0].selectFirst('span.major')?.text() ?: columns[0].text()

      if (matTrader) {
        matTrader = matTrader.indexOf('/') > 0 ? matTrader.split(/[\s\/]+/)[0] : matTrader
        foundMatTrader = MaterialTraderType.values().find {
          return TextUtils.underThreshold(it.toString().toLowerCase(), matTrader.toLowerCase(), 2)
        }
        materialTraderResult.materialTraderType = foundMatTrader
      }

      String stationName = columns[1].selectFirst('a').text()
      materialTraderResult.stationName = stationName

      String name = columns[2].selectFirst('a').text()
      materialTraderResult.systemName = name

      String distanceFromStar = columns[6].text()
      if (distanceFromStar) {
        materialTraderResult.distanceFromStarLs = Long.valueOf(distanceFromStar.replaceAll(/[^0-9.]+/, ''))
      }

      String distanceFromRef = columns[7].text()
      if (distanceFromRef) {
        materialTraderResult.distanceFromRefSystemLy = (distanceFromRef == 'here') ?
            0 : Double.valueOf(distanceFromRef.replaceAll(/[^0-9.]/, ''))
      }

      return materialTraderResult
    }

    return matTraders
  }

  private Document getPage(String url, Map<String, String> formParams = null) {

    log.info("Sending web request to ${url} with params: ${formParams}")

    Connection connection = Jsoup.connect(url)
    connection.userAgent(USER_AGENT)

    // Add headers
    connection.headers(getHeaders(url))

    connection.timeout(30000)

    // Add form params
    if (formParams) {

      formParams.each {
        connection.data(it.key, it.value)
      }
      connection.method(Connection.Method.POST)

    } else {
      connection.method(Connection.Method.GET)
    }

    // Execute call
    Connection.Response response = connection.execute()
    Document docCustomConn = response.parse()

    return docCustomConn
  }

  private static Map<String, String> getHeaders(String url) {
    Map<String, String> headers = [
        'authority'                : 'inara.cz',
        'cache-control'            : 'max-age=0',
        'origin'                   : HOST,
        'upgrade-insecure-requests': '1',
        'content-type'             : 'application/x-www-form-urlencoded',
        'referer'                  : url,
        'accept-encoding'          : 'gzip, deflate, br',
        'accept-language'          : 'en-US,en;q=0.9'
    ]

    return headers
  }

}
