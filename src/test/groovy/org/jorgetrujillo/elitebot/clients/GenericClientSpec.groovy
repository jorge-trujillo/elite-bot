package org.jorgetrujillo.elitebot.clients

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.Charset

import static org.springframework.http.HttpMethod.GET
import static org.springframework.http.HttpMethod.PUT

class GenericClientSpec extends Specification {

  GenericClient genericClient

  int retries = 3

  void setup() {

    genericClient = new GenericClient(
        retries: retries,
        restTemplate: Mock(RestTemplate)
    )
  }

  class RequestExample {
    String name
    String value
  }

  class RequestResponse {
    String response
  }

  @Unroll
  void 'getHttpResponse returns correctly with a response of #responseStatus'() {

    given:
    String url = 'http://host.com/request_id'
    HttpHeaders httpHeaders = new HttpHeaders()
    httpHeaders.add('test', 'test_value')

    GenericRequest<RequestExample, RequestResponse> genericRequest = new GenericRequest<>(
        method: method,
        body: body,
        url: url,
        httpHeaders: httpHeaders,
        responseType: RequestResponse
    )

    when:
    GenericResponse<RequestResponse> clientResponse = genericClient.getHttpResponse(genericRequest)

    then:
    attempts * genericClient.restTemplate.exchange({ RequestEntity requestEntity ->
      assert requestEntity.method == method
      assert requestEntity.body == body
      assert requestEntity.headers == httpHeaders
      assert requestEntity.url.toString() == url
      return true
    } as RequestEntity,
        RequestResponse) >> {
      if (responseStatus.is2xxSuccessful()) {
        return new ResponseEntity(responseStatus)
      } else if (responseStatus.is4xxClientError()) {
        throw new HttpClientErrorException(
            responseStatus,
            'Error',
            '''
{
  "message": "Bad request",
  "errors": ["test error"]
 }
'''.getBytes('UTF-8'),
            Charset.forName('UTF-8'))
      } else {
        throw new UnknownHostException()
      }
    }
    clientResponse.statusCode == responseStatus
    0 * _

    where:
    method | body                                  | responseStatus                   | attempts
    GET    | null                                  | HttpStatus.NOT_FOUND             | 1
    GET    | null                                  | HttpStatus.NOT_FOUND             | 1
    GET    | null                                  | HttpStatus.BAD_REQUEST           | 1
    GET    | null                                  | HttpStatus.INTERNAL_SERVER_ERROR | 3
    GET    | null                                  | HttpStatus.INTERNAL_SERVER_ERROR | 3
    GET    | null                                  | HttpStatus.OK                    | 1
    PUT    | new RequestResponse(response: 'test') | HttpStatus.OK                    | 1

  }

}
