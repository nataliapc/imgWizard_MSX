package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.image.chunks.ChunkCompanion
import org.nataliapc.imagewizard.utils.DataByteArrayOutputStream
import org.nataliapc.imagewizard.utils.LittleEndianByteBuffer
import org.nataliapc.imagewizard.utils.readUnsignedShortLE
import org.nataliapc.imagewizard.utils.writeShortLE
import java.io.DataInputStream


/*
    Chunk DAAD Redirect format:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type: (0:redirect)
        0x0001  2    Chunk data length (always 0)
        0x0003  2    New image location to read
 */
class DaadRedirectToImage(val location: Short) : ChunkAbstractImpl(0)
{
    companion object : ChunkCompanion {
        override fun from(stream: DataInputStream): DaadRedirectToImage {
            val id = stream.readUnsignedByte()
            stream.readUnsignedShortLE()                    // Skip length
            val aux = stream.readUnsignedShortLE()

            val obj = DaadRedirectToImage(aux.toShort())
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
        out.writeShortLE(location)

        return out.toByteArray()
    }

    override fun printInfo() {
        println("[${getId()}] DAAD Redirect Image (to location: $location)")
    }
}