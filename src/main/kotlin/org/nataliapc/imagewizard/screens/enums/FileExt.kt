package org.nataliapc.imagewizard.screens.enums


enum class FileExt(val fileExtension: String, val imxExt: String) {
    Unknown("", ""),
    SC5("SC5", "IM5"),
    SC6("SC6", "IM6"),
    SC7("SC7", "IM7"),
    SC8("SC8", "IM8"),
    SCA("SCA", "IMA"),
    SCC("SCC", "IMC");

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