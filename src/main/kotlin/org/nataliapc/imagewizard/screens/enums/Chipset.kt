package org.nataliapc.imagewizard.screens.enums

enum class Chipset(val ramKb: Int) {
    Unspecified(0),
    TMS9918(16),
    V9938(128),
    V9958(128),
    V9990(512);

    companion object {
        fun byId(id: Int): Chipset {
            return values()[id]
        }
    }
}