package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.compressor.Compressor
import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.ChunkCompanion
import org.nataliapc.imagewizard.screens.interfaces.ScreenRectangle
import org.nataliapc.imagewizard.utils.readUnsignedShortLE
import java.io.DataInputStream
import java.lang.RuntimeException


/*
    Chunk V9990 Send data to Command Port (P#2):
        Offset Size  Description
        --header--
        0x0000  1    Chunk type  (Raw:33 RLE:34 Pletter:35)
        0x0001  2    Compressed data length (max: 2043 bytes)
        0x0003  2    Uncompressed data length in bytes
        ---data---
        0x0005 ...   Compressed data (1-2043 bytes length)
 */
class V9990CmdDataChunk(data: ByteArray, private val compressor: Compressor) : ChunkAbstractImpl(33 + compressor.id)
{
    private var compressedData: ByteArray = compressor.compress(data)

    init {
        auxData = data.size
        if (compressedData.size > MAX_CHUNK_DATA_SIZE) {
            throw RuntimeException("Maximum Chunk data size exceeded (max:$MAX_CHUNK_DATA_SIZE current:${compressedData.size})")
        }
//        if (!compressor.uncompress(compressedData).contentEquals(data)) {
//TODO            throw RuntimeException("Error compressing data with ${compressor.javaClass.simpleName}")
//        }
    }

    companion object : ChunkCompanion {
        override fun createFrom(stream: DataInputStream): Chunk {
            val id = stream.readUnsignedByte()
            val len = stream.readUnsignedShortLE()
            val auxData = stream.readUnsignedShortLE()

            val obj = V9990CmdDataChunk(
                ByteArray(len),                             // TODO: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!! uncompress the data
                Compressor.Types.compressorById(id-33)
            )

            obj.checkId(id)
            obj.auxData = auxData
            obj.compressedData = stream.readNBytes(len)

            return obj
        }

        fun fromRectangle(scr: ScreenRectangle, x: Int, y: Int, w: Int, h: Int, compressor: Compressor): V9990CmdDataChunk
        {
            return V9990CmdDataChunk(scr.getRectangle(x, y, w, h), compressor)
        }
    }

    override fun build(): ByteArray
    {
        return ensemble(compressedData)
    }

    override fun printInfo() {
        println("    ID ${getId()}: V9990 ${compressor.javaClass.simpleName.uppercase()} Data Command (${compressedData.size} bytes) [${compressedData.size*100/auxData}%]\n"+
                "        Uncompressed size: $auxData bytes")
    }
}