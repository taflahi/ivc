package com.flavour.invoice.feature.business

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.flavour.invoice.R
import com.flavour.invoice.constants.CURRENCIES
import com.flavour.invoice.feature.launch.CurrencyAdapter
import com.flavour.invoice.model.Business
import com.flavour.invoice.storage.Preference
import com.jakewharton.rxbinding2.widget.RxTextView
import kotlinx.android.synthetic.main.activity_business.*

class BusinessActivity : AppCompatActivity(), CurrencyAdapter.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business)

        setupEditTexts()
        setupRecyclerView()
        setupButton()
    }

    @SuppressLint("CheckResult")
    fun setupEditTexts(){
        val business = Preference(this).getMyBusiness()
        business?.apply {
            nameEditText.setText(this.name)
            addressEditText.setText(this.address)
            emailEditText.setText(this.email)
            phoneEditText.setText(this.phone)
        }

        val currency = Preference(this).getCurrency()
        currency?.apply {
            currencyButton.text = currency
        }

        RxTextView.textChanges(nameEditText).subscribe {
            saveButton.visibility = if(it.isNotBlank()) View.VISIBLE else View.GONE
        }

        RxTextView.textChanges(addressEditText).subscribe {
            saveButton.visibility = if(it.isNotBlank()) View.VISIBLE else View.GONE
        }

        RxTextView.textChanges(emailEditText).subscribe {
            saveButton.visibility = if(it.isNotBlank()) View.VISIBLE else View.GONE
        }

        RxTextView.textChanges(phoneEditText).subscribe {
            saveButton.visibility = if(it.isNotBlank()) View.VISIBLE else View.GONE
        }

        RxTextView.textChanges(currencyButton).subscribe {
            saveButton.visibility = if(it != getString(R.string.launch_prompt_currency)) View.VISIBLE else View.GONE
        }

        currencyButton.setOnClickListener {
            popupLayout.visibility = View.VISIBLE
        }

        popupLayout.setOnClickListener {
            popupLayout.visibility = View.GONE
        }
    }

    fun setupButton(){
        saveButton.setOnClickListener {
            Preference(this).setCurrency(currencyButton.text.toString())
            Preference(this).setMyBusiness(Business(nameEditText.text.toString(), addressEditText.text.toString(), emailEditText.text.toString(), phoneEditText.text.toString()))

            finish()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    fun setupRecyclerView(){
        currencyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@BusinessActivity)

            val currencyAdapter = CurrencyAdapter(CURRENCIES)
            currencyAdapter.listener = this@BusinessActivity

            adapter = currencyAdapter
        }
    }

    override fun onClick(text: String) {
        currencyButton.text = text
        popupLayout.visibility = View.GONE
    }
}
