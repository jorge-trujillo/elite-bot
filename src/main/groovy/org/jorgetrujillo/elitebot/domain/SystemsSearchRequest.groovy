package org.jorgetrujillo.elitebot.domain

import org.jorgetrujillo.elitebot.domain.elite.SecurityLevel

/**
 * Search request for systems.
 * Either the system ID or system name must be used. If both are set, ID will be used.
 */
class SystemsSearchRequest {
  String referenceSystemId
  String referenceSystemName

  boolean populated
  SecurityLevel securityLevel

  // Paging
  SortType sortType

  enum SortType {
    DISTANCE_TO_REF, POPULATION
  }
}
