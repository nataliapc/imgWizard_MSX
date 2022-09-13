package org.nataliapc.imagewizard.compressor

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.nataliapc.imagewizard.utils.toHex


/**
 * Based on ZX7Mini by Einar Saukas and Antonio Villena
 * https://github.com/antoniovillena/zx7mini
 */
@ExperimentalUnsignedTypes
internal class Zx7MiniExternTest
{
    private val zx7 = Zx7MiniExtern()

    private val rawData1 = "35456456775643ABCABCDEFABCDE21379879032\n".toByteArray()
    private val validCompressed1 = ubyteArrayOf(
        0x33u, 0x0Eu, 0x35u, 0x34u, 0x35u, 0x36u, 0x02u, 0x37u, 0x70u, 0x37u, 0x06u, 0x33u, 0x41u, 0x42u, 0x43u, 0xE2u,
        0x02u, 0x44u, 0x45u, 0x46u, 0x60u, 0x05u, 0x32u, 0x31u, 0x33u, 0x37u, 0x39u, 0x50u, 0x38u, 0x02u, 0x30u, 0x33u,
        0x32u, 0x0Au, 0x80u, 0x00u, 0x00u).toByteArray()

    private val rawData2 = "101000101110001011010101010101001010100010000100100110101010100110100101010\n".toByteArray()
    private val validCompressed2 = ubyteArrayOf(
        0x31u, 0x5Bu, 0x30u, 0x01u, 0x00u, 0xD0u, 0x05u, 0x31u, 0x58u, 0x07u, 0x30u, 0x60u, 0x01u, 0xCFu, 0x06u, 0x03u,
        0x38u, 0x0Fu, 0x73u, 0x22u, 0x0Bu, 0xB4u, 0x0Cu, 0x0Au, 0x00u, 0x00u).toByteArray()

    @Test
    fun id_Ok()
    {
        val result = zx7.id

        assertEquals(3, result)
    }

    @Test
    fun compress_Ok1()
    {
        val result = zx7.compress(rawData1).copyOf()

        assertArrayEquals(validCompressed1, result)
        assertArrayEquals(rawData1, zx7.uncompress(result))
    }

    @Test
    fun compress_Ok2()
    {
        val result = zx7.compress(rawData2)

        assertArrayEquals(validCompressed2, result)
        assertArrayEquals(rawData2, zx7.uncompress(result))
    }

}