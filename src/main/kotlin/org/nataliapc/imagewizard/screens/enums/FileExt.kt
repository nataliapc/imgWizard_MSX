package org.nataliapc.imagewizard.screens.enums


enum class FileExt(val fileExtension: String, val imxExt: String, val magicHeader: String) {
    Unknown("", "", ""),
    SC5("SC5", "IM5", "IMG5"),
    SC6("SC6", "IM6", "IMG6"),
    SC7("SC7", "IM7", "IMG7"),
    SC8("SC8", "IM8", "IMG8"),
    SCA("SCA", "IMA", "IMGA"),
    SCC("SCC", "IMC", "IMGC");

    companion object {
        fun byName(value: String): FileExt {
            values().forEach {
                if (it.name == value) {
                    return it
                }
            }
            return Unknown
        }
        fun byFileExtension(value: String): FileExt {
            values().forEach {
                if (it.fileExtension == value) {
                    return it
                }
            }
            return Unknown
        }
    }
}