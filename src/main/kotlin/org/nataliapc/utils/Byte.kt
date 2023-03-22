package org.nataliapc.utils


fun Byte.nibbleHigh(): Int = this.toUByte().toInt().shr(4)
fun Byte.nibbleLow(): Int = this.toUByte().toInt().and(0xf)
