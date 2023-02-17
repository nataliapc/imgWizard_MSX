package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.image.chunks.ChunkCreateFrom
import org.nataliapc.utils.DataByteArrayOutputStream
import org.nataliapc.utils.readUnsignedShortLE
import org.nataliapc.utils.writeShortLE
import java.io.DataInputStream
import java.lang.RuntimeException


/*
    Chunk Pause format:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type: (19:Pause)
        0x0001  2    Extra header length (2)
        0x0003  2    Data length (0)
        --extra header--
        0x0005  2    Pause ticks
 */
class PauseChunk private constructor() : ChunkAbstractImpl(19)
{
    var pauseTicks: Int = 0
        set(value) {
            if (value !in 0..0xffff) {
                throw RuntimeException("Pause ticks must be in range 0..65535")
            }
            field = value
        }

    constructor(pauseTicks: Int) : this() {
        this.pauseTicks = pauseTicks
    }

    companion object : ChunkCreateFrom {
        override fun from(stream: DataInputStream): PauseChunk {
            val obj = PauseChunk()
            obj.readChunk(stream)
            return obj
        }
    }

    override fun ensembleExtraHeader(): ByteArray
    {
        val out = DataByteArrayOutputStream()
        out.use {
            it.writeShortLE(pauseTicks)
        }
        return out.toByteArray()
    }

    override fun readExtraHeader(stream: DataInputStream) {
        pauseTicks = stream.readUnsignedShortLE()
    }

    override fun getInfo(): Array<String> {
        return arrayOf("Pause Chunk: $pauseTicks ticks (~${"%.1f".format(pauseTicks/50.0)} sec)")
    }
}