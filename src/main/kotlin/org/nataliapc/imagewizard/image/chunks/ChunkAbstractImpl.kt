package org.nataliapc.imagewizard.image.chunks

import org.nataliapc.imagewizard.image.chunks.impl.InfoChunk
import org.nataliapc.imagewizard.utils.DataByteArrayOutputStream
import org.nataliapc.imagewizard.utils.LittleEndianByteBuffer
import org.nataliapc.imagewizard.utils.readUnsignedShortLE
import org.nataliapc.imagewizard.utils.writeShortLE
import java.io.DataInputStream
import java.lang.RuntimeException


abstract class ChunkAbstractImpl(private val id: Int): Chunk
{
    var auxData: Int = 0

    companion object : ChunkCompanion {
        const val MAX_CHUNK_DATA_SIZE = 2043

        override fun createFrom(stream: DataInputStream): Chunk {
            val id = stream.readUnsignedByte()
            val length = stream.readUnsignedShortLE()
            stream.readNBytes(2 + length)
            return InfoChunk()
        }
    }

    override fun getId(): Int {
        return id
    }

    override fun setAuxData(value: Int): Chunk
    {
        auxData = value
        return this
    }

    override fun printInfo() {
        println("    ID $id: **** UNKNOWN CHUNK TYPE ****")
    }

    protected fun checkId(id: Int) {
        if (this.id != id) {
            throw RuntimeException("Invalid Id Chunk")
        }
    }

    protected fun buildHeader(): ByteArray
    {
        return byteArrayOf(getId().toByte())
    }

    protected fun ensemble(data: ByteArray): ByteArray
    {
        val header = buildHeader()
        val out = DataByteArrayOutputStream()

        out.write(header)
        out.writeShortLE(data.size)
        out.writeShortLE(auxData)
        out.write(data)

        return out.toByteArray()
    }
}