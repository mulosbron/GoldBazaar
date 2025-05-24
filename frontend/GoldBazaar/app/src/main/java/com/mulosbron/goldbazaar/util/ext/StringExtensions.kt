package com.mulosbron.goldbazaar.util.ext

fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}                     // Büyük-küçük farkını da sil

