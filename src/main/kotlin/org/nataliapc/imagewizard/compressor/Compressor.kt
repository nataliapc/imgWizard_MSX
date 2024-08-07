package org.nataliapc.imagewizard.compressor

import java.lang.RuntimeException

interface Compressor
{
    val id: Int
    fun compress(data: ByteArray): ByteArray
    fun uncompress(data: ByteArray): ByteArray

    companion object {
        const val MAX_SIZE_UNCOMPRESSED = 48 * 1024
    }

    fun getName(): String {
        return this::class.java.simpleName
    }

    enum class Types(val instance: Compressor) {
        RAW(Raw()),
        RLE(Rle()),
        PLETTER(Pletter()),
        PLETTEREXT(PletterExtern()),
        ZX7EXT(Zx7MiniExtern()),
        ZX0EXT(Zx0Extern());

        companion object {
            fun byId(id: Int): Compressor {
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

abstract class CompressorImpl(override val id: Int): Compressor
