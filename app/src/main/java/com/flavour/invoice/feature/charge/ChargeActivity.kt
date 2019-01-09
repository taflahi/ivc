package com.flavour.invoice.feature.charge

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.flavour.invoice.R
import androidx.appcompat.app.AppCompatActivity
import com.flavour.invoice.model.InvoiceCharge
import com.google.gson.GsonBuilder
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.activity_charge.*

class ChargeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charge)

        setupEditText()
        setupButton()
    }

    @SuppressLint("CheckResult")
    fun setupEditText(){
        Observable.combineLatest(RxTextView.textChanges(nameEditText),
            RxTextView.textChanges(valueEditText), BiFunction<CharSequence, CharSequence, Boolean> { t1, t2 ->
                t1.isNotBlank() and t2.isNotBlank()
            }).subscribe {
            saveButton.visibility = if(it) View.VISIBLE else View.GONE
        }
    }

    fun setupButton(){
        saveButton.setOnClickListener {
//            val item = InvoiceCharge(nameEditText.text.toString(), valueEditText.text.toString().toDouble())
//
//            val gson = GsonBuilder().create()
//            val itemString = gson.toJson(item, InvoiceCharge::class.java)
//
//            setResult(Activity.RESULT_OK, Intent().putExtra("CHARGE", itemString))
            finish()
        }

        backButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}
