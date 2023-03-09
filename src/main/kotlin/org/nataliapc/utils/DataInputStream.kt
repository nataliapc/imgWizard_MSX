@file:JvmName("DataInputStreamKt")
package org.nataliapc.utils

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

fun DataInputStream.readUnsignedIntLE(): Long
{
    return readUnsignedShortLE().toLong() or (readUnsignedShortLE().toLong() shl 16)
}