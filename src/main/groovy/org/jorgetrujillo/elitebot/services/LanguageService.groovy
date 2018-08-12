package org.jorgetrujillo.elitebot.services

import org.springframework.stereotype.Service

@Service
class LanguageService {

  public static final String GREETING_RESPONSE = "Hi there! I'm still not able to do much, but soon... soon..."
  public static final String DEFAULT_RESPONSE = "Yeah... no. No idea what you're asking, I only speak binary right now"

  String processMessage(String message) {

    List<String> tokens = message.split('[\\s]+')

    if (message.find(/(?i)where is)/) && message.find(/(?i)system)/))  {
      println 'help'
    }
  }

}
