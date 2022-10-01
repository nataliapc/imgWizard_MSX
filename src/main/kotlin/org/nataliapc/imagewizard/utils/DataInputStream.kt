@file:JvmName("DataInputStreamKt")
package org.nataliapc.imagewizard.utils

import java.io.DataInputStream


fun DataInputStream.readShortLE(): Short
{
    return readUnsignedShortLE().toShort()
}

fun DataInputStream.readUnsignedShortLE(): Int
{
    val aux = readUnsignedShort()
    return ((aux shr 8) or ((aux and 0xff) shl 8))
}
