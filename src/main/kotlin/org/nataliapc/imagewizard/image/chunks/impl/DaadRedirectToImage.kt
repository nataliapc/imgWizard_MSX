package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.image.chunks.ChunkCompanion
import java.io.DataInputStream
import java.lang.RuntimeException


/*
    Chunk DAAD Redirect format:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type: (0:redirect)
        0x0001  2    Extra header length (1)
        0x0003  2    Data length (0)
        --extra header--
        0x0005  1    New image location to read
 */
class DaadRedirectToImage private constructor() : ChunkAbstractImpl(0)
{
    var location: Int = 0
        set(value) {
            if (value !in 0..0xff) {
                throw RuntimeException("Location must be in range 0..255")
            }
            field = value
        }

    constructor(location: Int) : this() {
        this.location = location
    }

    companion object : ChunkCompanion {
        override fun from(stream: DataInputStream): DaadRedirectToImage {
            val obj = DaadRedirectToImage()
            obj.readChunk(stream)
            return obj
        }
    }

    override fun readExtraHeader(stream: DataInputStream) {
        location = stream.readUnsignedByte()
    }

    override fun ensembleExtraHeader(): ByteArray {
        return byteArrayOf(location.toByte())
    }

    override fun getInfo(): Array<String> {
        return arrayOf("DAAD Redirect Image (to location: $location)")
    }
}