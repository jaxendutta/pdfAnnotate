package com.example.pdfAnnotate

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val LOGNAME = "pdf_image"
    private val FILENAME = "shannon1948.pdf"
    private val FILERESID = R.raw.shannon1948
    private var counter = CURRENT

    // manage the pages of the PDF, see below
    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var parcelFileDescriptor: ParcelFileDescriptor
    private var currentPage: PdfRenderer.Page? = null

    // custom ImageView class that captures strokes and draws them over the image
    private lateinit var pageImage: PDFimage


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val layout = findViewById<LinearLayout>(R.id.pdfLayout)
        val draw = findViewById<ImageButton>(R.id.draw_button)
        val erase = findViewById<ImageButton>(R.id.erase_button)
        val highlight = findViewById<ImageButton>(R.id.highlight_button)
        val prev = findViewById<ImageButton>(R.id.prev_button)
        val next = findViewById<ImageButton>(R.id.next_button)
        val filename = findViewById<TextView>(R.id.file_name)
        val undoStack = findViewById<ImageButton>(R.id.undo_button)
        val redoStack = findViewById<ImageButton>(R.id.redo_button)
        val pageNumber =  findViewById<TextView>(R.id.page)
        layout.isEnabled = true
        filename.text = FILENAME
        filename.textSize = 15f
        pageNumber.textSize = 12f

        prev.setOnClickListener{
            if(currentPage?.index != 0 ) {
                currentPage?.index?.minus(1)?.let { it1 -> showPage(it1) }
                pageNumber.text = (currentPage?.index?.plus(1).toString()) + "/" + showTotal()
                CURRENT--
            }
        }

        next.setOnClickListener{
            if(currentPage?.index != showTotal().toInt() - 1 ) {
                currentPage?.index?.plus(1)?.let { it1 -> showPage(it1) }
                pageNumber.text = (currentPage?.index?.plus(1).toString()) + "/" + showTotal()
                CURRENT++
            }
        }

        draw.setOnClickListener{
            Tool = "pen"
        }
        highlight.setOnClickListener{
            Tool = "highlight"
        }
        erase.setOnClickListener{
            Tool = "eraser"
        }
        undoStack.setOnClickListener{
            if(!undo.isEmpty()){
                undo.peek().visibility = !undo.peek().visibility
                redo.push(undo.pop())
            }
        }
        redoStack.setOnClickListener{
            if(!redo.isEmpty()){
                redo.peek().visibility = !redo.peek().visibility
                undo.push(redo.pop())
            }
        }

        pageImage = PDFimage(this)
        layout.addView(pageImage)
        pageImage.minimumWidth = 1000
        pageImage.minimumHeight = 2000
        try {
            openRenderer(this)
            pageNumber.text = (CURRENT.plus(1).toString()) + "/" + showTotal()
            showPage(counter)
        } catch (exception: IOException) {
            Log.d(LOGNAME, "Error opening PDF")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        with (outState) {
            putInt("COUNTER", counter)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(inState: Bundle) {
        super.onRestoreInstanceState(inState)
        with (inState) {
            counter= getInt("COUNTER")
            showPage(counter)
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            closeRenderer()
        } catch (ex: IOException) {
            Log.d(LOGNAME, "Unable to close PDF renderer")
        }
    }

    @Throws(IOException::class)
    private fun openRenderer(context: Context) {
        val file = File(context.cacheDir, FILENAME)
        if (!file.exists()) {
            val asset = this.resources.openRawResource(FILERESID)
            val output = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var size: Int
            while (asset.read(buffer).also { size = it } != -1) {
                output.write(buffer, 0, size)
            }
            asset.close()
            output.close()
        }
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        pdfRenderer = PdfRenderer(parcelFileDescriptor)
    }

    @Throws(IOException::class)
    private fun closeRenderer() {
        if (null != currentPage) {
            currentPage?.close()
        }
        pdfRenderer.close()
        parcelFileDescriptor.close()
    }

    private fun showTotal():String{
        Total = pdfRenderer.pageCount
        return pdfRenderer.pageCount.toString()
    }


    private fun showPage(index: Int) {
        if (pdfRenderer.pageCount <= index) return
        currentPage?.close()
        currentPage = pdfRenderer.openPage(index)
        if (currentPage != null) {
            val bitmap = Bitmap.createBitmap(currentPage!!.width, currentPage!!.height, Bitmap.Config.ARGB_8888)
            currentPage!!.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            pageImage.setImage(bitmap, currentPage!!.index)
        }
    }
}