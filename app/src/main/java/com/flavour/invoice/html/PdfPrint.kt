package com.flavour.invoice.html

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.util.Log
import com.android.dx.stock.ProxyBuilder
import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

open class PdfPrint(printAttributes: PrintAttributes, context: Context, onFinishedAll: OnFinishedAll){

    lateinit var printAttributes : PrintAttributes
    lateinit var cacheFolder: File
    lateinit var context: Context
    lateinit var onFinishedAll: OnFinishedAll

    init {
        this.printAttributes = printAttributes
        cacheFolder = File(context.filesDir.absolutePath + "/etemp/")
        this.onFinishedAll = onFinishedAll
    }

    @Throws(IOException::class)
    fun getWriteResultCallback(
        invocationHandler: InvocationHandler,
        dexCacheDir: File
    ): PrintDocumentAdapter.WriteResultCallback {
        return ProxyBuilder.forClass(PrintDocumentAdapter.WriteResultCallback::class.java)
            .dexCache(dexCacheDir)
            .handler(invocationHandler)
            .build()
    }

    @Throws(IOException::class)
    fun getLayoutResultCallback(
        invocationHandler: InvocationHandler,
        dexCacheDir: File
    ): PrintDocumentAdapter.LayoutResultCallback {
        return ProxyBuilder.forClass(PrintDocumentAdapter.LayoutResultCallback::class.java)
            .dexCache(dexCacheDir)
            .handler(invocationHandler)
            .build()
    }

    fun printNew(printAdapter: PrintDocumentAdapter, path: File, fileName: String, fileNamenew: String) {
        try {

            printAdapter.onStart()
            printAdapter.onLayout(
                null,
                printAttributes,
                CancellationSignal(),
                getLayoutResultCallback(object : InvocationHandler {
                    @Throws(Throwable::class)
                    override fun invoke(o: Any, method: Method, objects: Array<Any>): Any? {

                        if (method.getName().equals("onLayoutFinished")) {
                            onLayoutSuccess(printAdapter, path, fileName, fileNamenew)
                        } else {
                            Log.e("PFD", "Layout failed")

                        }
                        return null
                    }
                }, File(fileNamenew)),
                Bundle()
            )


        } catch (e: Exception) {
            e.printStackTrace()

        }

    }

    @Throws(IOException::class)
    private fun onLayoutSuccess(printAdapter: PrintDocumentAdapter, path: File, fileName: String, filenamenew: String) {
        val callback = getWriteResultCallback(object : InvocationHandler {
            @Throws(Throwable::class)
            override fun invoke(o: Any, method: Method, objects: Array<Any>): Any? {
                if (method.getName().equals("onWriteFinished")) {

                    print("hello")
                    onFinishedAll.finished()
                } else {
                    Log.e("PFD", "Layout failed")

                }
                return null
            }
        }, File(filenamenew))
        printAdapter.onWrite(
            arrayOf(PageRange.ALL_PAGES),
            getOutputFile(path, fileName),
            CancellationSignal(),
            callback
        )
    }

    private fun getOutputFile(path: File, fileName: String): ParcelFileDescriptor? {
        if (!path.exists()) {
            path.mkdirs()
        }
        val file = File(path, fileName)
        try {
            file.createNewFile()
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE)
        } catch (e: Exception) {
            Log.e("PFD", "Failed to open ParcelFileDescriptor", e)
        }

        return null
    }

    interface OnFinishedAll{
        fun finished()
    }
}