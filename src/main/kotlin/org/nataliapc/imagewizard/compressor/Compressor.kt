package org.nataliapc.imagewizard.compressor

interface Compressor
{
    val id: Int
    fun compress(data: ByteArray): ByteArray
}