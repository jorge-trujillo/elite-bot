package org.jorgetrujillo.elitebot.domain.elite

class Station {

  // Basic details
  String id
  String name
  String url
  PadSize landingPad
  Long distanceFromStarLs
  Allegiance allegiance

  boolean planetary

  MaterialTraderType materialTrader

  // System name and ID
  String systemName
  String systemId

  // Distance from reference point
  Double distanceFromRefLy
}
