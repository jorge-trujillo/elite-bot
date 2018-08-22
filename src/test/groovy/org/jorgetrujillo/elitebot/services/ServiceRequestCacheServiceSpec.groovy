package org.jorgetrujillo.elitebot.services

import org.jorgetrujillo.elitebot.domain.ServiceRequest
import spock.lang.Specification

class ServiceRequestCacheServiceSpec extends Specification {

  ServiceRequestCacheService serviceRequestCacheService

  void setup() {
    serviceRequestCacheService = new ServiceRequestCacheService()
  }

  void 'Service requests equality works'() {

    given:
    ServiceRequest serviceRequest = new ServiceRequest(
        actionType: ServiceRequest.ActionType.FIND,
        resourceType: ServiceRequest.ResourceType.INTERSTELLAR_FACTORS
    )

    expect:
    serviceRequest == new ServiceRequest(
        actionType: ServiceRequest.ActionType.FIND,
        resourceType: ServiceRequest.ResourceType.INTERSTELLAR_FACTORS
    )
    serviceRequest != new ServiceRequest(
        actionType: ServiceRequest.ActionType.SYSTEM_DETAILS
    )
  }

  void 'Get an entry'() {
    given:
    ServiceRequest serviceRequest = new ServiceRequest(
        actionType: ServiceRequest.ActionType.FIND
    )
    String expected = 'response'
    serviceRequestCacheService.saveEntry(serviceRequest, expected)

    when:
    String actual = serviceRequestCacheService.getEntry(serviceRequest)

    then:
    0 * _

    actual.is(expected)
  }

}
