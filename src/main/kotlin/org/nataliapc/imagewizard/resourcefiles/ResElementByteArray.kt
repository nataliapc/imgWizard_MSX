package org.nataliapc.imagewizard.resourcefiles

class ResElementByteArray(private val name: String, private val content: ByteArray): ResElement
{
    override fun getName(): String
    {
        return name
    }

    override fun getContent(): ByteArray
    {
        return content
    }

    override fun getSize(): Int
    {
        return content.size
    }
}