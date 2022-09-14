package org.nataliapc.imagewizard

import org.nataliapc.imagewizard.image.chunks.impl.InfoChunk
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.system.exitProcess
import javax.swing.KeyStroke


class ViewFrame(
    private val appName: String,
    private val file: File,
    private val infoChunk: InfoChunk?,
    private val origImage: BufferedImage
) : JFrame(), KeyListener
{
    private var scaledImage: Image
    private var zoomFactor = 2

    private var picLabel: JLabel

    init {
        updateTitle()
        addKeyListener(this)

        //JMenu
        val menuBar = createMenuBar()

        //JLabel Picture
        picLabel = JLabel()
        picLabel.addNotify()
        scaledImage = scaleImage(picLabel)

        //JPanel
        val jPanel = JPanel()
        jPanel.background = Color.DARK_GRAY
        jPanel.layout = BorderLayout()
        jPanel.add(picLabel, BorderLayout.CENTER)

        //JScrollPane
        val scroller = JScrollPane(jPanel)

        //JFrame
        this.title = title
        defaultCloseOperation = EXIT_ON_CLOSE
        add(menuBar, BorderLayout.NORTH)
        add(scroller, BorderLayout.CENTER)
        pack()
        setLocationRelativeTo(null)
        isVisible = true
    }

    private fun updateTitle() {
        title = "$file.name (zoom x$zoomFactor) - $appName"
    }

    private fun createMenuBar(): JMenuBar {
        val menuBar = JMenuBar()

        // Menu File
        val menuFile = JMenu("File")
        menuFile.mnemonic = KeyEvent.VK_F
        menuBar.add(menuFile)
        if (true) {
            val mItemSave = JMenuItem("Save original image as PNG", 's'.code)
            mItemSave.addActionListener { doSaveOriginalImage() }
            mItemSave.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK)
            menuFile.add(mItemSave)

            val mItemSaveScaled = JMenuItem("Save scaled image as PNG")
            mItemSaveScaled.addActionListener { doSaveScaledImage() }
            mItemSaveScaled.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK or KeyEvent.SHIFT_DOWN_MASK)
            menuFile.add(mItemSaveScaled)

            val mItemExit = JMenuItem("Exit", 'x'.code)
            mItemExit.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.ALT_DOWN_MASK)
            mItemExit.addActionListener { doExit() }
            menuFile.add(mItemExit)
        }

        // Menu View
        val menuView = JMenu("View")
        menuView.mnemonic = KeyEvent.VK_V
        menuBar.add(menuView)
        if (true) {
            val mZoomIn = JMenuItem("Zoom In", '+'.code)
            mZoomIn.addActionListener { doZoomIn() }
            mZoomIn.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0)
            menuView.add(mZoomIn)

            val mZoomOut = JMenuItem("Zoom Out", '-'.code)
            mZoomOut.addActionListener { doZoomOut() }
            mZoomOut.accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0)
            menuView.add(mZoomOut)
        }

        // Menu About
        val menuAbout = JMenu("About")
        menuAbout.mnemonic = KeyEvent.VK_A
        menuAbout.addActionListener {
//            val popup = Popup()
//            this.add(popup)
        }
        menuBar.add(menuAbout)

        return menuBar
    }

    private fun doSaveOriginalImage() {
        val outputfile = fileToSave("Save original image to...")
        if (outputfile.name.isNotEmpty()) {
            ImageIO.write(origImage, outputfile.extension, outputfile)
        }
    }

    private fun doSaveScaledImage() {
        val outputfile = fileToSave("Save scaled image to...")
        if (outputfile.name.isNotEmpty()) {
            ImageIO.write(toBufferedImage(scaledImage), outputfile.extension, outputfile)
        }
    }

    private fun doZoomIn() {
        zoomFactor = (zoomFactor + 1).coerceAtMost(8)
        scaledImage = scaleImage(picLabel)
        updateTitle()
    }

    private fun doZoomOut() {
        zoomFactor = (zoomFactor - 1).coerceAtLeast(1)
        scaledImage = scaleImage(picLabel)
        updateTitle()
    }

    private fun doExit() {
        dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
        exitProcess(0)
    }

    private fun scaleImage(label: JLabel): Image {
        val zoomWidth = origImage.width * zoomFactor
        val zoomHeight = origImage.height * zoomFactor
        val image = origImage.getScaledInstance(zoomWidth, zoomHeight, Image.SCALE_FAST)
        label.icon = ImageIcon(image)
        pack()
        return image
    }

    private fun fileToSave(title: String): File {
        val dialog = FileDialog(this, title, FileDialog.SAVE)

        dialog.isMultipleMode = false
        dialog.isAlwaysOnTop = true
        dialog.toFront()
        dialog.directory = System.getProperty("user.dir")
        dialog.setLocation(x - 100, y - 50)
        dialog.isVisible = true

        return File(dialog.file ?: "")
    }

    private fun toBufferedImage(img: Image): BufferedImage {
        if (img is BufferedImage) {
            return img
        }

        // Create a buffered image with transparency
        val bufferedimage = BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB)

        // Draw the image on to the buffered image
        val bGr = bufferedimage.createGraphics()
        bGr.drawImage(img, 0, 0, null)
        bGr.dispose()

        // Return the buffered image
        return bufferedimage
    }

    override fun keyTyped(event: KeyEvent?) {
        if (event != null) {
            when (event.keyChar.code) {
                43 -> doZoomIn()
                KeyEvent.VK_MINUS -> doZoomOut()
            }
        }
    }

    override fun keyPressed(event: KeyEvent?) {}

    override fun keyReleased(event: KeyEvent?) {}

}