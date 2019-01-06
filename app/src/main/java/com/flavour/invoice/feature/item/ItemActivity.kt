package com.flavour.invoice.feature.item

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.flavour.invoice.R
import androidx.appcompat.app.AppCompatActivity
import com.flavour.invoice.model.InvoiceItem
import com.google.gson.GsonBuilder
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.functions.Function3
import kotlinx.android.synthetic.main.activity_item.*

class ItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        setupEditText()
        setupButton()
    }

    @SuppressLint("CheckResult")
    fun setupEditText(){
        Observable.combineLatest(RxTextView.textChanges(nameEditText),
            RxTextView.textChanges(valueEditText),
            RxTextView.textChanges(discountEditText), Function3<CharSequence, CharSequence, CharSequence, Boolean> { t1, t2, t3 ->
                t1.isNotBlank() and t2.isNotBlank() and t3.isNotBlank()
            }).subscribe {
            saveButton.visibility = if(it) View.VISIBLE else View.GONE
        }
    }

    fun setupButton(){
        saveButton.setOnClickListener {
            val discount = if(discountEditText.text.toString().isBlank()) 0.0 else discountEditText.text.toString().toDouble()
            if(discount > valueEditText.text.toString().toDouble()){
                showInvalidDiscount()
            } else {
                val item = InvoiceItem(nameEditText.text.toString(), valueEditText.text.toString().toDouble(), discount)

                val gson = GsonBuilder().create()
                val itemString = gson.toJson(item, InvoiceItem::class.java)

                setResult(Activity.RESULT_OK, Intent().putExtra("ITEM", itemString))
                finish()
            }
        }

        backButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    fun showInvalidDiscount(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Invalid Discount")
            .setMessage("Discount Cannot Bigger than Price")
            .setPositiveButton("YES", DialogInterface.OnClickListener { dialogInterface, i ->

            }).show()
    }
}
