package org.nataliapc.utils

import java.nio.ByteOrder
import java.nio.ByteBuffer


abstract class LittleEndianByteBuffer
{
    companion object
    {
        fun allocate(capacity: Int): ByteBuffer
        {
            return ByteBuffer.allocate(capacity)
                .order(ByteOrder.LITTLE_ENDIAN)
        }

        fun wrap(array: ByteArray?, offset: Int, length: Int): ByteBuffer
        {
            return ByteBuffer.wrap(array, offset, length)
                .order(ByteOrder.LITTLE_ENDIAN)
        }

        fun wrap(array: ByteArray): ByteBuffer
        {
            return ByteBuffer.wrap(array, 0, array.size)
                .order(ByteOrder.LITTLE_ENDIAN)
        }
    }
}