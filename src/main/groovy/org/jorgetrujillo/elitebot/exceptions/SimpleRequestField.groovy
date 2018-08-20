package org.jorgetrujillo.elitebot.exceptions

enum SimpleRequestField {
  RESOURCE_TYPE('After **find:** I expect to see a resource type, like **system** or **interstellar factors**'),
  REFERENCE_SYSTEM( 'If using **find:**, you need to provide a reference system. For example: **near**: *sol*'),
  DETAILS_LOCATION('When using **system:**, provide a system name after. For example: **system**: *sol*'),
  DISTANCE_LOCATION('To find the distance between two points, provide two system names. For instance:\n' +
      '**distance:** sol **to:** maya'),
  PAD_SIZE('If you want to filter on pad size, add your pad size like so: **pad:** L'),
  ALLEGIANCE('To filter on allegiance, provide a valid allegiance after **allegiance:**, ' +
      'like **Empire**, **Independent** or **Federation**'),
  SECURITY_LEVEL('For the **security:** parameter, provide a value like **low** or **high**'),
  POWER('To find a system or station with a certain power, add a search filter like this:\n' +
      ' - **power**: *Torval* **C**   *The letter afterward can be used to search for *C*ontrol or *E*xploited')

  String helpText

  SimpleRequestField(String helpText) {
    this.helpText = helpText
  }
}
