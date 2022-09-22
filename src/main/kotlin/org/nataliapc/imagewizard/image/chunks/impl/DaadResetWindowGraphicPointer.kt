package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.image.chunks.ChunkCompanion
import org.nataliapc.imagewizard.utils.DataByteArrayOutputStream
import org.nataliapc.imagewizard.utils.readUnsignedShortLE
import org.nataliapc.imagewizard.utils.writeShortLE
import java.io.DataInputStream


/*
    Chunk DAAD Reset Window Graphic Pointer format:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type: (16:ResetPointer)
        0x0001  2    Chunk data length (0x0000)
        0x0003  2    Empty chunk header (0x0000)
 */
class DaadResetWindowGraphicPointer() : ChunkAbstractImpl(16)
{
    companion object : ChunkCompanion {
        override fun from(stream: DataInputStream): DaadResetWindowGraphicPointer {
            val id = stream.readUnsignedByte()
            stream.readUnsignedShortLE()                    // Skip length
            val aux = stream.readUnsignedShortLE()

            val obj = DaadResetWindowGraphicPointer()
            obj.auxData = aux
            obj.checkId(id)
            return obj
        }
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
        return arrayOf("DAAD Reset Window Graphic Pointer")
    }
}