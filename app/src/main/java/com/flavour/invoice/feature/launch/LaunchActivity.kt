package com.flavour.invoice.feature.launch

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.flavour.invoice.R
import com.flavour.invoice.constants.CURRENCIES
import com.flavour.invoice.feature.main.MainActivity
import com.flavour.invoice.model.Business
import com.flavour.invoice.storage.Preference
import com.jakewharton.rxbinding2.widget.RxTextView
import kotlinx.android.synthetic.main.activity_launch.*

class LaunchActivity : AppCompatActivity(), CurrencyAdapter.OnClickListener {

    var businessName = ""
    var address = ""
    var email = ""
    var phoneNumber = ""
    var currency = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        setupEditTexts()
        setupRecyclerView()
        setupDirection()
    }

    @SuppressLint("CheckResult")
    fun setupEditTexts(){
        RxTextView.textChanges(nameEditText).subscribe {
            continueButton.isEnabled = it.isNotBlank()
        }

        RxTextView.textChanges(addressEditText).subscribe {
            continueButton.isEnabled = it.isNotBlank()
        }

        RxTextView.textChanges(emailEditText).subscribe {
            continueButton.isEnabled = it.isNotBlank()
        }

        RxTextView.textChanges(phoneEditText).subscribe {
            continueButton.isEnabled = it.isNotBlank()
        }

        RxTextView.textChanges(currencyButton).subscribe {
            continueButton.isEnabled = (it != getString(R.string.launch_prompt_currency))
        }

        continueButton.setOnClickListener {
            if(nameLayout.visibility == View.VISIBLE){
                businessName = nameEditText.text.toString()
                nameEditText.setText("")
                nameLayout.visibility = View.GONE
                addressLayout.visibility = View.VISIBLE
            } else if(addressLayout.visibility == View.VISIBLE) {
                address = addressEditText.text.toString()
                addressEditText.setText("")
                addressLayout.visibility = View.GONE
                emailLayout.visibility = View.VISIBLE
            } else if(emailLayout.visibility == View.VISIBLE) {
                email = emailEditText.text.toString()
                emailEditText.setText("")
                emailLayout.visibility = View.GONE
                phoneLayout.visibility = View.VISIBLE
            } else if(phoneLayout.visibility == View.VISIBLE) {
                phoneNumber = phoneEditText.text.toString()
                phoneEditText.setText("")
                phoneLayout.visibility = View.GONE
                currencyButton.visibility = View.VISIBLE
            } else if(currencyButton.visibility == View.VISIBLE){
                Log.e("DATA", businessName)
                Log.e("DATA", address)
                Log.e("DATA", phoneNumber)
                Log.e("DATA", email)
                Log.e("DATA", currency)

                Preference(this).setCurrency(currency)
                Preference(this).setMyBusiness(Business(businessName, address, email, phoneNumber))

                Intent(this, MainActivity::class.java).also {
                    startActivity(it)
                }
            }
        }

        currencyButton.setOnClickListener {
            popupLayout.visibility = View.VISIBLE
        }

        popupLayout.setOnClickListener {
            popupLayout.visibility = View.GONE
        }
    }

    fun setupDirection(){
        val business = Preference(this).getMyBusiness()
        val currency = Preference(this).getCurrency()
        if(business != null && currency!!.isNotBlank()) {
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    fun setupRecyclerView(){
        currencyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LaunchActivity)

            val currencyAdapter = CurrencyAdapter(CURRENCIES)
            currencyAdapter.listener = this@LaunchActivity

            adapter = currencyAdapter
        }
    }

    override fun onClick(text: String) {
        popupLayout.visibility = View.GONE
        currencyButton.text = text
        currency = text
    }
}
