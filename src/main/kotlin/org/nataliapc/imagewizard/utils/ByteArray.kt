package org.nataliapc.imagewizard.utils


fun ByteArray.toCharString(): String {
    return joinToString("") {
        it.toInt().toChar().toString()
    }
}