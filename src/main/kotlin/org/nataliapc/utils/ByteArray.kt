package org.nataliapc.utils


fun ByteArray.toCharString(): String {
    return joinToString("") {
        it.toInt().toChar().toString()
    }
}