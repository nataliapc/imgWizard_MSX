package org.nataliapc.imagewizard.image


class ImgXFactory {
    fun getInstance(withInfoChunk: Boolean = true): ImgX = ImgXImpl(withInfoChunk)
}