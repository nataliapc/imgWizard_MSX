package org.nataliapc.imagewizard

import org.nataliapc.imagewizard.image.chunks.impl.InfoChunk
import org.nataliapc.imagewizard.screens.enums.PixelType
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
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder
import java.awt.GridBagConstraints
import java.awt.Insets
import java.awt.Font
import java.util.*

import javax.swing.UIManager
import javax.swing.plaf.FontUIResource


class ViewFrame(
    private val appName: String,
    private val file: File,
    private val infoChunk: InfoChunk?,
    private val origImage: BufferedImage
) : JFrame(), KeyListener
{
    private val maxZoom = 8

    private var scaledImage: Image
    private var zoomFactor = 1

    private var picLabel: JLabel
    private var scroller: JScrollPane
    private lateinit var panelInfo: JPanel

    init {
        setUIFont(FontUIResource("Arial", Font.PLAIN, 12))
        title = appName
        addKeyListener(this)
        defaultCloseOperation = EXIT_ON_CLOSE

        //MainPanel
        val mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.X_AXIS)
        add(mainPanel)

        //JMenuBar menuBar
        val menuBar = createMenuBar()
        add(menuBar, BorderLayout.NORTH)

        //JLabel picLabel
        picLabel = JLabel()

        //JPanel picPanel
        val picPanel = JPanel()
        picPanel.background = Color.DARK_GRAY
        picPanel.layout = BorderLayout(10, 10)
        picPanel.add(picLabel, BorderLayout.CENTER)

        //JScrollPane scroller
        scroller = JScrollPane(picPanel)
        scroller.border = TitledBorder("Parent Panel")
        mainPanel.add(scroller, BorderLayout.CENTER)

        if (infoChunk != null) {
            panelInfo = createInfoPanel(infoChunk)
            mainPanel.add(panelInfo, BorderLayout.NORTH)
        }

        //JFrame
        updateTitle()
        scaledImage = scaleImage(origImage)
        updateImage(scaledImage, true)

        setLocationRelativeTo(null)
        isVisible = true

        SwingUtilities.invokeLater {
            Thread.sleep(250)
            minimumSize = size
        }
    }

    fun setUIFont(f: FontUIResource?) {
        val keys: Enumeration<*> = UIManager.getDefaults().keys()
        while (keys.hasMoreElements()) {
            val key = keys.nextElement()
            val value = UIManager.get(key)
            if (value is FontUIResource) UIManager.put(key, f)
        }
    }

    private fun updateImage(image: Image, doPack: Boolean) {
        picLabel.icon = ImageIcon(image)
        if (doPack) pack()
    }

    private fun updateTitle() {
        val titledBorder = TitledBorder("${file.name} (zoom x$zoomFactor)")
        titledBorder.titleJustification = TitledBorder.RIGHT
        scroller.border = titledBorder
    }

    private fun createInfoPanel(info: InfoChunk): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = EmptyBorder(10,10,10,10)
        panel.size.width = 500

        val title = JLabel("Image metadata:")
        title.font = title.font.deriveFont(title.font.style or Font.BOLD)
        panel.add(title, BorderLayout.NORTH)

        var row = 0

        panel.add(getLabelSeparator(), BorderLayout.NORTH)

        val subPanel = JPanel()
        subPanel.layout = GridBagLayout()
        subPanel.alignmentX = LEFT_ALIGNMENT
        subPanel.alignmentY = TOP_ALIGNMENT
        panel.add(subPanel, BorderLayout.NORTH)

        subPanel.add(JLabel("Info version:"), createGbc(0,row))
        subPanel.add(JLabel(info.infoVersion.toString()), createGbc(1,row++))

        subPanel.add(JLabel("Chunk count:"), createGbc(0,row))
        subPanel.add(JLabel(info.chunkCount.toString()), createGbc(1,row++))

        subPanel.add(getLabelSeparator(), createGbc(0,row++))

        subPanel.add(JLabel("Original Width:"), createGbc(0,row))
        subPanel.add(JLabel(info.originalWidth.toString()+" px"), createGbc(1,row++))

        subPanel.add(JLabel("Original Height:"), createGbc(0,row))
        subPanel.add(JLabel(info.originalHeight.toString()+" px"), createGbc(1,row++))

        if (info.pixelType != PixelType.Unspecified) {
            subPanel.add(JLabel("Colors:"), createGbc(0,row))
            subPanel.add(JLabel("${info.pixelType.colors}"), createGbc(1,row++))
        }

        subPanel.add(getLabelSeparator(), createGbc(0,row++))

        subPanel.add(JLabel("Pixel type:"), createGbc(0,row))
        subPanel.add(JLabel(info.pixelType.name), createGbc(1,row++))

        subPanel.add(JLabel("Palette type:"), createGbc(0,row))
        subPanel.add(JLabel(info.paletteType.name), createGbc(1,row++))

        subPanel.add(JLabel("Chipset:"), createGbc(0,row))
        subPanel.add(JLabel(info.chipset.name), createGbc(1,row++))

        subPanel.add(JLabel(""), createGbc(0,row, true))
        return panel
    }

    private fun createGbc(x: Int, y: Int, last: Boolean = false): GridBagConstraints {
        val gbc = GridBagConstraints()
        gbc.gridx = x
        gbc.gridy = y
        gbc.gridwidth = 1
        gbc.gridheight = 1
        gbc.anchor = if (x == 0) GridBagConstraints.WEST else GridBagConstraints.EAST
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.insets = Insets(3, 3, 0, 0)
        gbc.weightx = if (x == 0) 0.1 else 1.0
        if (last) gbc.weighty = 100.0
        return gbc
    }

    private fun getLabelSeparator(): JLabel {
        val labelSeparator = JLabel(" ")
        labelSeparator.font = labelSeparator.font.deriveFont(labelSeparator.font.size / 2f)
        return labelSeparator
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

            menuFile.add(JSeparator())

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
        if (zoomFactor == maxZoom) return
        zoomFactor = (zoomFactor + 1).coerceAtMost(maxZoom)
        scaledImage = scaleImage(origImage)
        updateImage(scaledImage, true)
        updateTitle()
    }

    private fun doZoomOut() {
        if (zoomFactor == 1) return
        zoomFactor = (zoomFactor - 1).coerceAtLeast(1)
        scaledImage = scaleImage(origImage)
        updateImage(scaledImage, true)
        updateTitle()
    }

    private fun doExit() {
        dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
        exitProcess(0)
    }

    private fun scaleImage(image: Image): Image {
        if (zoomFactor == 1) return image
        val zoomWidth = origImage.width * zoomFactor
        val zoomHeight = origImage.height * zoomFactor
        return image.getScaledInstance(zoomWidth, zoomHeight, Image.SCALE_FAST)
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