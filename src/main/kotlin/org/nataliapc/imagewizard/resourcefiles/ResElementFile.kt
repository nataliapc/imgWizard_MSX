package org.nataliapc.imagewizard.resourcefiles

import java.io.File


class ResElementFile(private val file: File): ResElement
{
    override fun getName(): String
    {
        return file.name
    }

    override fun getContent(): ByteArray
    {
        return file.inputStream().use {
            it.readAllBytes()
        }
    }

    override fun getSize(): Int
    {
        return file.length().toInt()
    }

}