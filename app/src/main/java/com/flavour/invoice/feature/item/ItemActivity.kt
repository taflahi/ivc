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
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.functions.Function3
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_item.*

class ItemActivity : AppCompatActivity() {

    var itemId: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        itemId = intent?.extras?.getInt("ITEM_ID", 0)

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
            deleteButton.visibility = if(it and (itemId != 0) and (itemId != null)) View.VISIBLE else View.GONE
        }

        if(itemId != null && itemId != 0){
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                val itemResult = it.where(InvoiceItem::class.java).equalTo("id", itemId).findFirst()
                itemResult?.apply {
                    nameEditText.setText(name)
                    valueEditText.setText(value.toString())
                    discountEditText.setText(discountValue.toString())
                }
            }
        }
    }

    fun setupButton(){
        saveButton.setOnClickListener {
            val discount = if(discountEditText.text.toString().isBlank()) 0.0 else discountEditText.text.toString().toDouble()
            if(discount > valueEditText.text.toString().toDouble()){
                showInvalidDiscount()
            } else {
                setResult(Activity.RESULT_OK, Intent().putExtra("NAME", nameEditText.text.toString()).putExtra("PRICE", valueEditText.text.toString().toDouble()).putExtra("DISCOUNT", discountEditText.text.toString().toDouble()).putExtra("ID", itemId))
                finish()
            }
        }

        backButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        deleteButton.setOnClickListener {
            showDeletePopup()
        }
    }

    fun showInvalidDiscount(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Invalid Discount")
            .setMessage("Discount Cannot Bigger than Price")
            .setPositiveButton("YES", DialogInterface.OnClickListener { dialogInterface, i ->

            }).show()
    }

    fun showDeletePopup(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Remove this item?")
            .setMessage("This action is irreversible")
            .setPositiveButton("YES", DialogInterface.OnClickListener { dialogInterface, i ->
                setResult(Activity.RESULT_FIRST_USER, Intent().putExtra("ID", itemId))
                finish()
            }).setNegativeButton("NO", DialogInterface.OnClickListener { dialogInterface, i ->  }).show()
    }
}
