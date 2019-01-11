package com.flavour.invoice.feature.charge

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.flavour.invoice.R
import androidx.appcompat.app.AppCompatActivity
import com.flavour.invoice.model.InvoiceCharge
import com.google.gson.GsonBuilder
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_charge.*

class ChargeActivity : AppCompatActivity() {

    var chargeId: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charge)

        chargeId = intent?.extras?.getInt("CHARGE_ID", 0)

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
            deleteButton.visibility = if(it and (chargeId != 0) and (chargeId != null)) View.VISIBLE else View.GONE
        }

        if(chargeId != null && chargeId != 0){
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                val itemResult = it.where(InvoiceCharge::class.java).equalTo("id", chargeId).findFirst()
                itemResult?.apply {
                    nameEditText.setText(name)
                    valueEditText.setText(value.toString())
                }
            }
        }
    }

    fun setupButton(){
        saveButton.setOnClickListener {
            setResult(Activity.RESULT_OK, Intent().putExtra("NAME", nameEditText.text.toString()).putExtra("VALUE", valueEditText.text.toString().toDouble()).putExtra("ID", chargeId))
            finish()
        }

        backButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        deleteButton.setOnClickListener {
            showDeletePopup()
        }
    }

    fun showDeletePopup(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Remove this item?")
            .setMessage("This action is irreversible")
            .setPositiveButton("YES", DialogInterface.OnClickListener { dialogInterface, i ->
                setResult(Activity.RESULT_FIRST_USER, Intent().putExtra("ID", chargeId))
                finish()
            }).setNegativeButton("NO", DialogInterface.OnClickListener { dialogInterface, i ->  }).show()
    }
}
