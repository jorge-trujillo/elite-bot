package org.jorgetrujillo.elitebot.exceptions

class LanguageRequestParseException extends RuntimeException {

  String field

  LanguageRequestParseException(String field) {
    super()
    this.field = field
  }
  LanguageRequestParseException(String message, String field) {
    super(message)
    this.field = field
  }

  LanguageRequestParseException(Throwable cause, String field) {
    super(cause)
    this.field = field
  }
  LanguageRequestParseException(String message, Throwable cause, String field) {
    super(message, cause)
    this.field = field
  }
}
