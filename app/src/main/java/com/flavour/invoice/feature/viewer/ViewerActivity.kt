package com.flavour.invoice.feature.viewer

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import com.flavour.invoice.R
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_viewer.*
import java.io.File
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.flavour.invoice.html.Generator
import com.flavour.invoice.html.PdfPrint
import com.flavour.invoice.storage.Preference
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.android.material.snackbar.Snackbar
import java.io.FileInputStream
import java.io.FileOutputStream

class ViewerActivity : AppCompatActivity(), PdfPrint.OnFinishedAll, ActivityCompat.OnRequestPermissionsResultCallback, RewardedVideoAdListener {
    var pdfRenderer: PdfRenderer? = null
    var html: String? = null
    var number: String? = null
    private lateinit var mRewardedVideoAd: RewardedVideoAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewer)

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = this
        loadRewardedVideoAd()

        setupButton()
        setupData()
        startWebView()
    }

    override fun onResume() {
        super.onResume()

        mRewardedVideoAd.resume(this)
    }

    override fun onPause() {
        super.onPause()

        mRewardedVideoAd.pause(this)
    }

    fun setupButton(){
        backButton.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {
            processSave()
        }

//        menuButton.setOnClickListener {
//            val popupMenu = PopupMenu(this@ViewerActivity, menuButton)
//            popupMenu.menuInflater.inflate(R.menu.viewer_menu, popupMenu.menu)
//
//            popupMenu.setOnMenuItemClickListener {
//                if(it.title.equals(getString(R.string.preview_save))){
//                    downloadPermission()
//                } else if(it.title.equals(getString(R.string.preview_send))){
//                    sendPdf()
//                }
//                true
//            }
//
//            popupMenu.show()
//        }
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

    fun setupPdf(){
        number?.apply {
            val file = File(filesDir, this + ".pdf")
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
        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()

        number?.apply {
            val file = File(filesDir.toURI())

            val pdfPrint = PdfPrint(attributes, this@ViewerActivity, this@ViewerActivity)
            val printAdapter = pdfWebView.createPrintDocumentAdapter(jobName)

            pdfPrint.printNew(printAdapter, file, this + ".pdf", filesDir.absolutePath)
        }
    }

    override fun finished() {
        setupPdf()
        openPdf()
    }

    fun downloadPermission(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                downloadPdf()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        } else {
            downloadPdf()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v("GRANTED","Permission: "+permissions[0]+ "was "+grantResults[0]);
            downloadPdf()
        }
    }

    fun downloadPdf(){
        number?.apply {
            val currentFile = File(filesDir, this + ".pdf")

            val byteArray = currentFile.readBytes()
            Log.e("ARRAY", byteArray.size.toString())
            val targetFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), this + ".pdf")
            targetFile.writeBytes(byteArray)
            Log.e("LENGTH", targetFile.length().toString())

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.addCompletedDownload(targetFile.name, targetFile.name, true, "application/pdf", targetFile.absolutePath, targetFile.length(), true)

            val snackbar = Snackbar.make(pdfImage, "Invoice is saved to your Downloads folder", Snackbar.LENGTH_SHORT)
            snackbar.show()
        }
    }

    fun processSave(){
        val saveNumber = Preference(this).getSaveNumber()
        if(saveNumber % 5 == 4){
            showRewardedVideo()
        } else {
            downloadPermission()
        }

        Preference(this).incrementSaveNumber()
    }

    private fun showRewardedVideo() {
        if (mRewardedVideoAd.isLoaded) {
            mRewardedVideoAd.show()
        }
    }

    private fun loadRewardedVideoAd() {
        if (!mRewardedVideoAd.isLoaded) {
            mRewardedVideoAd.loadAd("ca-app-pub-4510440310817876/4328125038", //prod ca-app-pub-4510440310817876/4328125038
                AdRequest.Builder().build()) //test ca-app-pub-3940256099942544/5224354917
        }
    }

    override fun onRewardedVideoAdClosed() {
        loadRewardedVideoAd()
    }

    override fun onRewardedVideoAdLeftApplication() {

    }

    override fun onRewardedVideoAdLoaded() {

    }

    override fun onRewardedVideoAdOpened() {

    }

    override fun onRewardedVideoCompleted() {

    }

    override fun onRewarded(p0: RewardItem?) {
        // go to save pdf
        downloadPermission()
    }

    override fun onRewardedVideoStarted() {

    }

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {

    }
}
