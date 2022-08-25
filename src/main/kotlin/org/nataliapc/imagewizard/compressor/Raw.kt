package org.nataliapc.imagewizard.compressor

class Raw : CompressorImpl(0)
{
    override fun compress(data: ByteArray): ByteArray {
        return data
    }

    override fun uncompress(data: ByteArray): ByteArray {
        return data
    }
}