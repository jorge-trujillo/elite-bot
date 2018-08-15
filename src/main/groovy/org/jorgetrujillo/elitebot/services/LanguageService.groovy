package org.jorgetrujillo.elitebot.services

import edu.stanford.nlp.simple.Sentence
import edu.stanford.nlp.simple.Token
import groovy.util.logging.Slf4j
import org.jorgetrujillo.elitebot.domain.elite.System
import org.jorgetrujillo.elitebot.exceptions.LanguageRequestParseException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import static org.jorgetrujillo.elitebot.services.ServiceRequest.ActionType.FIND

@Service
@Slf4j
class LanguageService {

  @Autowired
  SystemsService systemsService

  String processMessage(String message) {

    try {
      // Parse the request
      ServiceRequest serviceRequest = parseRequest(message)

      // Execute it
      String response = null
      switch (serviceRequest.resourceType) {
        case ServiceRequest.ResourceType.INTERSTELLAR_FACTORS:
          System referenceSystem = systemsService.getSystemByName(serviceRequest.referencePoint)
          if (referenceSystem) {
            List<System> systems = systemsService.getNearestInterstellarFactors(referenceSystem.id)
            if (systems) {
              response = "You should try ${systems[0].name}! It is ${systems[0].distanceFromRef} from your location"
            }
          } else {
            response = "I could not locate the system ${serviceRequest.referencePoint}, sorry"
          }
          break
        default:
          response = "I can't really help yet with finding a ${serviceRequest.resourceType.name()}!"
      }

      return response
    }
    catch (LanguageRequestParseException e) {
      log.error("Exception parsing ${message}", e)
    }

    return 'I cannot understand this request! Ask me for help to see what I can do.'
  }

  ServiceRequest parseRequest(String text) {

    ServiceRequest serviceRequest = new ServiceRequest()
    Sentence sentence = new Sentence(text)

    // Find the action (Only support find for now)
    serviceRequest.actionType = FIND

    // Find the resource type
    ServiceRequest.ResourceType matchedType = ServiceRequest.ResourceType.values()
        .find { ServiceRequest.ResourceType resourceType ->
      return sentence.tokens().find { Token token ->
        resourceType.terms.find { token.originalText().toLowerCase().contains(it) }
      }
    }
    if (matchedType) {
      serviceRequest.resourceType = matchedType
    } else {
      log.warn("Could not find resource type: ${printSentence(sentence)}")
      throw new LanguageRequestParseException('resource type')
    }

    // Find any reference point if applicable

    // Oddly, the NLP framework sees a lot of system names as verbs
    List<PartOfSpeech> acceptableReferencePointTypes = PartOfSpeech.nouns + [PartOfSpeech.CD, PartOfSpeech.VB]
    List<String> refPointTokens = [PartOfSpeech.IN, PartOfSpeech.TO].findResult {
      return findTokensOfTypeAfter(acceptableReferencePointTypes, it, sentence) ?: null
    }
    if (refPointTokens) {
      serviceRequest.referencePoint = refPointTokens.join(' ')
    } else {
      log.warn("Could not find resource type: ${printSentence(sentence)}")
      throw new LanguageRequestParseException('resource type')
    }

    // Find any modifiers for search
    List<String> modifiers = findPatterns([PartOfSpeech.JJ, PartOfSpeech.NN], sentence)
    modifiers = modifiers ?: findPatterns([PartOfSpeech.JJS, PartOfSpeech.NN], sentence)

    serviceRequest.modifiers = modifiers.collect()

    return serviceRequest
  }

  private String printSentence(Sentence sentence) {
    return sentence.tokens()
        .collect { "${it.originalText()}[${it.posTag()}]" }
        .join(' ')
  }

  private static List<String> findTokensOfTypeAfter(List<PartOfSpeech> wantedTypes,
                                                    PartOfSpeech afterToken,
                                                    Sentence sentence) {

    List<String> matches = []

    Integer markerIndex = sentence.tokens().find {
      PartOfSpeech.of(it.posTag()) == afterToken
    }?.index

    if (markerIndex && markerIndex < sentence.tokens().size()) {
      int index = markerIndex + 1

      boolean allFound = false
      while (!allFound) {
        PartOfSpeech currentTag = PartOfSpeech.of(sentence.posTag(index))
        if (currentTag in wantedTypes) {
          matches.add(sentence.originalText(index))
        } else {
          allFound = true
        }

        index++
        if (index >= sentence.tokens().size()) {
          allFound = true
        }
      }

    }

    return matches
  }

  private static List<String> findPatterns(List<PartOfSpeech> tagPattern, Sentence sentence) {
    int sequenceLength = tagPattern.size()

    List<Token> tokens = sentence.tokens()

    List<Integer> matchedPatternIndices = tokens.withIndex().findResults { Token token, int index ->
      if (index + sequenceLength <= tokens.size()) {
        boolean mismatch = tagPattern.withIndex().find { PartOfSpeech patternToken, int patternIndex ->
          return patternToken != PartOfSpeech.of(tokens[index + patternIndex].posTag())
        }

        if (!mismatch) {
          return token.index()
        }
      }
    } as List<Integer>

    List<String> sequences = matchedPatternIndices?.collect { int startIndex ->

      return tokens
          .subList(startIndex, startIndex + sequenceLength)
          *.originalText()
          .join(' ')
    }

    return sequences
  }

}
