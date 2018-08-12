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

  // Extended info
  SecurityLevel securityLevel
  String government
  String state
  Boolean needsPermit

  // Location info
  Long x
  Long y
  Long z

  // Stations
  List<Station> stations
}
