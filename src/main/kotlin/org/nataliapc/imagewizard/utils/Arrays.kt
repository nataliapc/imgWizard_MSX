package org.nataliapc.imagewizard.utils

fun ByteArray.toHex(): String = joinToString(separator = " ") { eachByte -> "%02x".format(eachByte) }
fun IntArray.toHex(): String = joinToString(separator = " ") { eachByte -> "%06x".format(eachByte) }
