package org.nataliapc.imagewizard.compressor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


@ExperimentalUnsignedTypes
internal class PletterTest
{
    private val pletter = Pletter()

    private val rawData1 = "35456456775643ABCABCDEFABCDE21379879032\n".toByteArray()
    private val validCompressed1 = ubyteArrayOf(
        0x01u, 0x33u, 0x35u, 0x34u, 0x35u, 0x36u, 0x86u, 0x02u, 0x37u, 0x37u, 0x06u, 0x06u, 0x33u, 0x41u, 0x42u, 0x43u,
        0x0Du, 0x02u, 0x44u, 0x45u, 0x46u, 0x00u, 0x05u, 0x32u, 0x31u, 0x33u, 0x37u, 0x39u, 0x38u, 0x83u, 0x02u, 0x30u,
        0x33u, 0x32u, 0x0Au, 0xFFu, 0xFFu, 0xFFu, 0xFFu).toByteArray()

    private val rawData2 = "101000101110001011010101010101001010100010000100100110101010100110100101010\n".toByteArray()
    private val validCompressed2 = ubyteArrayOf(
        0x0Au, 0x31u, 0x30u, 0x01u, 0x00u, 0xC7u, 0x05u, 0x31u, 0xCDu, 0x07u, 0x30u, 0xEFu, 0x01u, 0xB4u, 0x06u, 0x03u,
        0xDBu, 0x0Fu, 0x7Au, 0x22u, 0x0Bu, 0xDCu, 0x24u, 0x7Fu, 0x0Au, 0xFFu, 0xFFu, 0xFFu, 0xE0u).toByteArray()

    @Test
    fun id_Ok()
    {
        val result = pletter.id

        assertEquals(2, result)
    }

    @Test
    fun compress_Ok1()
    {
        val result = pletter.compress(rawData1)

        assertArrayEquals(validCompressed1, result)
        assertArrayEquals(rawData1, pletter.uncompress(result))
    }

    @Test
    fun compress_Ok2()
    {
        val result = pletter.compress(rawData2)

        assertArrayEquals(validCompressed2, result)
        assertArrayEquals(rawData2, pletter.uncompress(result))
    }

}