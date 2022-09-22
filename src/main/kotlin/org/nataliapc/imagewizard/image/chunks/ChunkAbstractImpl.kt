package org.nataliapc.imagewizard.image.chunks

import org.nataliapc.imagewizard.image.chunks.impl.DaadClearWindow
import org.nataliapc.imagewizard.utils.DataByteArrayOutputStream
import org.nataliapc.imagewizard.utils.readUnsignedShortLE
import org.nataliapc.imagewizard.utils.writeShortLE
import java.io.DataInputStream
import java.lang.RuntimeException


abstract class ChunkAbstractImpl(private val id: Int): Chunk
{
    open var auxData: Int = 0

    companion object : ChunkCompanion {
        const val MAX_CHUNK_DATA_SIZE = 2043

        override fun from(stream: DataInputStream): Chunk {
            stream.readUnsignedByte()
            val length = stream.readUnsignedShortLE()
            stream.readNBytes(2 + length)
            return DaadClearWindow()
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

    override fun getInfo(): Array<String> {
        return arrayOf("**** UNKNOWN CHUNK TYPE ****")
    }

    override fun printInfo() {
        getInfo().forEach {
            println(it)
        }
    }

    override fun printInfoWithOrdinal(ordinal: Int) {
        print("CHUNK #$ordinal [id:${getId()}] ")
        printInfo()
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