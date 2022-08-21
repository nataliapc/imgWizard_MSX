package org.nataliapc.imagewizard.image.chunks

import org.nataliapc.imagewizard.image.chunks.impl.DaadRedirectToImage
import org.nataliapc.imagewizard.image.chunks.impl.InfoChunk
import org.nataliapc.imagewizard.image.chunks.impl.V9990CmdChunk
import org.nataliapc.imagewizard.image.chunks.impl.V9990CmdDataChunk
import org.nataliapc.imagewizard.utils.DataByteArrayInputStream
import org.nataliapc.imagewizard.utils.DataByteArrayOutputStream
import org.nataliapc.imagewizard.utils.readUnsignedShortLE
import org.nataliapc.imagewizard.utils.writeShortLE
import java.io.DataInputStream
import java.lang.RuntimeException
import java.nio.ByteBuffer


interface Chunk
{
    class Factory {
        companion object {
            fun createFromStream(stream: DataInputStream): Chunk {
                val id = stream.readUnsignedByte()
                val len = stream.readUnsignedShortLE()
                val buffer = DataByteArrayOutputStream()
                buffer.writeByte(id)
                buffer.writeShortLE(len)
                buffer.writeShortLE(stream.readUnsignedShortLE())
                buffer.write(stream.readNBytes(len))
                val chunkStream = DataByteArrayInputStream(buffer.toByteArray())

                return when (id) {
                    0 -> DaadRedirectToImage.createFrom(chunkStream)
                    32 -> V9990CmdChunk.createFrom(chunkStream)
                    33 -> V9990CmdDataChunk.createFrom(chunkStream)
                    34 -> V9990CmdDataChunk.createFrom(chunkStream)
                    35 -> V9990CmdDataChunk.createFrom(chunkStream)
                    128 -> InfoChunk.createFrom(chunkStream)
                    else -> throw RuntimeException("Unknown Chunk type")
                }
            }
        }
    }

    fun getId(): Int
    fun setAuxData(value: Int): Chunk
    fun build(): ByteArray
    fun printInfo()
}

interface ChunkCompanion
{
    fun createFrom(stream: DataInputStream): Chunk
}