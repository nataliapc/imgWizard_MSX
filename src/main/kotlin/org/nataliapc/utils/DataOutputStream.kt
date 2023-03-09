@file:JvmName("DataOutputStreamKt")
package org.nataliapc.utils

import java.io.DataOutputStream


fun DataOutputStream.writeByte(v: Short)
{
    writeByte(v.toInt())
}

fun DataOutputStream.writeShortLE(v: Short)
{
    writeShortLE(v.toInt())
}

fun DataOutputStream.writeShortLE(v: UShort)
{
    writeShortLE(v.toInt())
}

fun DataOutputStream.writeShortLE(v: Int)
{
    writeByte(v and 0xff)
    writeByte(v shr 8 and 0xff)
}

fun DataOutputStream.writeIntLE(v: UInt) {
    writeIntLE(v.toLong())
}

fun DataOutputStream.writeIntLE(v: Long) {
    writeShortLE((v and 0xffff).toInt())
    writeShortLE((v shr 16).toInt())
}
