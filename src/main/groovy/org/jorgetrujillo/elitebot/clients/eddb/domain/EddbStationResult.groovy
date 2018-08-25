package org.jorgetrujillo.elitebot.clients.eddb.domain

import java.time.Instant

class EddbStationResult {

  long id
  String name
  String maxLandingPadSize
  long distanceToStar
  String faction
  int typeId
  String allegiance

  // 15,14,13,16,17 are Planetary
  boolean isPlanetary() {
    return typeId in [15, 14, 13, 16, 17]
  }

  SystemSummary system

  boolean hasBlackmarket
  boolean hasRefuel
  boolean hasRepair
  boolean hasRearm
  boolean hasOutfitting
  boolean hasShipyard
  boolean hasDocking
  boolean hasCommidities
  boolean hasMaterialTrader
  boolean hasTechnologyBroker

  Instant updatedAt

  static class SystemSummary {
    String name
    Long id
  }
}
