package org.jorgetrujillo.elitebot.clients.eddb

import org.jorgetrujillo.elitebot.clients.eddb.EddbSession
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component

@Component
class EddbWebClient {

  private static final String SYSTEMS_URL = 'https://eddb.io/system'
  private static final String USER_AGENT = 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6)'

  Document getWebPage(EddbSession eddbSession, Map<String, String> formParams = null) {

    Connection connection = Jsoup.connect(SYSTEMS_URL)
    connection.header('Content-Type', 'application/x-www-form-urlencoded')
    connection.userAgent(USER_AGENT)
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

    }
    else {
      connection.method(Connection.Method.GET)
    }

    // Execute call and save cookies
    Connection.Response response = connection.execute()
    eddbSession.cookies = response.cookies()

    Document docCustomConn = response.parse()

    // Save the csrf token
    eddbSession.csrfToken = docCustomConn.select('meta[name=csrf-token]').attr('content').toString()
    eddbSession.csrfTokenParam = docCustomConn.select('meta[name=csrf-param]').attr('content').toString()

    return docCustomConn
  }

}

