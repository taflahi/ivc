package com.flavour.invoice.feature.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import com.flavour.invoice.R
import androidx.appcompat.app.AppCompatActivity
import com.flavour.invoice.feature.business.BusinessActivity
import com.flavour.invoice.feature.invoice.InvoiceActivity
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

        setupMenu()
        setupButton()
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

}
