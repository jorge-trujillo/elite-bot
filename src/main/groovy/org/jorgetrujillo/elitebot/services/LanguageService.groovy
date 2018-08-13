package org.jorgetrujillo.elitebot.services

import edu.stanford.nlp.simple.Sentence
import edu.stanford.nlp.simple.Token
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

import static org.jorgetrujillo.elitebot.services.ServiceRequest.ActionType.FIND

@Service
class LanguageService {

  public static final String GREETING_RESPONSE = "Hi there! I'm still not able to do much, but soon... soon..."
  public static final String DEFAULT_RESPONSE = "Yeah... no. No idea what you're asking, I only speak binary right now"

  ParseNode parseTree

  @PostConstruct
  void init() {

    parseTree = new ParseNode()

  }

  String processMessage(String message) {

    List<String> tokens = message.split('[\\s]+')

    if (message.find(/(?i)where is)/) && message.find(/(?i)system)/)) {
      println 'help'
    }
  }

  ServiceRequest parseRequest(String text) {

    ServiceRequest serviceRequest = new ServiceRequest()
    Sentence sentence = new Sentence(text)

    // Find the action (Only support find for now)
    serviceRequest.actionType = FIND

    // Find the resource type
    List<String> potentialResourceTypes = []
    potentialResourceTypes.addAll(findPatterns([PartOfSpeech.NN], sentence))
    potentialResourceTypes.addAll(findPatterns([PartOfSpeech.NN, PartOfSpeech.NN], sentence))

    if (potentialResourceTypes) {

      ServiceRequest.ResourceType matchedType = ServiceRequest.ResourceType.values()
          .find { ServiceRequest.ResourceType resourceType ->
        return potentialResourceTypes.find { it.toLowerCase() in resourceType.terms }
      }

      if (matchedType) {
        serviceRequest.resourceType = matchedType
      }
    }

    // Find any reference point if applicable
    List<PartOfSpeech> acceptableReferencePointTypes = PartOfSpeech.nouns + [PartOfSpeech.CD]
    List<String> refPointTokens = [PartOfSpeech.IN, PartOfSpeech.TO].findResult {
      return findTokensOfTypeAfter(acceptableReferencePointTypes, it, sentence) ?: null
    }
    if (refPointTokens) {
      serviceRequest.referencePoint = refPointTokens.join(' ')
    }

    // Find any modifiers for search
    List<String> modifiers = findPatterns([PartOfSpeech.JJ, PartOfSpeech.NN], sentence)
    if (!modifiers) {
      modifiers = findPatterns([PartOfSpeech.JJS, PartOfSpeech.NN], sentence)
    }
    serviceRequest.modifiers = modifiers.collect()

    return serviceRequest
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
    }

    List<String> sequences = matchedPatternIndices?.collect { int startIndex ->

      return tokens
          .subList(startIndex, startIndex + sequenceLength)
          .collect { it.originalText() }
          .join(' ')
    }

    return sequences
  }

}
