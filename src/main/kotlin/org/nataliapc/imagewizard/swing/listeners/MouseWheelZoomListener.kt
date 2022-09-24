package org.nataliapc.imagewizard.swing.listeners

import org.nataliapc.imagewizard.swing.interfaces.Zoomable
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import java.util.concurrent.atomic.AtomicBoolean

class MouseWheelZoomListener(private val zoomable: Zoomable): MouseWheelListener {
    // MouseWheelListener
    var lockWheel = AtomicBoolean(false)

    override fun mouseWheelMoved(event: MouseWheelEvent) {
        if (lockWheel.getAndSet(true)) return

        val notches: Int = event.wheelRotation
        if (notches < 0) {
            zoomable.doZoomIn()
        } else {
            zoomable.doZoomOut()
        }
        lockWheel.set(false)
    }
}