package org.jorgetrujillo.elitebot.clients.inara.domain

import org.jorgetrujillo.elitebot.domain.elite.MaterialTraderType

class MaterialTraderResult {
  String stationName
  String systemName
  MaterialTraderType materialTraderType

  Long distanceFromStarLs
  Double distanceFromRefSystemLy
}
