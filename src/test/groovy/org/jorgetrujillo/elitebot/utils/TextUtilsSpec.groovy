package org.jorgetrujillo.elitebot.utils

import spock.lang.Specification
import spock.lang.Unroll

class TextUtilsSpec extends Specification {

  @Unroll
  void 'GetLevenshteinDistance for #first and #second returns #expectedDistance'() {

    when:
    int distance = TextUtils.getLevenshteinDistance(first, second)

    then:
    distance == expectedDistance

    where:
    first  | second | expectedDistance
    'help' | 'hold' | 2
    'help' | 'Help' | 1
    'help' | 'help' | 0
    ''     | 'help' | 4
  }

  @Unroll
  void 'UnderThreshold for #first and #second with #threshold returns #expectedResult'() {
    when:
    boolean underThreshold = TextUtils.underThreshold(first, second, threshold)

    then:
    underThreshold == expectedResult

    where:
    first  | second | threshold | expectedResult
    'help' | 'hold' | 1         | false
    'help' | 'hold' | 2         | false
    'help' | 'hold' | 3         | true

  }
}
