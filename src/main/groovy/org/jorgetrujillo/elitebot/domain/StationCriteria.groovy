package org.jorgetrujillo.elitebot.domain

import org.jorgetrujillo.elitebot.domain.elite.Allegiance
import org.jorgetrujillo.elitebot.domain.elite.PadSize
import org.jorgetrujillo.elitebot.domain.elite.PowerEffect
import org.jorgetrujillo.elitebot.domain.elite.PowerType
import org.jorgetrujillo.elitebot.domain.elite.SecurityLevel

class StationCriteria {

  String referenceSystemName
  String referenceSystemId
  PadSize minPadSize
  Allegiance allegiance
  SecurityLevel securityLevel

  PowerType powerType
  PowerEffect powerEffect

  // Paging
  SortType sortType

  enum SortType {
    DISTANCE_TO_REF, ARRIVAL_DISTANCE
  }
}
