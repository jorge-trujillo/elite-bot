package org.jorgetrujillo.elitebot.clients.eddb

import spock.lang.Specification

import java.time.Duration
import java.time.Instant

class EddbSessionSpec extends Specification {
  void 'clear session'() {

    given:
    EddbSession eddbSession = new EddbSession(
        sessionStart: Instant.now() - Duration.ofDays(1),
        cookies: ['id': 'value'],
        csrfToken: 'token'
    )

    when:
    eddbSession.clearSession()

    then:
    eddbSession.sessionStart > Instant.now() - Duration.ofMinutes(1)
    eddbSession.cookies.size() == 0
    !eddbSession.csrfToken

  }
}
