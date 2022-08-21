package org.nataliapc.imagewizard.utils

import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.InputStream


open class DataByteArrayInputStream(private val stream: InputStream) : DataInputStream(stream)
{
    constructor(byteArray: ByteArray) : this(ByteArrayInputStream(byteArray))

    override fun close() {
        super.close()
        stream.close()
    }
}