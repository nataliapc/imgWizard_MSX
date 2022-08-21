package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.ImgX
import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.image.chunks.ChunkCompanion
import org.nataliapc.imagewizard.utils.DataByteArrayOutputStream
import org.nataliapc.imagewizard.utils.LittleEndianByteBuffer
import org.nataliapc.imagewizard.utils.readUnsignedShortLE
import org.nataliapc.imagewizard.utils.writeShortLE
import java.io.DataInputStream


/*
    Chunk Info format:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type: (128)
        0x0001  2    Chunk data length (avoiding header)
        0x0003  2    Empty chunk header (filled with 0x00)
        --data--
        0x0005  1    InfoChunk version
        0x0006  2    Chunk count (without info chunk)
 */
class InfoChunk : ChunkAbstractImpl(128)
{
    private var infoVersion = 1
    private var chunkCount = 0

    companion object : ChunkCompanion {
        override fun createFrom(stream: DataInputStream): Chunk {
            val id = stream.readUnsignedByte()
            stream.readUnsignedShortLE()                    // Skip length

            val obj = InfoChunk()
            obj.checkId(id)

            obj.auxData = stream.readUnsignedShortLE()
            obj.infoVersion = stream.readUnsignedByte()
            obj.chunkCount = stream.readUnsignedShortLE()

            return obj
        }
    }

    fun update(image: ImgX): Chunk
    {
        chunkCount = image.chunkCount() - 1
        return this
    }

    override fun build(): ByteArray
    {
        val out = DataByteArrayOutputStream()

        out.writeByte(infoVersion)
        out.writeShortLE(chunkCount)

        return ensemble(out.toByteArray())
    }

    override fun printInfo() {
        println("    ID ${getId()}: Image Info\n"+
                "        Chunk version: $infoVersion\n"+
                "        Chunk count: $chunkCount")
    }
}