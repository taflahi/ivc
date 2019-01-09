package com.flavour.invoice.feature.billing

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import com.flavour.invoice.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.functions.Function7
import kotlinx.android.synthetic.main.activity_billing_detail.*
import org.joda.time.format.DateTimeFormat
import java.util.*

class BillingDetailActivity : AppCompatActivity() {

    var name: String = ""
    var address: String = ""
    var email: String = ""
    var phone: String = ""
    var date: String = ""
    var dueDate: String = ""
    var number: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_billing_detail)

        getData()
        setupEditText()
    }

    fun getData(){
        val data = intent.extras
        if(data == null) return

        if(data.getString("NAME", "").isNotBlank()){
            name = data.getString("NAME", "")
        }

        if(data.getString("ADDRESS", "").isNotBlank()){
            address = data.getString("ADDRESS", "")
        }

        if(data.getString("EMAIL", "").isNotBlank()){
            email = data.getString("EMAIL", "")
        }

        if(data.getString("PHONE", "").isNotBlank()){
            phone = data.getString("PHONE", "")
        }

        if(data.getString("DATE", "").isNotBlank()){
            date = data.getString("DATE", "")
        }

        if(data.getString("DUEDATE", "").isNotBlank()){
            dueDate = data.getString("DUEDATE", "")
        }

        if(data.getString("NUMBER", "").isNotBlank()){
            number = data.getString("NUMBER", "")
        }
    }

    @SuppressLint("CheckResult")
    fun setupEditText(){
        Observable.combineLatest(RxTextView.textChanges(nameEditText),
            RxTextView.textChanges(addressEditText),
            RxTextView.textChanges(emailEditText),
            RxTextView.textChanges(phoneEditText),
            RxTextView.textChanges(dateEditText),
            RxTextView.textChanges(dueDateEditText),
            RxTextView.textChanges(numberEditText),
            Function7 <CharSequence, CharSequence, CharSequence, CharSequence, CharSequence, CharSequence, CharSequence, Boolean>
            { t1, t2, t3, t4, t5, t6, t7 ->
                t1.isNotBlank() and t2.isNotBlank() and t3.isNotBlank() and t4.isNotBlank() and t5.isNotBlank() and t6.isNotBlank() and t7.isNotBlank()
            }).subscribe {
            saveButton.visibility = if(it) View.VISIBLE else View.GONE
        }

        nameEditText.setText(name)
        addressEditText.setText(address)
        emailEditText.setText(email)
        phoneEditText.setText(phone)
        dateEditText.setText(date)
        dueDateEditText.setText(dueDate)
        numberEditText.setText(number)

        dueDateEditText.setOnClickListener {
            showDatePicker {
                dueDateEditText.setText(it)
            }
        }

        dateEditText.setOnClickListener {
            showDatePicker {
                dateEditText.setText(it)
            }
        }
    }

    private fun showDatePicker(lambda: (String) -> Unit){
        val calendar = Calendar.getInstance()
        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            // Display Selected date in textbox

            val calendar = Calendar.getInstance()
            calendar.set(year, monthOfYear, dayOfMonth)

            val dateFormat = DateTimeFormat.forPattern("EEEE, dd MMMM yyyy")
            lambda(dateFormat.print(calendar.timeInMillis))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) - 1, calendar.get(Calendar.DAY_OF_MONTH))

        dpd.show()
    }
}
