package org.jorgetrujillo.elitebot.services

import org.apache.commons.lang3.StringUtils
import org.jorgetrujillo.elitebot.domain.ServiceRequest
import org.jorgetrujillo.elitebot.domain.elite.Allegiance
import org.jorgetrujillo.elitebot.domain.elite.PadSize
import org.jorgetrujillo.elitebot.domain.elite.PowerEffect
import org.jorgetrujillo.elitebot.domain.elite.PowerType
import org.jorgetrujillo.elitebot.domain.elite.SecurityLevel
import org.springframework.stereotype.Service

@Service
class SimpleRequestService {

  /**
   * Parse a request in structured form. Format is:
   * from: <system> to: <resource target> pad: <L|Large> allegiance: <fed|empire|independent> state: <state>
   *   security: <low|med|high> power: <power name> C|E
   * @param text
   * @return
   */
  ServiceRequest parseRequest(String text) {

    ServiceRequest serviceRequest = new ServiceRequest()

    text = text.replaceAll(/:[\s]*/, ': ')
    List<String> tokens = text.toLowerCase().split(/[\s]+/)

    tokens.eachWithIndex { String token, int index ->

      if (token.equalsIgnoreCase('find:')) {
        serviceRequest.actionType = ServiceRequest.ActionType.FIND

        List<String> resourceTypeTokens = getTokensAfter(index, tokens)

        ServiceRequest.ResourceType resourceType = ServiceRequest.ResourceType.values().find {
          ServiceRequest.ResourceType resourceTypeVal ->
            List<String> resourceTypeTerms = resourceTypeVal.terms
            return resourceTypeTokens.find { String resourceTypeToken ->
              return resourceTypeTerms.find { measureDistance(it, resourceTypeToken) <= 2 }
            }
        }

        serviceRequest.resourceType = resourceType
      }

      if (token.equalsIgnoreCase('distance:')) {
        serviceRequest.actionType = ServiceRequest.ActionType.COMPUTE_DISTANCE

        int toTokenIndex = tokens.findIndexOf { it == 'to:' }
        String systemA = getTokensAfter(index, tokens).join(' ')
        String systemB = getTokensAfter(toTokenIndex, tokens).join(' ')

        serviceRequest.systemPair = new Tuple2<String, String>(systemA, systemB)
      }

      if (token.equalsIgnoreCase('system:')) {
        serviceRequest.actionType = ServiceRequest.ActionType.SYSTEM_DETAILS

        String systemName = getTokensAfter(index, tokens).join(' ')
        serviceRequest.systemCriteria.referenceSystemName = systemName
      }

      if (token.equalsIgnoreCase('near:')) {

        String systemName = getTokensAfter(index, tokens).join(' ')
        serviceRequest.systemCriteria.referenceSystemName = systemName
      }

      if (token.equalsIgnoreCase('pad:')) {

        List<String> padTokens = getTokensAfter(index, tokens)
        if (padTokens) {
          serviceRequest.systemCriteria.minPadSize = padTokens.first().startsWith('l') ? PadSize.L : null
        }
      }

      if (token.equalsIgnoreCase('allegiance:')) {

        String allegianceString = getTokensAfter(index, tokens).join(' ')
        Allegiance allegiance = Allegiance.values().find {
          return measureDistance(allegianceString, it.toString().toLowerCase()) <= 2
        }
        serviceRequest.systemCriteria.allegiance = allegiance
      }

      if (token.equalsIgnoreCase('security:')) {

        String security = getTokensAfter(index, tokens).join(' ')
        SecurityLevel securityLevel = SecurityLevel.values().find {
          return measureDistance(security, it.toString().toLowerCase()) <= 2
        }
        serviceRequest.systemCriteria.securityLevel = securityLevel
      }

      if (token.equalsIgnoreCase('power:')) {

        List<String> powerTokens = getTokensAfter(index, tokens)
        if (powerTokens.last() in ['c', 'e']) {
          String powerEffectString = powerTokens.pop()
          PowerEffect powerEffect = powerEffectString == 'c' ? PowerEffect.CONTROL : PowerEffect.EXPLOITED
          serviceRequest.systemCriteria.powerEffect = powerEffect
        }

        PowerType powerType = PowerType.values().find { PowerType powerTypeVal ->
          List<String> powerNameTokens = powerTypeVal.powerName.toLowerCase().split(/[\s-_]+/)
          return powerNameTokens.find { String powerNameToken ->
            return powerTokens.find { measureDistance(it, powerNameToken) <= 2 }
          }
        }
        serviceRequest.systemCriteria.powerType = powerType
      }
    }

    return serviceRequest
  }

  private static List<String> getTokensAfter(int tokenIndex, List<String> tokens) {

    List<String> matchingTokens = []

    if (tokenIndex < tokens.size() + 1) {
      tokens
          .subList(tokenIndex + 1, tokens.size()).any { String token ->

        boolean isParameterName = token.endsWith(':')
        if (!isParameterName) {
          matchingTokens.add(token)
        }
        return isParameterName
      }
    }

    return matchingTokens
  }

  private static int measureDistance(String one, String two) {
    StringUtils.getLevenshteinDistance(one, two)
  }
}
