package com.flavour.invoice.feature.invoice

import android.content.Intent
import android.os.Bundle
import com.flavour.invoice.R
import androidx.appcompat.app.AppCompatActivity
import com.flavour.invoice.feature.charge.ChargeActivity
import com.flavour.invoice.feature.item.ItemActivity
import com.flavour.invoice.model.Invoice
import com.flavour.invoice.storage.Preference
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_invoice.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.text.DecimalFormat

class InvoiceActivity : AppCompatActivity() {
    val REQ_ITEM = 1
    val REQ_CHARGE = 2
    lateinit var invoice: Invoice
    lateinit var realm: Realm
    lateinit var currency: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice)

        setupButton()
        setupData()
        setupViews()
    }

    fun setupData(){
        realm = Realm.getDefaultInstance()
        val data = intent?.extras?.getString("INVOICE_ID", "")
        if(data.isNullOrBlank()){
            realm.executeTransaction {
                val currentId = realm.where(Invoice::class.java).max("id")
                var nextId = 0
                if(currentId == null){
                    nextId = 1
                } else {
                    nextId = currentId.toInt() + 1
                }

                invoice = Invoice(id = nextId)
                invoice.number = "INV" + nextId.toString()
                invoice.dateTime = DateTime.now().withTime(0,0,0,0)
                invoice.dueDateTime = DateTime.now().withTime(0,0,0,0).plusDays(2)
                it.insert(invoice)
            }
        } else {
            realm.executeTransaction {
                val invoiceResult = it.where(Invoice::class.java).equalTo("id", data.toInt()).findFirst()
                invoiceResult?.apply {
                    invoice = this
                }
            }
        }

        currency = Preference(this).getCurrency().toString()
    }

    fun setupViews(){
        toTextView.text = "To: " + invoice.billTo?.name
        numberTextView.text = invoice.number

        val dateFormat = DateTimeFormat.forPattern("EEEE, dd MMMM yyyy")
        dateTextView.text = dateFormat.print(invoice.dateTime)

        val decimalFormat = DecimalFormat()
        totalTextView.text = currency + " " + decimalFormat.format(invoice.total)
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
