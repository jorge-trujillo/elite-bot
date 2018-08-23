package org.jorgetrujillo.elitebot.clients

import org.jorgetrujillo.elitebot.IntegrationTestBase
import org.jorgetrujillo.elitebot.clients.eddb.EddbStationsClient
import org.jorgetrujillo.elitebot.domain.StationCriteria
import org.jorgetrujillo.elitebot.domain.elite.Allegiance
import org.jorgetrujillo.elitebot.domain.elite.PadSize
import org.jorgetrujillo.elitebot.domain.elite.PowerEffect
import org.jorgetrujillo.elitebot.domain.elite.PowerType
import org.jorgetrujillo.elitebot.domain.elite.Station
import org.springframework.beans.factory.annotation.Autowired

class EddbStationsClientIntegrationSpec extends IntegrationTestBase {

  @Autowired
  EddbStationsClient eddbStationsClient

  void 'Find a station by name'() {

    given:
    String stationName = 'jameson'

    when:
    List<Station> stations = eddbStationsClient.findStationsByName(stationName)

    then:
    stations

    and: 'First station is Jameson'
    stations[0].name == 'Jameson Memorial'
    stations[0].id == '571'
    stations[0].url != null
    !stations[0].planetary
    stations[0].landingPad == PadSize.L
    stations[0].distanceFromStarLs == 327

    stations[0].systemName == 'Shinrarta Dezhra'

    and: 'Second system is Jameson Base'
    stations[1].name == 'Jameson Base'
    stations[1].id == '64351'
    stations[1].planetary

  }

  void 'Find large pad station near another system'() {

    given:
    String hurukuntakId = '9608'
    StationCriteria stationCriteria = new StationCriteria(
        referenceSystemId: hurukuntakId,
        sortType: StationCriteria.SortType.DISTANCE_TO_REF,
        minPadSize: PadSize.L
    )

    when:
    List<Station> stations = eddbStationsClient.findStations(stationCriteria)

    then:
    stations
    stations[0].name == 'Bauschinger Terminal'
    stations[0].id == '3651'
    stations[0].url
    !stations[0].planetary
    stations[0].allegiance == Allegiance.EMPIRE
    stations[0].distanceFromStarLs == 17
    stations[0].distanceFromRefLy == 0
    stations[0].landingPad == PadSize.L

    stations[0].systemName == 'Hurukuntak'
    stations[0].systemId == '9608'

    and:
    stations[1].name == 'Dickson Point'
    stations[1].id == '43888'
    stations[1].url
    stations[1].planetary
    stations[1].allegiance == Allegiance.EMPIRE
    stations[1].distanceFromStarLs == 170186
    stations[1].distanceFromRefLy == 0
    stations[1].landingPad == PadSize.L

    stations[1].systemName == 'Hurukuntak'
    stations[1].systemId == '9608'

  }

  void 'Find federation station near another system'() {

    given:
    String hurukuntakId = '9608'
    StationCriteria stationCriteria = new StationCriteria(
        referenceSystemId: hurukuntakId,
        sortType: StationCriteria.SortType.DISTANCE_TO_REF,
        minPadSize: PadSize.M,
        allegiance: Allegiance.FEDERATION
    )

    when:
    List<Station> stations = eddbStationsClient.findStations(stationCriteria)

    then:
    stations
    stations[0].name == 'Zahn Enterprise'
    stations[0].systemName == 'HIP 84255'
    stations[0].allegiance == Allegiance.FEDERATION
    stations[0].distanceFromStarLs == 1072
    stations[0].distanceFromRefLy == 6.71
  }

  void 'Find Pranav Antal station near another system'() {

    given:
    String hurukuntakId = '9608'
    StationCriteria stationCriteria = new StationCriteria(
        referenceSystemId: hurukuntakId,
        sortType: StationCriteria.SortType.DISTANCE_TO_REF,
        minPadSize: PadSize.M,
        powerType: PowerType.PRANAV_ANTAL,
        powerEffect: PowerEffect.CONTROL
    )

    when:
    List<Station> stations = eddbStationsClient.findStations(stationCriteria)

    then:
    stations
    stations[0].name == 'Acton Station'
    stations[0].systemName == 'Allowa'
    !stations[0].planetary
    stations[0].distanceFromRefLy == 135.55
  }
}
