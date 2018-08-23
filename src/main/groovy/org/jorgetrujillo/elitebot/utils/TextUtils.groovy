package org.jorgetrujillo.elitebot.utils

import org.apache.commons.text.similarity.LevenshteinDistance

class TextUtils {

  private static final LevenshteinDistance LEVENSHTEIN_DISTANCE = LevenshteinDistance.getDefaultInstance()

  static int getLevenshteinDistance(String first, String second) {
    return LEVENSHTEIN_DISTANCE.apply(first, second)
  }
}
