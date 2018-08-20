package org.jorgetrujillo.elitebot.clients.eddb

import groovy.util.logging.Slf4j
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component

import java.time.Duration
import java.time.Instant

@Component
@Slf4j
class EddbWebClient {

  public static final String EDDB_HOST = 'https://eddb.io'
  private static final String USER_AGENT = 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6)'
  private static final int MAX_SESSION_AGE_MINUTES = 60

  final EddbSession eddbSession = new EddbSession()

  Document getWebPage(String url, Map<String, String> formParams = null) {

    // Start or refresh session as needed
    if (!eddbSession.sessionStart ||
        (Instant.now() > eddbSession.sessionStart + Duration.ofMinutes(MAX_SESSION_AGE_MINUTES))) {

      synchronized (eddbSession) {
        eddbSession.clearSession()
        refreshSession(eddbSession)
      }
    }

    return getEddbPage(url, eddbSession, formParams)
  }

  private static refreshSession(EddbSession eddbSession) {

    log.info('Refreshing EDDB session')

    Connection connection = Jsoup.connect(EDDB_HOST)
    connection.userAgent(USER_AGENT)

    // Add headers
    connection.headers(getHeaders(eddbSession))

    // Set timeout
    connection.timeout(30000)

    // Set cookies if we have them
    if (eddbSession.cookies) {
      connection.cookies(eddbSession.cookies)
    }

    // Execute call and save cookies
    Connection.Response response = connection.execute()
    eddbSession.cookies = response.cookies()

    Document docCustomConn = response.parse()

    // Save the csrf token
    eddbSession.csrfToken = docCustomConn.select('meta[name=csrf-token]').attr('content').toString()
    eddbSession.csrfTokenParam = docCustomConn.select('meta[name=csrf-param]').attr('content').toString()
  }

  private static Document getEddbPage(String url, EddbSession eddbSession, Map<String, String> formParams = null) {

    log.info("Sending web request to ${url} with params: ${formParams}")

    Connection connection = Jsoup.connect(url)
    connection.userAgent(USER_AGENT)

    // Add headers
    connection.headers(getHeaders(eddbSession))

    connection.timeout(30000)

    // Set cookies if we have them
    if (eddbSession.cookies) {
      connection.cookies(eddbSession.cookies)
    }

    // Add form params
    if (formParams) {
      if (eddbSession.csrfToken) {
        formParams.put(eddbSession.csrfTokenParam, eddbSession.csrfToken)
      }

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

  private static Map<String, String> getHeaders(EddbSession eddbSession) {
    Map<String, String> headers = [
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
        'Origin': EDDB_HOST,
        'X-Requested-With': 'XMLHttpRequest',
        'Connection': 'keep-alive'
    ]

    if (eddbSession.csrfToken) {
      headers.put('X-CSRF-Token', eddbSession.csrfToken)
    }

    return headers
  }
}

