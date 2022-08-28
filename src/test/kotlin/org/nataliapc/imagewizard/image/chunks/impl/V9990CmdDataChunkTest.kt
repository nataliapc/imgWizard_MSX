package org.nataliapc.imagewizard.image.chunks.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.nataliapc.imagewizard.compressor.Pletter
import org.nataliapc.imagewizard.compressor.Rle
import org.nataliapc.imagewizard.compressor.Raw
import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl.Companion.MAX_CHUNK_DATA_SIZE
import org.nataliapc.imagewizard.screens.PixelType
import org.nataliapc.imagewizard.screens.imagewrapper.ImageWrapperImpl
import org.nataliapc.imagewizard.utils.DataByteArrayInputStream
import org.nataliapc.imagewizard.utils.DataByteArrayOutputStream
import java.lang.RuntimeException
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO


@ExperimentalUnsignedTypes
internal class V9990CmdDataChunkTest {

    @Test
    fun from_Ok() {
        val stream = DataByteArrayInputStream(byteArrayOf(33, 5,0, 5,0, 0,1,2,3,4))

        val result = V9990CmdDataChunk.from(stream)

        assertEquals(33, result.getId())
        assertArrayEquals(byteArrayOf(0,1,2,3,4), result.getRawData())
    }

    @Test
    fun fromRectangle_Ok() {
        val bufimg = BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB)
        bufimg.setRGB(0,0,0x000000)
        bufimg.setRGB(1,0,0xff0000)
        bufimg.setRGB(0,1,0x00ff00)
        bufimg.setRGB(1,1,0x0000ff)
        val stream = DataByteArrayOutputStream()
        ImageIO.write(bufimg, "png", stream)
        val inStream = ByteArrayInputStream(stream.toByteArray())
        val image = ImageWrapperImpl.from(inStream, pixelType = PixelType.BD16)

        val result = V9990CmdDataChunk.fromRectangle(image,0,0,2,2, Raw())

        assertEquals(33, result.getId())
        assertArrayEquals(
            ubyteArrayOf(0x00u,0x00u,0xe0u,0x03u,0x00u,0x7cu,0x1fu,0x00u).toByteArray(),
            result.getUncompressedData()
        )
    }

    @Test
    fun getRawData_Ok() {
        val chunk = V9990CmdDataChunk(byteArrayOf(0,1,2,3,4), Rle())

        val result = chunk.getRawData()

        assertArrayEquals(byteArrayOf(5, 0,1,2,3,4, 5,0), result)
    }

    @Test
    fun getUncompressedData_Ok() {
        val chunk = V9990CmdDataChunk(byteArrayOf(0,1,2,3,4), Rle())

        val result = chunk.getUncompressedData()

        assertArrayEquals(byteArrayOf(0,1,2,3,4), result)
    }

    @Test
    fun build_Ok_Raw() {
        val chunk = V9990CmdDataChunk(byteArrayOf(1,2,3,4,5), Raw())

        val result = chunk.build()

        assertEquals(33, chunk.getId())
        assertArrayEquals(
            byteArrayOf(33, 5,0, 5,0, 1,2,3,4,5),
            result
        )
    }

    @Test
    fun build_Ok_RLE() {
        val chunk = V9990CmdDataChunk("12234445666666666677776866966660".toByteArray(), Rle())

        val result = chunk.build()

        assertEquals(34, chunk.getId())
        assertArrayEquals(
            byteArrayOf(34, 26,0, 32,0) +
            "\u000012234445\u0000\u000a6\u0000\u0004768669\u0000\u000460\u0000\u0000".toByteArray(),
            result
        )
    }

    @Test
    fun build_Ok_Pletter() {
        val chunk = V9990CmdDataChunk(byteArrayOf(1,2,3,4,5,1,2,3,4,5,6,7), Pletter())

        val result = chunk.build()

        assertEquals(35, chunk.getId())
        assertArrayEquals(
            byteArrayOf(35, 15,0, 12,0, 1,1,2,3,4,5,-95,4,6,7,-1,-1,-1,-1,-128),
            result
        )
    }

    @Test
    fun build_Fail_ChunkSizeExceeded() {
        assertThrows(RuntimeException::class.java) {
            V9990CmdDataChunk(ByteArray(MAX_CHUNK_DATA_SIZE + 1), Raw())
        }
    }

    @Test
    fun printInfo_Ok() {
        val chunk = V9990CmdDataChunk(byteArrayOf(1,2,3,4,5,1,2,3,4,5,6,7), Pletter())

        chunk.printInfo()
    }
}