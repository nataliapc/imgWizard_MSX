package org.nataliapc.imagewizard.utils

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream


open class DataByteArrayOutputStream(private val baos: ByteArrayOutputStream = ByteArrayOutputStream()) : DataOutputStream(baos)
{
    override fun close() {
        super.close()
        baos.close()
    }

    fun toByteArray(): ByteArray {
        return baos.toByteArray()
    }
}