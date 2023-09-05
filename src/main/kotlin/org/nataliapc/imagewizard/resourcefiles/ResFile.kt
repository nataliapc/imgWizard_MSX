package org.nataliapc.imagewizard.resourcefiles


interface ResFile
{
    fun addResource(item: ResElement)
    fun getReource(index: Int): ResElement
    fun build(verbose: Boolean = false): ByteArray
    fun generateInclude(): String
    fun printInfo()
}