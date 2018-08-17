package org.jorgetrujillo.elitebot.services

import edu.stanford.nlp.simple.Sentence
import org.jorgetrujillo.elitebot.domain.ServiceRequest
import spock.lang.Specification
import spock.lang.Unroll

import static org.jorgetrujillo.elitebot.domain.ServiceRequest.ActionType.FIND
import static org.jorgetrujillo.elitebot.domain.ServiceRequest.ResourceType.INTERSTELLAR_FACTORS
import static org.jorgetrujillo.elitebot.domain.ServiceRequest.ResourceType.STATION
import static org.jorgetrujillo.elitebot.domain.ServiceRequest.ResourceType.SYSTEM
import static org.jorgetrujillo.elitebot.services.PartOfSpeech.CD
import static org.jorgetrujillo.elitebot.services.PartOfSpeech.DT
import static org.jorgetrujillo.elitebot.services.PartOfSpeech.JJ
import static org.jorgetrujillo.elitebot.services.PartOfSpeech.NN
import static org.jorgetrujillo.elitebot.services.PartOfSpeech.VB

class LanguageServiceSpec extends Specification {

  LanguageService languageService

  void setup() {
    languageService = new LanguageService()
  }

  @Unroll
  void 'Parse request'() {

    when:
    ServiceRequest serviceRequest = languageService.parseRequest(text)

    then:
    serviceRequest.actionType == actionType
    serviceRequest.resourceType == resourceType
    serviceRequest.systemCriteria.referenceSystemName.equalsIgnoreCase(referencePoint)

    where:
    text                                                     | actionType | resourceType         | referencePoint | modifiers
    'find nearest interstellar factors to HIP 8561'          | FIND       | INTERSTELLAR_FACTORS | 'HIP 8561'     | []
    'find the nearest station to HIP 8561'                   | FIND       | STATION              | 'HIP 8561'     | []
    'find an interstellar factors near HIP 4922'             | FIND       | INTERSTELLAR_FACTORS | 'HIP 4922'     | []
    'find the highest population system near Hurukuntak'     | FIND       | SYSTEM               | 'Hurukuntak'   | ['highest population']
    'give me the nearest interstellar factors to HIP abc-1'  | FIND       | INTERSTELLAR_FACTORS | 'HIP abc-1'    | []
    'what is the nearest low security system near HIP abc-1' | FIND       | SYSTEM               | 'HIP abc-1'    | ['low security']
    'find the nearest interstellar factors to kotilem'       | FIND       | INTERSTELLAR_FACTORS | 'kotilem'      | []
  }

  @Unroll
  void 'Find patterns of tokens for #pattern'() {

    given:
    Sentence sentence = new Sentence(text)

    when:
    List<String> actual = languageService.findPatterns(pattern, sentence)

    then:
    actual == expected
    0 * _

    where:
    text                                    | pattern  | expected
    'This is a big lamp'                    | [JJ, NN] | ['big lamp']
    'I like to play golf with a red ball'   | [JJ, NN] | ['red ball']
    'I like to play golf with a red ball'   | [VB, NN] | ['play golf']
    'I like to play golf with a red ball'   | [JJ, NN] | ['red ball']
    'Given a chance, I would hit a homerun' | [DT, NN] | ['a chance', 'a homerun']
  }

  @Unroll
  void 'Find sequence of types #wantedTypes after #markerType'() {

    given:
    Sentence sentence = new Sentence(text)

    when:
    List<String> actual = languageService.findTokensOfTypeAfter(wantedTypes, markerType, sentence)

    then:
    actual.join(' ') == expected
    0 * _

    where:
    text                                                     | wantedTypes               | markerType      | expected
    'find the nearest station to HIP 8561'                   | PartOfSpeech.nouns + [CD] | PartOfSpeech.TO | 'HIP 8561'
    'find an interstellar factors near HIP 4922'             | PartOfSpeech.nouns + [CD] | PartOfSpeech.IN | 'HIP 4922'
    'find the highest population system near Hurukuntak'     | PartOfSpeech.nouns + [CD] | PartOfSpeech.IN | 'Hurukuntak'
    'give me the nearest interstellar factors to HIP abc-1'  | PartOfSpeech.nouns + [CD] | PartOfSpeech.TO | 'HIP abc-1'
    'what is the nearest low security system near HIP abc-1' | PartOfSpeech.nouns + [CD] | PartOfSpeech.IN | 'HIP abc-1'
    'what is the nearest low security system near HIP abc-1' | PartOfSpeech.nouns + [CD] | PartOfSpeech.TO | ''
  }

}
