package org.nataliapc.imagewizard.resourcefiles

import org.nataliapc.imagewizard.compressor.Compressor
import org.nataliapc.imagewizard.resourcefiles.interfaces.ResElement
import org.nataliapc.utils.*
import java.io.DataInputStream
import java.io.InputStream
import java.util.zip.CRC32


class ResFileImpl() : ResFile
{
    data class IndexResource(
        val filename: String,
        val absolutePosition: Long,
        val compressor: Compressor,
        val rawSize: Int,
        val compressedSize: Int,
        val crc32: Long)
    {
        companion object {
            fun sizeOf() = IndexResource(
                "",
                0,
                Compressor.Types.RAW.instance,
                0,
                0,
                0).build().size
            fun from(inputStream: InputStream): IndexResource {
                val stream = DataInputStream(inputStream)
                return IndexResource(
                    stream.readNBytes(13).copyOf(12).toCharString().trimEnd('\u0000'),
                    stream.readUnsignedIntLE(),
                    Compressor.Types.byId(stream.read()),
                    stream.readUnsignedShortLE(),
                    stream.readUnsignedShortLE(),
                    stream.readUnsignedIntLE()
                )
            }
        }
        fun build(): ByteArray
        {
            return DataByteArrayOutputStream().use {
                it.write(filename.toByteArray().copyOf(12).copyOf(13))
                it.writeIntLE(absolutePosition)
                it.writeByte(compressor.id)
                it.writeShortLE(rawSize)
                it.writeShortLE(compressedSize)
                it.writeIntLE(crc32)
                it.toByteArray()
            }
        }
        fun printInfo(index: Int = 1) {
            println(" %3d | %-12s | %7d | %-10s | %8d | %8d | %02d%% | %08x".format(
                index,
                filename,
                absolutePosition,
                compressor.getName(),
                rawSize,
                compressedSize,
                compressedSize * 100 / rawSize,
                crc32
            ))
        }
    }

    /**
     * A mutable list to store the resource elements for the resource file.
     * This list contains the actual resource data, such as images, fonts, or other binary assets.
     * The resources are stored in the order they appear in the resource file index.
     */
    private var resCollection = mutableListOf<ResElement>()
    /**
     * A mutable list to store the index resources for the resource file.
     * This list contains metadata about each resource in the file, such as the filename,
     * absolute position, compressor type, raw size, compressed size, and CRC32 checksum.
     * The index is used to quickly locate and retrieve individual resources from the file.
     */
    private val resIndex = mutableListOf<IndexResource>()

    companion object
    {
        private const val magicHeader = "RESX"

        fun from(inputStream: InputStream): ResFile {
            val stream = DataInputStream(inputStream)
            val resX = ResFileImpl()

            val header = stream.readNBytes(magicHeader.length).toCharString()
            if (!header.startsWith(magicHeader.subSequence(0, 3))) {
                throw java.lang.RuntimeException("Bad magic header reading RES (${header})")
            }

            val indexSize = stream.readUnsignedShortLE()
            for (i in 0 until indexSize) {
                resX.resIndex.add(IndexResource.from(stream))
            }

            resX.resIndex.forEach {
                resX.resCollection.add(
                    ResElementByteArray(it, stream.readNBytes(it.compressedSize))
                )
            }

            return resX
        }
    }

    override fun addResource(item: ResElement)
    {
        resCollection.removeIf { it.getName() == item.getName() }
        resCollection.add(item)
        resIndex.clear()
    }

    override fun getReource(index: Int): ResElement {
        return resCollection[index]
    }

    override fun build(verbose: Boolean): ByteArray
    {
        val out = DataByteArrayOutputStream()
        val data = DataByteArrayOutputStream()

        out.write(magicHeader.toByteArray())
        out.writeShortLE(resCollection.size)     // Index elements count
        data.writeByte(0xff)                    // End of index

        val headerIndexRawSize: Long =
                out.size() +
                (resCollection.size.toLong() * IndexResource.sizeOf()) +
                data.size()

        resCollection.forEach {
            when (it) {
                is ResElementByteArray -> {
                    if (verbose) { println("      Keeping '${it.getName()}' [${it.getCompressor().getName()}]") }

                    resIndex.add(
                        IndexResource(
                            it.getName(),
                            headerIndexRawSize + data.size() - 1,
                            it.getCompressor(),
                            it.getSize(),
                            it.getCompressedSize(),
                            it.getCRC()
                        )
                    )
                    data.write(it.getContent())
                }
                is ResElementFile -> {
                    if (verbose) { print("  Compressing '${it.getName()}' [${it.getCompressor().getName()}]") }

                    val rawData = it.getContent()
                    val compressedData = it.getCompressor().compress(rawData)
                    val crc = CRC32()
                    crc.update(compressedData)

                    if (verbose) { println("\t${rawData.size} bytes -> ${compressedData.size} bytes") }

                    resIndex.add(IndexResource(
                        it.getName(),
                        headerIndexRawSize + data.size() - 1,
                        it.getCompressor(),
                        rawData.size,
                        compressedData.size,
                        crc.value
                    ))
                    data.write(compressedData)
                }
            }
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

    override fun printInfo() {
        var totalRawSize = 0
        var totalCompressedSize = 0
        println(" ### | Filename     | Abs.Pos.| Compressor | Raw Size | Compress |  %  | CRC32")
        println("-----+--------------+---------+------------+----------+----------+-----+----------")
        resIndex.forEachIndexed { index, it ->
            it.printInfo(index + 1)
            totalRawSize += it.rawSize
            totalCompressedSize += it.compressedSize
        }
        println("Total data: ${totalRawSize/1024}Kb | "+
                "Compressed: ${totalCompressedSize/1024}Kb "+
                "(${totalCompressedSize * 100 / totalRawSize}%)")
    }

}
