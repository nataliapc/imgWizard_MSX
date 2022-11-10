package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.image.chunks.ChunkCompanion
import org.nataliapc.imagewizard.utils.DataByteArrayOutputStream
import org.nataliapc.imagewizard.utils.readUnsignedShortLE
import org.nataliapc.imagewizard.utils.writeShortLE
import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.RuntimeException

/*
    Chunk SkipBytes format:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type: (18:SkipBytes)
        0x0001  2    Extra header length (2)
        0x0003  2    Data length (0)
        --extra header--
        0x0005  2    VRAM Bytes to skip
 */
class DaadSkipBytes private constructor() : ChunkAbstractImpl(18)
{
    var skipBytes: Int = 0
        set(value) {
            if (value !in 0..0xffff) {
                throw RuntimeException("Bytes to skip must be in range 0..65535")
            }
            field = value
        }

    constructor(skipBytes: Int): this() {
        this.skipBytes = skipBytes
    }

    companion object : ChunkCompanion {
        override fun from(stream: DataInputStream): DaadSkipBytes {
            val obj = DaadSkipBytes()
            obj.readChunk(stream)
            return obj
        }
    }

    override fun readExtraHeader(stream: DataInputStream) {
        skipBytes = stream.readUnsignedShortLE()
    }

    override fun ensembleExtraHeader(): ByteArray {
        val out = DataByteArrayOutputStream()
        out.use {
            it.writeShortLE(skipBytes)
        }
        return out.toByteArray()
    }

    override fun getInfo(): Array<String> {
        return arrayOf("DAAD Skip Bytes ($skipBytes bytes)")
    }
}

