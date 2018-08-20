package org.jorgetrujillo.elitebot.clients

import org.jorgetrujillo.elitebot.domain.StationCriteria
import org.jorgetrujillo.elitebot.domain.elite.Station

interface StationsClient {

  List<Station> findStationsByName(String name)

  List<Station> findStations(StationCriteria stationCriteria)
}
