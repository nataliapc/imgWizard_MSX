package org.nataliapc.imagewizard.swing.listeners

import java.awt.Cursor
import java.awt.Point
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.util.concurrent.atomic.AtomicBoolean

import java.util.concurrent.atomic.AtomicInteger
import javax.swing.*


class MouseDragAndMoveScrollListener(private val scrolledLabel: JLabel): MouseListener, MouseMotionListener {
    private var viewPort: JViewport = SwingUtilities.getAncestorOfClass(JViewport::class.java, scrolledLabel) as JViewport
    private val isPressed = AtomicBoolean(false)
    private var oldPos = Point()
    private var incX = AtomicInteger(0)
    private var incY = AtomicInteger(0)


    // MouseListener
    override fun mouseClicked(event: MouseEvent) {}
    override fun mouseEntered(event: MouseEvent) {}
    override fun mouseExited(event: MouseEvent) {}
    // MouseMotionListener
    override fun mouseMoved(event: MouseEvent) {}

    // MouseListener
    override fun mousePressed(event: MouseEvent) {
        if (event.button == MouseEvent.BUTTON1) {
            synchronized(this) {
                if (!event.isConsumed && !isPressed.getAndSet(true)) {
                    scrolledLabel.cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
                    viewPort = SwingUtilities.getAncestorOfClass(JViewport::class.java, scrolledLabel) as JViewport
                    oldPos.setLocation(event.x, event.y)
                    event.consume()
                }
            }
        }
    }

    override fun mouseReleased(event: MouseEvent) {
        if (event.button == MouseEvent.BUTTON1) {
            synchronized(this) {
                if (!event.isConsumed && isPressed.getAndSet(false)) {
                    scrolledLabel.cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
                    event.consume()
                }
            }
        }
    }

    // MouseMotionListener
    override fun mouseDragged(event: MouseEvent) {
        synchronized(this) {
            if (!event.isConsumed && isPressed.get()) {
                incX.addAndGet(oldPos.x - event.x)
                incY.addAndGet(oldPos.y - event.y)

                val view = viewPort.viewRect
                val pos = viewPort.viewPosition
                view.x += incX.get()
                view.y += incY.get()
                if (scrolledLabel.width > viewPort.width) {
                    pos.x -= event.x - oldPos.x
                    if (pos.x < 0) {
                        pos.x = 0
                        oldPos.x = event.x
                    }
                    if (pos.x > scrolledLabel.width - viewPort.width) {
                        pos.x = scrolledLabel.width - viewPort.width
                        oldPos.x = event.x
                    }
                }
                if (scrolledLabel.height > viewPort.height) {
                    pos.y -= event.y - oldPos.y
                    if (pos.y < 0) {
                        pos.y = 0
                        oldPos.y = event.y
                    }
                    if (pos.y > scrolledLabel.height - viewPort.height) {
                        pos.y = scrolledLabel.height - viewPort.height
                        oldPos.y = event.y
                    }
                }
                viewPort.viewPosition = pos

                event.consume()
                return
            }
        }
    }

}