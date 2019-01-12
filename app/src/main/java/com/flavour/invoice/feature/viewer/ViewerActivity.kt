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

class ViewerActivity : AppCompatActivity() {

    var pdfRenderer: PdfRenderer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewer)

        setupButton()
        setupPdf()
        openPdf()
    }

    fun setupButton(){
        backButton.setOnClickListener {
            finish()
        }
    }

    fun setupPdf(){
        intent.extras?.apply {
            val pdf = getString("PDF", "")

            if(pdf.isNotBlank()){


                val file = File(cacheDir, pdf)
                val parcelDescriptor =  ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

                parcelDescriptor.apply {
                    pdfRenderer = PdfRenderer(this)
                }
            }
        }
    }

    fun openPdf(){
        pdfRenderer?.apply {
            val currentPage = openPage(0)
            val bitmap = Bitmap.createBitmap(currentPage.width, currentPage.height, Bitmap.Config.ARGB_8888)

            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            pdfImage.setImageBitmap(bitmap)
        }
    }
}
