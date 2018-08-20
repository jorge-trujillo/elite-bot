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
  boolean isPlanetary

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
