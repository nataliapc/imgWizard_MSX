package org.nataliapc.imagewizard.resourcefiles.interfaces

import org.nataliapc.imagewizard.compressor.Compressor


interface ResElement
{
    fun getName(): String
    fun getContent(): ByteArray
    fun getSize(): Int
    fun getCompressedSize(): Int
    fun getCompressor(): Compressor
    fun getCRC(): Long
}