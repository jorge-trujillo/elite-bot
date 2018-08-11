package org.jorgetrujillo.elitebot.services

import org.springframework.stereotype.Service

@Service
class LanguageService {

  public static final String GREETING_RESPONSE = "Hi there! I'm still not able to do much, but soon... soon..."
  public static final String DEFAULT_RESPONSE = "Yeah... no. No idea what you're asking, I only speak binary right now"

  String processMessage(String message) {

    String response
    if (message =~ /(?i)hi/) {
      response = GREETING_RESPONSE
    }
    else {
      response = DEFAULT_RESPONSE
    }

    return response
  }
}
