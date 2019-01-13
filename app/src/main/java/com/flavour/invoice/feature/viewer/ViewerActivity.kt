package com.flavour.invoice.feature.viewer

import android.graphics.Bitmap
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import com.flavour.invoice.R
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_viewer.*
import java.io.File
import android.graphics.pdf.PdfRenderer
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.flavour.invoice.html.Generator
import com.flavour.invoice.html.PdfPrint

class ViewerActivity : AppCompatActivity(), PdfPrint.OnFinishedAll {

    var pdfRenderer: PdfRenderer? = null
    var html: String? = null
    var number: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewer)

        setupButton()
        setupData()
        startWebView()
    }

    fun setupButton(){
        backButton.setOnClickListener {
            finish()
        }
    }

    fun setupData(){
        intent.extras?.apply {
            html = getString("HTML", "")
            number = getString("NUMBER", "")
        }
    }

    fun startWebView(){
        html?.apply {
            pdfWebView.settings.useWideViewPort = true
            pdfWebView.settings.loadWithOverviewMode = true
            pdfWebView.webViewClient = object : WebViewClient(){
                override fun onPageFinished(view: WebView?, url: String?) {
                    // save the screen
                    createPdf()
                }
            }
            pdfWebView.loadData(html, "text/html", "UTF-8")
        }
    }


    fun saveAndLoadPdf(){

    }

    fun setupPdf(){
        number?.apply {
            val file = File(cacheDir, this + ".pdf")
            val parcelDescriptor =  ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

            parcelDescriptor.apply {
                pdfRenderer = PdfRenderer(this)
            }
        }
    }

    fun openPdf(){
        pdfRenderer?.apply {
            val currentPage = openPage(0)
            val bitmap = Bitmap.createBitmap(currentPage.width, currentPage.height, Bitmap.Config.ARGB_8888)

            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            pdfImage.setImageBitmap(bitmap)

            pdfWebView.visibility = View.GONE
            generateTextView.visibility = View.GONE
            pdfImage.visibility = View.VISIBLE
        }
    }

    fun createPdf(){
        val jobName = "Invoice Free Gen"
        var attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()

        number?.apply {
            val file = File(cacheDir.toURI())

            val pdfPrint = PdfPrint(attributes, this@ViewerActivity, this@ViewerActivity)
            val printAdapter = pdfWebView.createPrintDocumentAdapter(jobName)

            pdfPrint.printNew(printAdapter, file, this + ".pdf", cacheDir.absolutePath)
        }
    }

    override fun finished() {
        setupPdf()
        openPdf()
    }
}
