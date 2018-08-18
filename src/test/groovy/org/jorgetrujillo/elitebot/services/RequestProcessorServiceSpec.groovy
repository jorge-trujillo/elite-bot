package org.jorgetrujillo.elitebot.services

import org.jorgetrujillo.elitebot.domain.ServiceRequest
import org.jorgetrujillo.elitebot.domain.SystemCriteria
import org.jorgetrujillo.elitebot.domain.elite.PadSize
import org.jorgetrujillo.elitebot.domain.elite.PowerType
import org.jorgetrujillo.elitebot.domain.elite.Station
import org.jorgetrujillo.elitebot.domain.elite.System
import spock.lang.Specification

class RequestProcessorServiceSpec extends Specification {

  RequestProcessorService requestProcessorService

  void setup() {
    requestProcessorService = new RequestProcessorService(
        simpleRequestService: Mock(SimpleRequestService),
        systemsService: Mock(SystemsService)
    )
  }

  void 'Process a request to find interstellar factors'() {

    given:
    String message = 'find: interstellar factors near: HIP 8561'
    ServiceRequest serviceRequest = new ServiceRequest(
        actionType: ServiceRequest.ActionType.FIND,
        resourceType: ServiceRequest.ResourceType.INTERSTELLAR_FACTORS,
        systemCriteria: new SystemCriteria(
            referenceSystemName: 'HIP 8561',
            minPadSize: PadSize.L
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
    1 * requestProcessorService.systemsService.getNearestInterstellarFactors(refId, PadSize.L) >> systems
    0 * _

    and: 'System is returned'
    response =~ /Alpha/
  }

  void 'Process a request to find a system'() {

    given:
    String message = 'find: system near: HIP 8561 pad: L power: aisling'
    ServiceRequest serviceRequest = new ServiceRequest(
        actionType: ServiceRequest.ActionType.FIND,
        resourceType: ServiceRequest.ResourceType.SYSTEM,
        systemCriteria: new SystemCriteria(
            referenceSystemName: 'HIP 8561',
            minPadSize: PadSize.L,
            powerType: PowerType.AISLING_DUVAL
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
    1 * requestProcessorService.systemsService.findSystems(serviceRequest.systemCriteria) >> systems
    0 * _

    and: 'System is returned'
    response =~ /Alpha/
  }

  void 'Query for a system by name'() {

    given:
    String message = 'system: maya'
    ServiceRequest serviceRequest = new ServiceRequest(
        actionType: ServiceRequest.ActionType.SYSTEM_DETAILS,
        resourceType: ServiceRequest.ResourceType.SYSTEM,
        systemCriteria: new SystemCriteria(
            referenceSystemName: 'maya'
        )
    )

    System responseSystem = new System(
        name: 'Maya',
        id: '1',
        distanceFromRef: '100 ly',
        stations: [
            new Station(name: 'Orbital', landingPad: PadSize.L, distanceFromStarLs: 100)
        ]
    )

    when:
    String response = requestProcessorService.processMessage(message)

    then:
    1 * requestProcessorService.simpleRequestService.parseRequest(message) >> serviceRequest
    1 * requestProcessorService.systemsService.getSystemByName('maya') >> responseSystem
    0 * _

    and: 'System is returned'
    response =~ /Maya/
    response =~ /Orbital/
  }

  void 'Get the distance between 2 systems'() {

    given:
    String message = 'distance: alpha to: sol'
    ServiceRequest serviceRequest = new ServiceRequest(
        actionType: ServiceRequest.ActionType.COMPUTE_DISTANCE,
        systemPair: new Tuple2<String, String>('alpha', 'sol')
    )
    List<System> systems = [
        new System(name: 'Alpha', x: 10, y: 10, z: 10),
        new System(name: 'Sol', x: 0, y: 0, z: 0)
    ]

    when:
    String response = requestProcessorService.processMessage(message)

    then:
    1 * requestProcessorService.simpleRequestService.parseRequest(message) >> serviceRequest
    1 * requestProcessorService.systemsService.getSystemByName('alpha') >> systems[0]
    1 * requestProcessorService.systemsService.getSystemByName('sol') >> systems[1]
    0 * _

    and: 'Distance is returned'
    response =~ /17\.3/
  }
}
