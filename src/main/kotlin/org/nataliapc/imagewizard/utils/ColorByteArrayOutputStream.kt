package org.nataliapc.imagewizard.utils

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream


class ColorsOutputStream(private val baos: ByteArrayOutputStream = ByteArrayOutputStream()) : DataOutputStream(baos)
{
    override fun close() {
        super.close()
        baos.close()
    }

    fun toByteArray(): ByteArray {
        return baos.toByteArray()
    }
}