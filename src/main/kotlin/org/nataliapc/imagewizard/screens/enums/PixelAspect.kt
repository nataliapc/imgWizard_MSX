package org.nataliapc.imagewizard.screens.enums

import org.nataliapc.imagewizard.image.chunks.impl.InfoChunk


enum class PixelAspect(val horizontalFactor: Int, val verticalFactor: Int) {
    Ratio11(1,1),
    Ratio12(1,2);

    companion object {
        fun getFromInfoChunk(infoChunk: InfoChunk?): PixelAspect {
            return if (infoChunk!=null && infoChunk.originalWidth == 512 && infoChunk.originalHeight == 212) {
                Ratio12
            } else {
                Ratio11
            }
        }
    }
}