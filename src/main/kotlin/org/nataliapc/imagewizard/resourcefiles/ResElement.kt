package org.nataliapc.imagewizard.resourcefiles

interface ResElement
{
    fun getName(): String
    fun getContent(): ByteArray
    fun getSize(): Int
}