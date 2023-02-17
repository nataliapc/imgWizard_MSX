package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.compressor.Compressor
import org.nataliapc.imagewizard.image.chunks.ChunkCreateFrom
import org.nataliapc.imagewizard.image.chunks.ChunkData
import org.nataliapc.imagewizard.image.chunks.ChunkLegacy
import org.nataliapc.utils.DataByteArrayOutputStream
import org.nataliapc.utils.readUnsignedShortLE
import org.nataliapc.utils.writeShortLE
import java.io.DataInputStream
import java.lang.RuntimeException


/*
**** LEGACY CHUNK ****
    Chunk Screen Bitmap format:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type  (Raw:2 RLE:3 Pletter:4)
        0x0001  2    Compressed data length (max: 2043 bytes)
        0x0003  2    Uncompressed data length in bytes
        ---data---
        0x0005 ...   Compressed data (1-2043 bytes length)
 */
class ScreenBitmapChunk private constructor(val compressor: Compressor) :
    ChunkAbstractImpl(ID_BASE + compressor.id), ChunkData, ChunkLegacy
{
    private var compressedData = byteArrayOf()
    var auxData: Int = 0

    constructor(data: ByteArray, compressor: Compressor) : this(compressor) {
        compressedData = compressor.compress(data)
        auxData = data.size
        if (compressedData.size > MAX_CHUNK_DATA_SIZE) {
            throw RuntimeException("Maximum Chunk data size exceeded (max:$MAX_CHUNK_DATA_SIZE current:${compressedData.size})")
        }
        if (!compressor.uncompress(compressedData).contentEquals(data)) {
            throw RuntimeException("Error compressing data with ${compressor.javaClass.simpleName}")
        }
    }

    constructor(id: Int, compressedData: ByteArray, uncompressedSize: Int) : this(Compressor.Types.byId(id - ID_BASE)) {
        this.compressedData = compressedData
        auxData = uncompressedSize
    }

    companion object : ChunkCreateFrom {
        const val MAX_CHUNK_DATA_SIZE = 2043
        const val ID_BASE = 2

        override fun from(stream: DataInputStream): ScreenBitmapChunk {
            val id = stream.readUnsignedByte()
            val len = stream.readUnsignedShortLE()
            val auxData = stream.readUnsignedShortLE()
            val compressedData = stream.readNBytes(len)

            val obj = ScreenBitmapChunk(id, compressedData, auxData)
            obj.checkId(id)

            return obj
        }
    }

    override fun getRawData(): ByteArray = compressedData

    override fun getUncompressedData(): ByteArray = compressor.uncompress(compressedData)

    override fun build(): ByteArray
    {
        val out = DataByteArrayOutputStream()
        out.use {
            it.writeByte(getId())
            it.writeShortLE(compressedData.size)
            it.writeShortLE(auxData)
            it.write(compressedData)
        }
        return out.toByteArray()
    }

    override fun printInfo() {
        println("Screen Bitmap ${compressor.javaClass.simpleName.uppercase()} Data: $auxData bytes (${compressedData.size} bytes compressed) [${compressedData.size*100/auxData}%]")
    }
}