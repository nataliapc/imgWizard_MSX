package org.nataliapc.imagewizard.compressor

import java.lang.RuntimeException

interface Compressor
{
    val id: Int
    fun compress(data: ByteArray): ByteArray
    fun uncompress(data: ByteArray): ByteArray

    enum class Types(val instance: Compressor) {
        RAW(Raw()),
        RLE(Rle()),
        PLETTER(Pletter());

        companion object {
            fun compressorById(id: Int): Compressor {
                values().forEach {
                    if (it.instance.id == id) {
                        return it.instance
                    }
                }
                throw RuntimeException("Unknown compressor ID $id")
            }
        }
    }
}