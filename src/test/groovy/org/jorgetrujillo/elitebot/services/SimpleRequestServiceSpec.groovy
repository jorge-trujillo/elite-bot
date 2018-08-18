package org.jorgetrujillo.elitebot.services

import org.jorgetrujillo.elitebot.domain.ServiceRequest
import org.jorgetrujillo.elitebot.domain.SystemCriteria
import org.jorgetrujillo.elitebot.domain.elite.Allegiance
import org.jorgetrujillo.elitebot.domain.elite.PadSize
import org.jorgetrujillo.elitebot.domain.elite.PowerEffect
import org.jorgetrujillo.elitebot.domain.elite.PowerType
import org.jorgetrujillo.elitebot.domain.elite.SecurityLevel
import spock.lang.Specification
import spock.lang.Unroll

import static org.jorgetrujillo.elitebot.domain.ServiceRequest.ActionType.COMPUTE_DISTANCE
import static org.jorgetrujillo.elitebot.domain.ServiceRequest.ActionType.FIND
import static org.jorgetrujillo.elitebot.domain.ServiceRequest.ActionType.SYSTEM_DETAILS
import static org.jorgetrujillo.elitebot.domain.ServiceRequest.ResourceType.INTERSTELLAR_FACTORS
import static org.jorgetrujillo.elitebot.domain.ServiceRequest.ResourceType.SYSTEM

class SimpleRequestServiceSpec extends Specification {

  SimpleRequestService simpleRequestService

  void setup() {
    simpleRequestService = new SimpleRequestService()
  }

  @Unroll
  void 'Parse a system search request for: #text'() {

    when:
    ServiceRequest serviceRequest = simpleRequestService.parseRequest(text)

    then:
    serviceRequest.actionType == actionType
    serviceRequest.resourceType == resourceType

    SystemCriteria systemCriteria = serviceRequest.systemCriteria
    systemCriteria.referenceSystemName.equalsIgnoreCase(referencePoint)
    systemCriteria.minPadSize == pad
    systemCriteria.allegiance == allegiance
    systemCriteria.securityLevel == security
    systemCriteria.powerType == powerType
    systemCriteria.powerEffect == powerEffect

    where:
    text                                                     | actionType | resourceType | referencePoint | pad       | allegiance        | security          | powerType                | powerEffect
    'find: system near: hurukuntak'                          | FIND       | SYSTEM       | 'hurukuntak'   | null      | null              | null              | null                     | null
    'find:system near:hurukuntak'                            | FIND       | SYSTEM       | 'hurukuntak'   | null      | null              | null              | null                     | null
    'find: system near: HIP 8225 pad: L'                     | FIND       | SYSTEM       | 'HIP 8225'     | PadSize.L | null              | null              | null                     | null
    'find: system near: maya allegiance: empire'             | FIND       | SYSTEM       | 'maya'         | null      | Allegiance.EMPIRE | null              | null                     | null
    'find: system near: hurukuntak security: low pad: L'     | FIND       | SYSTEM       | 'hurukuntak'   | PadSize.L | null              | SecurityLevel.LOW | null                     | null
    'find: system near: hurukuntak security: low pad: large' | FIND       | SYSTEM       | 'hurukuntak'   | PadSize.L | null              | SecurityLevel.LOW | null                     | null
    'find: system near: hurukuntak power: torval C'          | FIND       | SYSTEM       | 'hurukuntak'   | null      | null              | null              | PowerType.ZEMINA_TORVAL  | PowerEffect.CONTROL
    'find: system near: hurukuntak power: aisling'           | FIND       | SYSTEM       | 'hurukuntak'   | null      | null              | null              | PowerType.AISLING_DUVAL  | null
    'find: system near: hurukuntak power: hudson E'          | FIND       | SYSTEM       | 'hurukuntak'   | null      | null              | null              | PowerType.ZACHARY_HUDSON | PowerEffect.EXPLOITED

  }

  @Unroll
  void 'Parse an interstellar factors request for: #text'() {

    when:
    ServiceRequest serviceRequest = simpleRequestService.parseRequest(text)

    then:
    serviceRequest.actionType == actionType
    serviceRequest.resourceType == resourceType

    SystemCriteria systemCriteria = serviceRequest.systemCriteria
    systemCriteria.referenceSystemName.equalsIgnoreCase(referencePoint)
    systemCriteria.minPadSize == pad
    systemCriteria.allegiance == allegiance
    systemCriteria.securityLevel == security
    systemCriteria.powerType == powerType
    systemCriteria.powerEffect == powerEffect

    where:
    text                                               | actionType | resourceType         | referencePoint | pad       | allegiance | security | powerType | powerEffect
    'find: interstellar factors near: hurukuntak'      | FIND       | INTERSTELLAR_FACTORS | 'hurukuntak'   | null      | null       | null     | null      | null
    'find:interstellar near:hurukuntak'                | FIND       | INTERSTELLAR_FACTORS | 'hurukuntak'   | null      | null       | null     | null      | null
    'find: interstellar factors near: HIP 8225 pad: L' | FIND       | INTERSTELLAR_FACTORS | 'HIP 8225'     | PadSize.L | null       | null     | null      | null
  }


  @Unroll
  void 'Parse a details request for: #text'() {

    when:
    ServiceRequest serviceRequest = simpleRequestService.parseRequest(text)

    then:
    serviceRequest.actionType == actionType

    SystemCriteria systemCriteria = serviceRequest.systemCriteria
    systemCriteria.referenceSystemName.equalsIgnoreCase(referencePoint)

    where:
    text                 | actionType     | referencePoint
    'system: hurukuntak' | SYSTEM_DETAILS | 'hurukuntak'
    'system:maya'        | SYSTEM_DETAILS | 'maya'
    'system: HIP 861-5a' | SYSTEM_DETAILS | 'HIP 861-5a'
  }

  @Unroll
  void 'Parse a distance request for: #text'() {

    when:
    ServiceRequest serviceRequest = simpleRequestService.parseRequest(text)

    then:
    serviceRequest.actionType == actionType
    serviceRequest.systemPair[0] == systemA
    serviceRequest.systemPair[1] == systemB

    where:
    text                           | actionType       | systemA      | systemB
    'distance: hurukuntak to: sol' | COMPUTE_DISTANCE | 'hurukuntak' | 'sol'
    'distance:maya to: hip 8119'   | COMPUTE_DISTANCE | 'maya'       | 'hip 8119'
    'distance: maya to: maya'      | COMPUTE_DISTANCE | 'maya'       | 'maya'
  }
}
