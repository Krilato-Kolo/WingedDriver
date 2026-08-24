package com.krilatokolo.wingeddriver.common.normalizer

/** Replace some characters with their english counterpart, for easier searching. Only works on lower case characters. **/
fun String.normalize(): String {
   return replace('č', 'c')
      .replace('ć', 'c')
      .replace('ž', 'z')
      .replace('š', 's')
      .replace('ö', 'o')
      .replace('ä', 'a')
      .replace('ü', 'u')
      .replace('ß', 's')
      .replace('đ', 'd')
}
