package org.nataliapc.imagewizard.resourcefiles

import org.nataliapc.imagewizard.compressor.Compressor
import org.nataliapc.imagewizard.compressor.Raw
import org.nataliapc.utils.DataByteArrayOutputStream
import org.nataliapc.utils.writeIntLE
import org.nataliapc.utils.writeShortLE
import java.io.ByteArrayOutputStream
import java.util.zip.CRC32C


class ResFileImpl(val compressor: Compressor = Raw()) : ResFile
{
    private data class IndexResource(
        val filename: String,
        val absolutePosition: Long,
        val compressor: Compressor,
        val rawSize: Int,
        val compressedSize: Int,
        val crc32c: Long)
    {
        companion object {
            fun sizeOf() = IndexResource(
                "",
                0,
                Compressor.Types.RAW.instance,
                0,
                0,
                0).build().size
        }
        fun build(): ByteArray
        {
            return DataByteArrayOutputStream().use {
                it.write(filename.padEnd(13, 0.toChar()).substring(0,13).toByteArray())
                it.writeIntLE(absolutePosition)
                it.writeByte(compressor.id)
                it.writeShortLE(rawSize)
                it.writeShortLE(compressedSize)
                it.writeIntLE(crc32c)
                it.toByteArray()
            }
        }
    }

    private var collection = mutableListOf<ResElement>()
    private val resIndex = mutableListOf<IndexResource>()

    companion object
    {
        private const val magicHeader = "RESX"
    }

    override fun addResource(item: ResElement)
    {
        collection.add(item)
        resIndex.clear()
    }

    override fun build(verbose: Boolean): ByteArray
    {
        val out = DataByteArrayOutputStream()
        val data = DataByteArrayOutputStream()

        out.write(magicHeader.toByteArray())
        out.writeShortLE(collection.size)     // Index elements count
        data.writeByte(0xff)               // End of index

        val headerIndexRawSize: Long =
                out.size() +
                (collection.size.toLong() * IndexResource.sizeOf()) +
                data.size()

        collection.forEach {
            if (verbose) { print("    Compressing '${it.getName()}'") }

            val rawData = it.getContent()
            val compressedData = compressor.compress(rawData)
            val crc = CRC32C()
            crc.update(compressedData)

            if (verbose) { println("\t${rawData.size} bytes -> ${compressedData.size} bytes") }

            resIndex.add(IndexResource(
                it.getName(),
                headerIndexRawSize + data.size() - 1,
                compressor,
                rawData.size,
                compressedData.size,
                crc.value
            ))
            data.write(compressedData)
        }

        return out.use { stream ->
            resIndex.forEach { res ->               // Resources Index
                stream.write(res.build())
            }
            stream.write(data.toByteArray())        // Compressed data
            stream.toByteArray()
        }
    }

    override fun generateInclude(): String {
        if (resIndex.isEmpty()) {
            throw RuntimeException("Include file must be generated always after a build()")
        }
        var out = "#pragma once\n\n"

        resIndex.forEachIndexed { index, it ->
            val name = it.filename.uppercase().replace('.', '_')
            out += "#define ${name}_ID\t ${index}\n" +
                   "#define ${name}_POS\t ${it.absolutePosition}\n" +
                   "#define ${name}_SIZE\t ${it.rawSize}\n" +
                   "#define ${name}_COMPSIZE\t ${it.compressedSize}\n\n"
        }

        return out
    }
}
