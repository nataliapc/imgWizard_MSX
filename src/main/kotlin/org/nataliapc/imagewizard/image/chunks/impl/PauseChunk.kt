package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.image.chunks.ChunkCompanion
import org.nataliapc.imagewizard.utils.DataByteArrayOutputStream
import org.nataliapc.imagewizard.utils.readUnsignedShortLE
import org.nataliapc.imagewizard.utils.writeShortLE
import java.io.DataInputStream


/*
    Chunk Pause format:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type: (19:Pause)
        0x0001  2    Chunk data length (0x0000)
        0x0003  2    Time to wait in 1/50 sec units
 */
class PauseChunk(ticks: Int) : ChunkAbstractImpl(19)
{
    override var auxData: Int = ticks

    companion object : ChunkCompanion {
        override fun from(stream: DataInputStream): PauseChunk {
            val id = stream.readUnsignedByte()
            stream.readUnsignedShortLE()                    // Skip length
            val aux = stream.readUnsignedShortLE()

            val obj = PauseChunk(aux)
            obj.checkId(id)
            return obj
        }
    }

    fun getTicks(): Int {
        return auxData
    }

    fun setTicks(value: Int) {
        auxData = value
    }

    override fun build(): ByteArray
    {
        val header = buildHeader()
        val out = DataByteArrayOutputStream()

        out.write(header)
        out.writeShortLE(0)
        out.writeShortLE(auxData)

        return out.toByteArray()
    }

    override fun getInfo(): Array<String> {
        return arrayOf("Pause Chunk: $auxData ticks (~${"%.1f".format(auxData/50.0)} sec)")
    }
}