package org.jorgetrujillo.elitebot.services

@SuppressWarnings('FieldName')
enum PartOfSpeech {

  CC('Coordinating conjunction'),
  CD('Cardinal number'),
  DT('Determiner'),
  EX('Existential there'),
  FW('Foreign word'),
  IN('Preposition or subordinating conjunction'),
  JJ('Adjective'),
  JJR('Adjective, comparative'),
  JJS('Adjective, superlative'),
  LS('List item marker'),
  MD('Modal'),
  NN('Noun, singular or mass'),
  NNS(' Noun, plural'),
  NNP('Proper noun, singular'),
  NNP$('Proper noun, plural'),
  PDT('Predeterminer'),
  POS('Possessive ending'),
  PRP('Personal pronoun'),
  PRP$('Possessive pronoun'),
  RB('Adverb'),
  RBR('Adverb, comparative'),
  RBS('Adverb, superlative'),
  RP('Particle'),
  SYM('Symbol'),
  TO('to'),
  UH('Interjection'),
  VB('Verb, base form'),
  VBD('Verb, past tense'),
  VBG('Verb, gerund or present participle'),
  VBN('Verb, past participle'),
  VBP('Verb, non-3rd person singular present'),
  VBZ('Verb, 3rd person singular present'),
  WDT('Wh-determiner'),
  WP('Wh-pronoun'),
  WP$('Possessive wh-pronoun'),
  WRB('Wh-adverb'),
  UNKNOWN(null)

  String name

  PartOfSpeech(String name) {
    this.name = name
  }

  static List<PartOfSpeech> getVerbs() {
    return [VB, VBD, VBG, VBN, VBP, VBZ]
  }

  static List<PartOfSpeech> getNouns() {
    return [NN, NNS, NNP, NNP$]
  }

  static PartOfSpeech of(String tag) {
    try {
      return valueOf(tag.toUpperCase())
    }
    catch (IllegalArgumentException e) {
      return UNKNOWN
    }
  }

}
