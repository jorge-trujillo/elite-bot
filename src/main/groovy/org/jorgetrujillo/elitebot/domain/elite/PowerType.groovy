package org.jorgetrujillo.elitebot.domain.elite

enum PowerType {

  ARISSA_LAVIGNY_DUVAL('Arissa Lavigny-Duval'),
  AISLING_DUVAL('Aisling Duval'),
  ARCHON_DELAINE('Archon Delaine'),
  DENTON_PATREUS('Denton Patreus'),
  EDMUND_MAHON('Edmund Mahon'),
  FELICIA_WINTERS('Felicia Winters'),
  LI_YONG_RUI('Li Yong-Rui'),
  PRANAV_ANTAL('Pranav Antal'),
  ZACHARY_HUDSON('Zachary Hudson'),
  ZEMINA_TORVAL('Zemina Torval'),
  YURI_GROM('Yuri Grom')

  String powerName

  PowerType(String powerName) {
    this.powerName = powerName
  }
}
