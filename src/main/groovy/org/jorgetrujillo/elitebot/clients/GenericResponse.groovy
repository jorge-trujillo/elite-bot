package org.jorgetrujillo.elitebot.clients

import org.springframework.http.HttpStatus

class GenericResponse<T> {

  T result
  HttpStatus statusCode
}
