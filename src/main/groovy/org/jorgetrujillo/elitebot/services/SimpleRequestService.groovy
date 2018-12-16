package org.jorgetrujillo.elitebot.services

import org.jorgetrujillo.elitebot.domain.ServiceRequest
import org.jorgetrujillo.elitebot.domain.elite.Allegiance
import org.jorgetrujillo.elitebot.domain.elite.PadSize
import org.jorgetrujillo.elitebot.domain.elite.PowerEffect
import org.jorgetrujillo.elitebot.domain.elite.PowerType
import org.jorgetrujillo.elitebot.domain.elite.SecurityLevel
import org.jorgetrujillo.elitebot.exceptions.SimpleRequestField
import org.jorgetrujillo.elitebot.exceptions.SimpleRequestParseException
import org.jorgetrujillo.elitebot.utils.TextUtils
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
  ServiceRequest parseRequest(String requestText) {

    ServiceRequest serviceRequest = new ServiceRequest()

    String text = requestText.replaceAll(/:[\s]*/, ': ')
    List<String> tokens = Arrays.asList(text.toLowerCase().split(/[\s]+/))

    tokens.eachWithIndex { String token, int index ->

      if (token.equalsIgnoreCase('find:')) {
        parseFindTokens(index, tokens, serviceRequest)
      }

      if (token.equalsIgnoreCase('distance:')) {
        parseDistanceTokens(index, tokens, serviceRequest)
      }

      if (token.equalsIgnoreCase('system:')) {
        parseSystemTokens(index, tokens, serviceRequest)
      }

      if (token.equalsIgnoreCase('near:')) {
        parseNearTokens(index, tokens, serviceRequest)
      }

      if (token.equalsIgnoreCase('pad:')) {
        parsePadTokens(index, tokens, serviceRequest)
      }

      if (token.equalsIgnoreCase('allegiance:')) {
        parseAllegianceTokens(index, tokens, serviceRequest)
      }

      if (token.equalsIgnoreCase('security:')) {
        parseSecurityLevelTokens(index, tokens, serviceRequest)
      }

      if (token.equalsIgnoreCase('power:')) {
        parsePowerTokens(index, tokens, serviceRequest)
      }
    }

    return serviceRequest
  }

  private void parseFindTokens(int index, List<String> tokens, ServiceRequest serviceRequest) {
    serviceRequest.actionType = ServiceRequest.ActionType.FIND

    ServiceRequest.ResourceType foundResourceType = matchTokensToValue(
        index,
        tokens,
        ServiceRequest.ResourceType.values().collect { ServiceRequest.ResourceType value ->
          return new Tuple2<List<String>, ServiceRequest.ResourceType>(value.terms, value)
        },
        true
    )
    serviceRequest.resourceType = foundResourceType

    if (!serviceRequest.resourceType) {
      throw new SimpleRequestParseException(SimpleRequestField.RESOURCE_TYPE)
    }
  }

  private void parseDistanceTokens(int index, List<String> tokens, ServiceRequest serviceRequest) {

    serviceRequest.actionType = ServiceRequest.ActionType.COMPUTE_DISTANCE

    int toTokenIndex = tokens.findIndexOf { it == 'to:' }
    String systemA = getTokensAfter(index, tokens).join(' ')
    String systemB = getTokensAfter(toTokenIndex, tokens).join(' ')
    serviceRequest.systemPair = new Tuple2<String, String>(systemA, systemB)

    if (!systemA || !systemB) {
      throw new SimpleRequestParseException(SimpleRequestField.DISTANCE_LOCATION)
    }
  }

  private void parseSystemTokens(int index, List<String> tokens, ServiceRequest serviceRequest) {
    serviceRequest.actionType = ServiceRequest.ActionType.SYSTEM_DETAILS

    String systemName = getTokensAfter(index, tokens).join(' ')
    serviceRequest.systemCriteria.referenceSystemName = systemName

    if (!serviceRequest.systemCriteria.referenceSystemName) {
      throw new SimpleRequestParseException(SimpleRequestField.DETAILS_LOCATION)
    }
  }

  private void parseNearTokens(int index, List<String> tokens, ServiceRequest serviceRequest) {
    String systemName = getTokensAfter(index, tokens).join(' ')
    serviceRequest.systemCriteria.referenceSystemName = systemName

    if (!serviceRequest.systemCriteria.referenceSystemName) {
      throw new SimpleRequestParseException(SimpleRequestField.REFERENCE_SYSTEM)
    }
  }

  private void parsePadTokens(int index, List<String> tokens, ServiceRequest serviceRequest) {
    PadSize foundPadSize = matchTokensToValue(
        index,
        tokens,
        PadSize.values().collect { PadSize value ->
          return new Tuple2<List<String>, PadSize>([value.toString(), value.name], value)
        }
    )
    serviceRequest.systemCriteria.minPadSize = foundPadSize

    if (!serviceRequest.systemCriteria.minPadSize) {
      throw new SimpleRequestParseException(SimpleRequestField.PAD_SIZE)
    }
  }

  private void parseAllegianceTokens(int index, List<String> tokens, ServiceRequest serviceRequest) {
    Allegiance foundAllegiance = matchTokensToValue(
        index,
        tokens,
        Allegiance.values().collect { Allegiance value ->
          return new Tuple2<List<String>, Allegiance>([value.toString()], value)
        },
        true
    )

    serviceRequest.systemCriteria.allegiance = foundAllegiance

    if (!serviceRequest.systemCriteria.allegiance) {
      throw new SimpleRequestParseException(SimpleRequestField.ALLEGIANCE)
    }
  }

  private void parseSecurityLevelTokens(int index, List<String> tokens, ServiceRequest serviceRequest) {
    SecurityLevel foundLevel = matchTokensToValue(
        index,
        tokens,
        SecurityLevel.values().collect { SecurityLevel value ->
          return new Tuple2<List<String>, SecurityLevel>([value.toString()], value)
        },
        true
    )
    serviceRequest.systemCriteria.securityLevel = foundLevel

    if (!serviceRequest.systemCriteria.securityLevel) {
      throw new SimpleRequestParseException(SimpleRequestField.SECURITY_LEVEL)
    }
  }

  private void parsePowerTokens(int index, List<String> tokens, ServiceRequest serviceRequest) {
    List<String> powerTokens = getTokensAfter(index, tokens)
    if (powerTokens.last() in ['c', 'e']) {
      String powerEffectString = powerTokens.removeLast()
      PowerEffect powerEffect = powerEffectString == 'c' ? PowerEffect.CONTROL : PowerEffect.EXPLOITED
      serviceRequest.systemCriteria.powerEffect = powerEffect
    }

    PowerType powerType = PowerType.values().find { PowerType powerTypeVal ->
      List<String> powerNameTokens = Arrays.asList(powerTypeVal.powerName.toLowerCase().split(/[\s-_]+/))
      return powerNameTokens.find { String powerNameToken ->
        return powerTokens.find { TextUtils.underThreshold(it, powerNameToken, 3) }
      }
    }
    serviceRequest.systemCriteria.powerType = powerType

    if (!serviceRequest.systemCriteria.powerType) {
      throw new SimpleRequestParseException(SimpleRequestField.POWER)
    }
  }

  private <T> T matchTokensToValue(int tokenIndex, List<String> tokens, List<Tuple2<List<String>, T>> values,
                                   boolean fuzzy = false) {

    // Get the tokens after the provided index
    List<String> valueTokens = getTokensAfter(tokenIndex, tokens)

    // Now find the right value from the provided value set
    T foundValue = values.findResult { Tuple2<List<String>, T> value ->

      if (value.getFirst().find { String stringValue ->

        boolean matchAsWhole = fuzzy ?
            TextUtils.underThreshold(valueTokens.join(' ').toLowerCase(), stringValue.toLowerCase(), 3) :
            valueTokens.join(' ').toLowerCase() == stringValue.toLowerCase()

        return matchAsWhole || valueTokens.find {
          if (fuzzy) {
            return TextUtils.underThreshold(it.toLowerCase(), stringValue.toLowerCase(), 3) ||
                TextUtils.underThreshold(it.toLowerCase(), stringValue.toLowerCase(), 3)
          }

          // Straight match if not fuzzy
          return it.toLowerCase() == stringValue.toLowerCase()
        }

      }) {
        return value.getSecond()
      }

      return null as T
    }

    return foundValue
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

}
