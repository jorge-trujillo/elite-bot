package org.jorgetrujillo.elitebot.domain.elite

class System {

  // Basic details
  String id
  String name
  String url
  String allegiance
  long population
  String distanceFromRef

  // Powerplay
  String powerPlay

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
