package org.jorgetrujillo.elitebot.clients

import org.jorgetrujillo.elitebot.clients.eddb.EddbSystemsClient
import org.jorgetrujillo.elitebot.clients.eddb.EddbWebClient
import spock.lang.Specification

class EddbSystemsClientSpec extends Specification {

  EddbSystemsClient eddbSystemsClient

  void setup() {
    eddbSystemsClient = new EddbSystemsClient(
        eddbWebClient: Mock(EddbWebClient)
    )
  }

  void 'FindSystems for a low sec'() {
  }
}
