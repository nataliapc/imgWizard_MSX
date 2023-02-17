package org.nataliapc.imagewizard.image.chunks


interface Chunk
{
    fun getId(): Int
    fun build(): ByteArray
    fun getInfo(): Array<String>
    fun printInfo()
    fun printInfoWithOrdinal(ordinal: Int)
}
