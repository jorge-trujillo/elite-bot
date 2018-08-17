package org.jorgetrujillo.elitebot.services

import org.jorgetrujillo.elitebot.domain.ServiceRequest
import org.jorgetrujillo.elitebot.domain.SystemCriteria
import org.jorgetrujillo.elitebot.domain.elite.System
import spock.lang.Specification
import spock.lang.Unroll

class RequestProcessorServiceSpec extends Specification {

  RequestProcessorService requestProcessorService

  void setup() {
    requestProcessorService = new RequestProcessorService(
        simpleRequestService: Mock(SimpleRequestService),
        systemsService: Mock(SystemsService)
    )
  }

  @Unroll
  void 'Process a request to find interstellar factors'() {

    given:
    String message = 'find the nearest interstellar factors to HIP 8561'
    ServiceRequest serviceRequest = new ServiceRequest(
        actionType: ServiceRequest.ActionType.FIND,
        resourceType: ServiceRequest.ResourceType.INTERSTELLAR_FACTORS,
        systemCriteria: new SystemCriteria(
            referenceSystemName: 'HIP 8561'
        )
    )

    String refId = 'id'
    System refSystem = new System(name: 'HIP 8561', id: refId)
    List<System> systems = [
        new System(name: 'Alpha', id: '1', distanceFromRef: '100 ly'),
        new System(name: 'Bravo', id: '2', distanceFromRef: '200 ly'),
    ]

    when:
    String response = requestProcessorService.processMessage(message)

    then:
    1 * requestProcessorService.simpleRequestService.parseRequest(message) >> serviceRequest
    1 * requestProcessorService.systemsService.getSystemByName('HIP 8561') >> refSystem
    1 * requestProcessorService.systemsService.getNearestInterstellarFactors(refId) >> systems
    0 * _

    and: 'System is returned'
    response =~ /Alpha/
  }
}
