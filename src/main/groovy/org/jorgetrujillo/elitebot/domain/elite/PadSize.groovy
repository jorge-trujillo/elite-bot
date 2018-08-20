package org.jorgetrujillo.elitebot.domain.elite

enum PadSize {
  S('small'),
  M('medium'),
  L('large')

  String name

  PadSize(String name) {
    this.name = name
  }
}
