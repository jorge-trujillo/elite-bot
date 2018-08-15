package org.jorgetrujillo.elitebot.clients

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
@Slf4j
class GenericClient {

  @Autowired
  RestTemplate restTemplate

  @Value('${clients.retries:3}')
  int retries

  @SuppressWarnings('AbcMetric')
  <S, T> GenericResponse<T> getHttpResponse(GenericRequest<S, T> genericRequest) {

    GenericResponse<T> clientResponse = new GenericResponse<>()
    boolean success = false
    int tryCount = 1

    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(genericRequest.url)
    genericRequest.parameters?.each { String key, Object value ->
      builder.queryParam(key, value)
    }

    RequestEntity<S> requestEntity = createRequestEntity(genericRequest, builder.build().encode().toUri())
    ResponseEntity<T> response

    while (!success) {

      try {
        if (genericRequest.typeReference) {
          response = restTemplate.exchange(
              requestEntity,
              genericRequest.typeReference
          )
        } else {
          response = restTemplate.exchange(
              requestEntity,
              genericRequest.responseType
          )
        }
        clientResponse.result = response.body
        clientResponse.statusCode = response.statusCode
        success = true
      }
      catch (HttpClientErrorException e) {
        log.warn("${requestEntity.getMethod()} ${requestEntity.getUrl().toString()}: " +
            "failed with ${e.statusCode} ${e.statusCode.reasonPhrase} ${e.responseBodyAsString}")
        clientResponse.statusCode = e.statusCode

        if (e.statusCode == HttpStatus.NOT_FOUND) {
          success = true
        } else if (e.statusCode.is4xxClientError()) {
          success = true
        } else if (!e.statusCode?.is5xxServerError() || (tryCount >= retries)) {
          success = true
        }
      }
      catch (Exception e) {
        log.warn("Unexpected exception sending request to ${genericRequest.url} on try ${tryCount}. ${e.message}")
        if (tryCount >= retries) {
          log.error("${requestEntity.getMethod()} ${requestEntity.getUrl().toString()}: " +
              'request failed for unexpected reason', e)
          clientResponse.statusCode = HttpStatus.INTERNAL_SERVER_ERROR
          success = true
        }
      }

      tryCount++

      // Terminal failure, so print out the result
      if (response && !response.statusCode?.is2xxSuccessful()) {
        log.warn("${requestEntity.getMethod()} ${requestEntity.getUrl().toString()}: " +
            "failed with ${response.statusCode} ${response.body}")
      }
    }

    return clientResponse
  }

  private static <S, T> RequestEntity<S> createRequestEntity(GenericRequest<S, T> genericRequest, URI uri) {
    RequestEntity<S> requestEntity

    if (genericRequest.body) {
      requestEntity = new RequestEntity<>(
          genericRequest.body,
          genericRequest.httpHeaders,
          genericRequest.method,
          uri
      )
    } else {
      requestEntity = new RequestEntity<>(
          genericRequest.httpHeaders,
          genericRequest.method,
          uri
      )
    }

    return requestEntity
  }
}
