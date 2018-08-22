package org.jorgetrujillo.elitebot.domain

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class ServiceRequest {

  ActionType actionType
  ResourceType resourceType

  List<String> systemPair

  SystemCriteria systemCriteria = new SystemCriteria()

  enum ActionType {
    FIND, SYSTEM_DETAILS, COMPUTE_DISTANCE
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
