package org.nataliapc.imagewizard.utils

fun ByteArray.toHex(separator: String = " "): String = joinToString(separator) { eachByte -> "%02x".format(eachByte) }
fun IntArray.toHex(separator: String = " "): String = joinToString(separator) { eachByte -> "%06x".format(eachByte) }
