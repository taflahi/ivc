package com.flavour.invoice.feature.main

import android.os.Bundle
import android.util.Log
import com.flavour.invoice.R
import androidx.appcompat.app.AppCompatActivity
import com.flavour.invoice.storage.Preference

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currency = Preference(this).getCurrency()
        val myBusiness = Preference(this).getMyBusiness()

        Log.e("CURR", currency)
        Log.e("BUSS", myBusiness.toString())
    }

}
