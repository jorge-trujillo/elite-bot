package org.jorgetrujillo.elitebot.services

import org.jorgetrujillo.elitebot.domain.ServiceRequest
import org.jorgetrujillo.elitebot.domain.SystemCriteria
import org.jorgetrujillo.elitebot.domain.elite.MaterialTraderType
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
        systemsService: Mock(SystemsService),
        stationsService: Mock(StationsService),
        cacheService: Mock(ServiceRequestCacheService)
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
    List<Station> stations = [
        new Station(name: 'Alpha', id: '1', distanceFromStarLs: 10, distanceFromRefLy: 100),
        new Station(name: 'Bravo', id: '2', distanceFromStarLs: 20, distanceFromRefLy: 200),
    ]

    when:
    String response = requestProcessorService.processMessage(message)

    then:
    1 * requestProcessorService.simpleRequestService.parseRequest(message) >> serviceRequest
    1 * requestProcessorService.systemsService.getSystemByName('HIP 8561') >> refSystem
    1 * requestProcessorService.stationsService.getNearestInterstellarFactors(refId, PadSize.L) >> stations

    and: 'Cache entry is retrieved and saved'
    1 * requestProcessorService.cacheService.getEntry(serviceRequest) >> null
    1 * requestProcessorService.cacheService.saveEntry(serviceRequest, _ as String)
    0 * _

    and: 'Stations are returned'
    response =~ /Alpha/
    response =~ /Bravo/
  }

  void 'The same request twice yields a cached response'() {

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
    List<Station> stations = [
        new Station(name: 'Alpha', id: '1', distanceFromStarLs: 10, distanceFromRefLy: 100),
        new Station(name: 'Bravo', id: '2', distanceFromStarLs: 20, distanceFromRefLy: 200),
    ]

    when:
    String response = requestProcessorService.processMessage(message)

    then:
    1 * requestProcessorService.simpleRequestService.parseRequest(message) >> serviceRequest
    1 * requestProcessorService.systemsService.getSystemByName('HIP 8561') >> refSystem
    1 * requestProcessorService.stationsService.getNearestInterstellarFactors(refId, PadSize.L) >> stations

    and: 'Cache entry is retrieved and saved'
    1 * requestProcessorService.cacheService.getEntry(serviceRequest) >> null
    1 * requestProcessorService.cacheService.saveEntry(serviceRequest, _ as String)
    0 * _

    and: 'Stations are returned'
    response =~ /Alpha/
    response =~ /Bravo/
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
        new System(name: 'Alpha', id: '1', distanceFromRefLy: 100),
        new System(name: 'Bravo', id: '2', distanceFromRefLy: 200),
    ]

    when:
    String response = requestProcessorService.processMessage(message)

    then:
    1 * requestProcessorService.simpleRequestService.parseRequest(message) >> serviceRequest
    1 * requestProcessorService.systemsService.getSystemByName('HIP 8561') >> refSystem
    1 * requestProcessorService.systemsService.findSystems(serviceRequest.systemCriteria) >> systems

    and: 'Cache entry is retrieved and saved'
    1 * requestProcessorService.cacheService.getEntry(serviceRequest) >> null
    1 * requestProcessorService.cacheService.saveEntry(serviceRequest, _ as String)
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
        distanceFromRefLy: 100,
        stations: [
            new Station(name: 'Orbital', landingPad: PadSize.L, distanceFromStarLs: 100)
        ]
    )

    when:
    String response = requestProcessorService.processMessage(message)

    then:
    1 * requestProcessorService.simpleRequestService.parseRequest(message) >> serviceRequest
    1 * requestProcessorService.systemsService.getSystemByName('maya') >> responseSystem

    and: 'Cache entry is retrieved and saved'
    1 * requestProcessorService.cacheService.getEntry(serviceRequest) >> null
    1 * requestProcessorService.cacheService.saveEntry(serviceRequest, _ as String)
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
    1 * requestProcessorService.systemsService.getSystemByName('alpha', true) >> systems[0]
    1 * requestProcessorService.systemsService.getSystemByName('sol', true) >> systems[1]

    and: 'Cache entry is retrieved and saved'
    1 * requestProcessorService.cacheService.getEntry(serviceRequest) >> null
    1 * requestProcessorService.cacheService.saveEntry(serviceRequest, _ as String)
    0 * _

    and: 'Distance is returned'
    response =~ /17\.3/
  }

  void 'If one or more systems is bad, return an error for distance query'() {

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
    1 * requestProcessorService.systemsService.getSystemByName('alpha', true) >> null
    1 * requestProcessorService.systemsService.getSystemByName('sol', true) >> systems[1]

    and: 'Cache entry is retrieved and saved'
    1 * requestProcessorService.cacheService.getEntry(serviceRequest) >> null
    1 * requestProcessorService.cacheService.saveEntry(serviceRequest, _ as String)
    0 * _

    and: 'Error is returned'
    response =~ /alpha/
  }

  void 'Process request to find material traders'() {

    given:
    String message = 'find: mat trader near: HIP 8561'

    String refId = 'id'
    System refSystem = new System(name: 'HIP 8561', id: refId)

    ServiceRequest serviceRequest = new ServiceRequest(
        actionType: ServiceRequest.ActionType.FIND,
        resourceType: ServiceRequest.ResourceType.MATERIAL_TRADER,
        systemCriteria: new SystemCriteria(
            referenceSystemName: 'HIP 8561'
        )
    )

    List<Station> stations = [
        new Station(name: 'Alpha', id: '1', materialTrader: MaterialTraderType.ENCODED, distanceFromStarLs: 10, distanceFromRefLy: 100),
        new Station(name: 'Bravo', id: '2', materialTrader: MaterialTraderType.RAW, distanceFromStarLs: 20, distanceFromRefLy: 200),
    ]

    when:
    String response = requestProcessorService.processMessage(message)

    then:
    1 * requestProcessorService.systemsService.getSystemByName('HIP 8561') >> refSystem
    1 * requestProcessorService.simpleRequestService.parseRequest(message) >> serviceRequest
    1 * requestProcessorService.stationsService.getNearestMaterialTraders('HIP 8561') >> stations

    and: 'Cache entry is retrieved and saved'
    1 * requestProcessorService.cacheService.getEntry(serviceRequest) >> null
    1 * requestProcessorService.cacheService.saveEntry(serviceRequest, _ as String)

    response =~ /Alpha/
    response =~ /encoded/
    response =~ /Bravo/
    response =~ /raw/
  }
}
