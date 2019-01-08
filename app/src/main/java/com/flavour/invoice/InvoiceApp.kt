package com.flavour.invoice

import android.app.Application
import io.realm.Realm

open class InvoiceApp: Application(){
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}