package com.flavour.invoice.feature.invoice

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.flavour.invoice.R
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.flavour.invoice.feature.billing.BillingDetailActivity
import com.flavour.invoice.feature.charge.ChargeActivity
import com.flavour.invoice.feature.item.ItemActivity
import com.flavour.invoice.model.Business
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
    val REQ_BILLING = 3
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
                val dateFormat = DateTimeFormat.forPattern("EEEE, dd MMMM yyyy")
                invoice.dateTime = dateFormat.print(DateTime.now().withTime(0,0,0,0))
                invoice.dueDateTime = dateFormat.print(DateTime.now().withTime(0,0,0,0).plusDays(2))
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
        toTextView.text = "To: "
        invoice.billTo?.apply {
            if(name!!.isNotBlank()){
                toTextView.text = "To: " + invoice.billTo?.name
            }
        }
        numberTextView.text = invoice.number
        dateTextView.text = invoice.dateTime
        paidTextView.text = if(invoice.isPaid) "Paid" else "Not Paid"

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

        menuButton.setOnClickListener {
            val popupMenu = PopupMenu(this@InvoiceActivity, menuButton)
            popupMenu.menuInflater.inflate(R.menu.invoice_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener {
                if(it.title.equals(getString(R.string.invoice_delete))){
                    showDeletePopup()
                } else if(it.title.equals(getString(R.string.invoice_toggle_paid))){
                    togglePaid()
                }
                true
            }

            popupMenu.show()
        }

        invoiceDescriptionBox.setOnClickListener {
            Intent(this@InvoiceActivity, BillingDetailActivity::class.java)
                .putExtra("NAME", invoice.billTo?.name)
                .putExtra("ADDRESS", invoice.billTo?.address)
                .putExtra("EMAIL", invoice.billTo?.email)
                .putExtra("PHONE", invoice.billTo?.phone)
                .putExtra("DATE", invoice.dateTime)
                .putExtra("DUEDATE", invoice.dueDateTime)
                .putExtra("NUMBER", invoice.number)
                .also {
                startActivityForResult(it, REQ_BILLING)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == REQ_BILLING){
                backFromBilling(data)
            }
        }
    }

    fun togglePaid(){
        val invResult = realm.where((Invoice::class.java)).equalTo("id", invoice.id).findFirst()
        invResult?.apply {
            realm.executeTransaction {
                invResult.isPaid = !invResult.isPaid
            }
            invoice = invResult
        }
        setupViews()
    }

    fun backFromBilling(data: Intent?){
        data?.extras?.apply {
            val name = getString("NAME", "")
            val address = getString("ADDRESS", "")
            val email = getString("EMAIL", "")
            val phone = getString("PHONE", "")
            val date = getString("DATE", "")
            val dueDate = getString("DUEDATE", "")
            val number = getString("NUMBER", "")

            val invResult = realm.where((Invoice::class.java)).equalTo("id", invoice.id).findFirst()

            invResult?.apply {
                realm.executeTransaction {
                    billTo = it.copyToRealm(Business(name, address, email, phone))
                    dateTime = date
                    dueDateTime = dueDate
                    this.number = number
                }

                invoice = invResult
            }
        }
        setupViews()
    }

    fun showDeletePopup(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Are you sure you want to delete this invoice?")
            .setMessage("This action is irreversible")
            .setPositiveButton("YES", DialogInterface.OnClickListener { dialogInterface, i ->
                realm.executeTransaction {
                    val theInvoice = it.where(Invoice::class.java).equalTo("id", invoice.id).findFirst()
                    theInvoice?.deleteFromRealm()
                }
                finish()
            }).setNegativeButton("NO", DialogInterface.OnClickListener { dialogInterface, i ->

            }).show()
    }
}
