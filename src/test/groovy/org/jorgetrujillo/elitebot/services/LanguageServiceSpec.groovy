package org.jorgetrujillo.elitebot.services

import spock.lang.Specification
import spock.lang.Unroll

class LanguageServiceSpec extends Specification {

  LanguageService languageService

  void setup() {
    languageService = new LanguageService()
  }

  @Unroll
  void 'ProcessMessage'() {

    when:
    String actual = languageService.processMessage(input)

    then:
    actual.is(expected)
    0 * _

    where:
    input  | expected
    'hi'   | LanguageService.GREETING_RESPONSE
    'blah' | LanguageService.DEFAULT_RESPONSE

  }
}
