package org.jorgetrujillo.elitebot.domain.elite

class System {

  // Basic details
  String id
  String name
  String url
  Allegiance allegiance
  long population
  Double distanceFromRefLy

  // Powerplay
  PowerType powerType
  PowerEffect powerEffect

  // Extended info - not always set
  SecurityLevel securityLevel
  String government
  String state
  Boolean needsPermit

  // Location info
  Double x
  Double y
  Double z

  // Stations
  List<Station> stations
}
