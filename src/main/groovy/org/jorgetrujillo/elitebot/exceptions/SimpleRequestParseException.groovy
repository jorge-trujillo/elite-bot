package org.jorgetrujillo.elitebot.exceptions

class SimpleRequestParseException extends RuntimeException {

  SimpleRequestField field

  SimpleRequestParseException(SimpleRequestField field) {
    super()
    this.field = field
  }

  SimpleRequestParseException(String message, SimpleRequestField field) {
    super(message)
    this.field = field
  }

  SimpleRequestParseException(Throwable cause, SimpleRequestField field) {
    super(cause)
    this.field = field
  }

  SimpleRequestParseException(String message, Throwable cause, SimpleRequestField field) {
    super(message, cause)
    this.field = field
  }
}
