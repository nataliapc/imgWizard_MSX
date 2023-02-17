package org.nataliapc.utils

import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.InputStream


open class DataByteArrayInputStream(private val stream: InputStream) : DataInputStream(stream)
{
    constructor(byteArray: ByteArray) : this(ByteArrayInputStream(byteArray))
}