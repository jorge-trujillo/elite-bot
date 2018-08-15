package org.jorgetrujillo.elitebot.clients

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod

class GenericRequest <S, T> {

  HttpMethod method
  String url
  S body
  HttpHeaders httpHeaders
  Map<String, String> parameters
  Class<T> responseType
  ParameterizedTypeReference<T> typeReference

}
