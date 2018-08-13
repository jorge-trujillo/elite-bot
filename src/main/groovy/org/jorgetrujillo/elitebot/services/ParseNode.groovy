package org.jorgetrujillo.elitebot.services

class ParseNode {

  List<String> phrases
  List<ParseNode> parseNodeChildren
  Closure<String> action
}
