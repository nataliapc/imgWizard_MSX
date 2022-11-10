package org.nataliapc.imagewizard.image.chunks

import org.nataliapc.imagewizard.image.chunks.impl.*
import org.nataliapc.imagewizard.utils.DataByteArrayInputStream
import org.nataliapc.imagewizard.utils.DataByteArrayOutputStream
import org.nataliapc.imagewizard.utils.readUnsignedShortLE
import org.nataliapc.imagewizard.utils.writeShortLE
import java.io.DataInputStream
import java.lang.RuntimeException


interface Chunk
{
    class Factory {
        companion object {
            fun from(stream: DataInputStream): Chunk {
                val id = stream.readUnsignedByte()
                val len = stream.readUnsignedShortLE()
                val len2 = stream.readUnsignedShortLE()

                val buffer = DataByteArrayOutputStream()
                buffer.writeByte(id)
                buffer.writeShortLE(len)
                buffer.writeShortLE(len2)
                if (id in 2..4) {
                    //Legacy DAAD Chunks
                    buffer.write(stream.readNBytes(len))
                } else {
                    //New Chunk format
                    buffer.write(stream.readNBytes(len + len2))
                }
                val chunkStream = DataByteArrayInputStream(buffer.toByteArray())

                return when (id) {
                    0 -> DaadRedirectToImage.from(chunkStream)
                    1 -> ScreenPaletteChunk.from(chunkStream)
                    2,3,4 -> ScreenBitmapChunk.from(chunkStream)
                    16 -> DaadResetWindowGraphicPointer.from(chunkStream)
                    17 -> DaadClearWindow.from(chunkStream)
                    18 -> DaadSkipBytes.from(chunkStream)
                    19 -> PauseChunk.from(chunkStream)
                    32 -> V9990CmdChunk.from(chunkStream)
                    33 -> V9990CmdDataChunk.from(chunkStream)
                    34 -> V9990CmdDataChunk.from(chunkStream)
                    35 -> V9990CmdDataChunk.from(chunkStream)
                    128 -> InfoChunk.from(chunkStream)
                    else -> throw RuntimeException("Unknown Chunk type $id")
                }
            }
        }
    }

    fun getId(): Int
    fun build(): ByteArray
    fun getInfo(): Array<String>
    fun printInfo()
    fun printInfoWithOrdinal(ordinal: Int)
}

interface ChunkCompanion
{
    fun from(stream: DataInputStream): Chunk
}