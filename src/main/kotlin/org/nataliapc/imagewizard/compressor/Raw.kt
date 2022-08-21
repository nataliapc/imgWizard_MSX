package org.nataliapc.imagewizard.compressor

class Raw : Compressor
{
    override val id = 0

    override fun compress(data: ByteArray): ByteArray {
        return data
    }
}