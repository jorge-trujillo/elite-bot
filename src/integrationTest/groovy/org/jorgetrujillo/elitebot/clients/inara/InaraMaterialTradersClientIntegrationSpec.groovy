package org.jorgetrujillo.elitebot.clients.inara

import org.jorgetrujillo.elitebot.IntegrationTestBase
import org.jorgetrujillo.elitebot.clients.inara.domain.MaterialTraderResult
import org.jorgetrujillo.elitebot.domain.elite.MaterialTraderType
import org.springframework.beans.factory.annotation.Autowired

class InaraMaterialTradersClientIntegrationSpec extends IntegrationTestBase {

  @Autowired
  InaraMaterialTradersClient materialTradersClient

  void 'Get material traders near system'() {
    given:
    String system = 'hurukuntak'

    when:
    List<MaterialTraderResult> matTraders = materialTradersClient.getMaterialTradersNear(system)

    then:
    matTraders.count { it.materialTraderType } >= 10

    matTraders[0].systemName == 'Hurukuntak'
    matTraders[0].stationName == 'Bauschinger Terminal'
    matTraders[0].materialTraderType == MaterialTraderType.ENCODED
    matTraders[0].distanceFromRefSystemLy == 0
    matTraders[0].distanceFromStarLs > 15 && matTraders[0].distanceFromStarLs < 20

    matTraders[1].systemName == 'Paraudika'
    matTraders[1].stationName == 'Tem Station'
    matTraders[1].materialTraderType == MaterialTraderType.MANUFACTURED
    matTraders[1].distanceFromRefSystemLy == 11.70
    matTraders[1].distanceFromStarLs == 8

    matTraders[2].systemName == 'HIP 80221'
    matTraders[2].stationName == 'Griggs City'
    matTraders[2].materialTraderType == MaterialTraderType.MANUFACTURED
    matTraders[2].distanceFromRefSystemLy == 19.43
    matTraders[2].distanceFromStarLs > 920 && matTraders[2].distanceFromStarLs < 980

  }
}
