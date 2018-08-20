package org.jorgetrujillo.elitebot.clients.eddb

import java.time.Instant

class EddbSession {

  Instant sessionStart

  String csrfToken
  String csrfTokenParam

  Map<String, String> cookies

  void clearSession() {
    sessionStart = Instant.now()
    csrfToken = null
    csrfTokenParam = null
    cookies = [:]
  }
}
