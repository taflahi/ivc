package com.flavour.invoice.feature.invoice

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.flavour.invoice.R
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.flavour.invoice.feature.billing.BillingDetailActivity
import com.flavour.invoice.feature.charge.ChargeActivity
import com.flavour.invoice.feature.item.ItemActivity
import com.flavour.invoice.model.Business
import com.flavour.invoice.model.Invoice
import com.flavour.invoice.model.InvoiceCharge
import com.flavour.invoice.model.InvoiceItem
import com.flavour.invoice.storage.Preference
import io.realm.Realm
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_invoice.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.text.DecimalFormat

class InvoiceActivity : AppCompatActivity(), InvoiceAdapter.OnClickListener {

    val REQ_ITEM = 1
    val REQ_CHARGE = 2
    val REQ_BILLING = 3
    lateinit var invoice: Invoice
    lateinit var realm: Realm
    lateinit var currency: String
    var itemList: MutableList<InvoiceItem> = emptyList<InvoiceItem>().toMutableList()
    var chargeList: MutableList<InvoiceCharge> = emptyList<InvoiceCharge>().toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice)

        realm = Realm.getDefaultInstance()
        setupNewData()
        setupButton()
    }

    override fun onStart() {
        super.onStart()
        setupData()
        setupViews()
    }

    fun setupNewData(){
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
            val invoiceResult = realm.where(Invoice::class.java).equalTo("id", data.toInt()).findFirst()
            invoiceResult?.apply {
                invoice = this
            }
        }
    }

    fun setupData(){
        realm.executeTransaction {
            val invoiceResult = it.where(Invoice::class.java).equalTo("id", invoice.id).findFirst()
            itemList = emptyList<InvoiceItem>().toMutableList()
            chargeList = emptyList<InvoiceCharge>().toMutableList()
            invoiceResult?.apply {
                invoice = this
                total = 0.0

                this.items?.forEach {
                    total += (it.value - it.discountValue)
                    itemList.add(it)
                }

                this.charges?.forEach {
                    total += it.value
                    chargeList.add(it)
                }
            }
        }

        currency = Preference(this).getCurrency().toString()

        setupRecyclerView()
    }

    fun setupRecyclerView(){
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@InvoiceActivity)

            val mainAdapter = InvoiceAdapter(itemList.toList(), chargeList.toList(), currency)
            mainAdapter.listener = this@InvoiceActivity

            adapter = mainAdapter
        }
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

    override fun onClick(itemId: Int, chargeId: Int) {
        if(itemId != 0){
            Intent(this, ItemActivity::class.java)
                .putExtra("ITEM_ID", itemId).also {
                    startActivityForResult(it, REQ_ITEM)
                }
        } else if(chargeId != 0){
            Intent(this, ChargeActivity::class.java)
                .putExtra("CHARGE_ID", chargeId).also {
                    startActivityForResult(it, REQ_CHARGE)
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == REQ_BILLING){
                backFromBilling(data)
            } else if(requestCode == REQ_ITEM){
                backFromItem(data)
            } else if(requestCode == REQ_CHARGE){
                backFromCharges(data)
            }
        } else if(resultCode == Activity.RESULT_FIRST_USER){ // delete
            if(requestCode == REQ_ITEM){
                deleteFromItem(data)
            } else if(requestCode == REQ_CHARGE){
                deleteFromCharges(data)
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

    fun deleteFromItem(data: Intent?){
        data?.extras?.apply {
            val id = getInt("ID", 0)
            if(id != 0){
                realm.executeTransaction {
                    it.where(InvoiceItem::class.java).equalTo("id", id).findFirst()?.deleteFromRealm()
                }
            }
        }
    }

    fun backFromItem(data: Intent?){
        data?.extras?.apply {
            val name = getString("NAME", "")
            val price = getDouble("PRICE", 0.0)
            val discount = getDouble("DISCOUNT", 0.0)
            val id = getInt("ID", 0)

            val invResult = realm.where((Invoice::class.java)).equalTo("id", invoice.id).findFirst()
            invResult?.apply {
                realm.executeTransaction {
                    if(id == 0){
                        val currentId = realm.where(InvoiceItem::class.java).max("id")
                        var nextId = 0
                        if(currentId == null){
                            nextId = 1
                        } else {
                            nextId = currentId.toInt() + 1
                        }

                        val invoiceItem = InvoiceItem(nextId, name,  price, discount)
                        if(items == null){
                            items = RealmList<InvoiceItem>()
                        }

                        items?.add(invoiceItem)
                    } else {
                        it.copyToRealmOrUpdate(InvoiceItem(id, name,  price, discount))
                    }
                }
            }
        }
    }

    fun deleteFromCharges(data: Intent?){
        data?.extras?.apply {
            val id = getInt("ID", 0)
            if(id != 0){
                realm.executeTransaction {
                    it.where(InvoiceCharge::class.java).equalTo("id", id).findFirst()?.deleteFromRealm()
                }
            }
        }
    }

    fun backFromCharges(data: Intent?){
        data?.extras?.apply {
            val name = getString("NAME", "")
            val price = getDouble("VALUE", 0.0)
            val id = getInt("ID", 0)

            val invResult = realm.where((Invoice::class.java)).equalTo("id", invoice.id).findFirst()
            invResult?.apply {
                realm.executeTransaction {
                    if(id == 0){
                        val currentId = realm.where(InvoiceCharge::class.java).max("id")
                        var nextId = 0
                        if(currentId == null){
                            nextId = 1
                        } else {
                            nextId = currentId.toInt() + 1
                        }

                        val invoiceItem = InvoiceCharge(nextId, name,  price)
                        if(charges == null){
                            charges = RealmList<InvoiceCharge>()
                        }

                        charges?.add(invoiceItem)
                    } else {
                        it.copyToRealmOrUpdate(InvoiceCharge(id, name,  price))
                    }
                }
            }
        }
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
