package org.nataliapc.imagewizard.image.chunks


interface Chunk
{
    companion object {
        const val MAX_CHUNK_DATA_SIZE = 2048
        const val CHUNK_DATA_SIZE_THREESHOLD = 2040
    }

    fun getId(): Int
    fun build(): ByteArray
    fun getInfo(): Array<String>
    fun printInfo()
    fun printInfoWithOrdinal(ordinal: Int)
}
