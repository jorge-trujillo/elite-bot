package org.jorgetrujillo.elitebot.services

class ServiceRequest {

  ActionType actionType
  ResourceType resourceType
  String referencePoint
  List<String> modifiers

  enum ActionType {
    FIND
  }

  enum ResourceType {
    SYSTEM(['system']),
    STATION(['station']),
    INTERSTELLAR_FACTORS(['interstellar', 'factors'])

    List<String> terms

    ResourceType(List<String> terms) {
      this.terms = terms
    }
  }
}
