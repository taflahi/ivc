package com.flavour.invoice.feature.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import com.flavour.invoice.R
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.flavour.invoice.feature.business.BusinessActivity
import com.flavour.invoice.feature.invoice.InvoiceActivity
import com.flavour.invoice.model.Business
import com.flavour.invoice.model.Invoice
import com.flavour.invoice.storage.Preference
import com.google.android.gms.ads.AdRequest
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MainAdapter.OnClickListener {

    var currency: String = ""
    var myBusiness: Business? = null
    var invoices: MutableList<Invoice> = emptyList<Invoice>().toMutableList()
    lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        realm = Realm.getDefaultInstance()

        setupMenu()
        setupButton()
        setupAds()
    }

    override fun onStart() {
        super.onStart()
        setupData()
    }

    fun setupData(){
        val savedCurrency = Preference(this).getCurrency()
        if (savedCurrency != null){
            currency = savedCurrency
        } else {
            currency = "$"
        }

        myBusiness = Preference(this).getMyBusiness()

        realm.executeTransaction {
            val invResults = it.where(Invoice::class.java).sort("id", Sort.DESCENDING).findAll()
            invoices = emptyList<Invoice>().toMutableList()
            invResults.forEach { inv ->
                invoices.add(inv)
            }
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)

            val mainAdapter = MainAdapter(invoices.toList(), currency)
            mainAdapter.listener = this@MainActivity

            adapter = mainAdapter
        }
    }

    fun setupMenu(){
        menuButton.setOnClickListener {
            val popupMenu = PopupMenu(this@MainActivity, menuButton)
            popupMenu.menuInflater.inflate(R.menu.main_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener {
                if(it.title.equals(getString(R.string.home_menu_edit_business))){
                    Intent(this@MainActivity, BusinessActivity::class.java).also {
                        startActivity(it)
                    }
                }
                true
            }

            popupMenu.show()
        }
    }

    fun setupButton(){
        newButton.setOnClickListener {
            Intent(this, InvoiceActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    override fun onClick(invoice: Invoice) {
        Intent(this, InvoiceActivity::class.java).putExtra("INVOICE_ID", invoice.id.toString()).also {
            startActivity(it)
        }
    }

    fun setupAds(){
//        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111" // test
//        adView.adUnitId = "ca-app-pub-4510440310817876/1258452022" // production
        adView.loadAd(AdRequest.Builder().build())
    }

}
