package com.flavour.invoice.feature.invoice

import android.content.Intent
import android.os.Bundle
import com.flavour.invoice.R
import androidx.appcompat.app.AppCompatActivity
import com.flavour.invoice.feature.charge.ChargeActivity
import com.flavour.invoice.feature.item.ItemActivity
import kotlinx.android.synthetic.main.activity_invoice.*

class InvoiceActivity : AppCompatActivity() {
    val REQ_ITEM = 1
    val REQ_CHARGE = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice)

        setupButton()
    }

    fun setupButton(){
        itemButton.setOnClickListener {
            Intent(this@InvoiceActivity, ItemActivity::class.java).also {
                startActivityForResult(it, REQ_ITEM)
            }
        }

        chargeButton.setOnClickListener {
            Intent(this@InvoiceActivity, ChargeActivity::class.java).also {
                startActivityForResult(it, REQ_CHARGE)
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
