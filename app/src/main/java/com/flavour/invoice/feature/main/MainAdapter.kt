package com.flavour.invoice.feature.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flavour.invoice.R
import com.flavour.invoice.model.Invoice
import java.text.DecimalFormat


class MainAdapter(private val invoices: List<Invoice>, private val currency: String) :
    RecyclerView.Adapter<ViewHolder>() {

    lateinit var listener: OnClickListener

    override fun getItemCount(): Int {
        return invoices.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_invoice, parent, false) as View

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            toTextView.text = "To: "
            invoices[position].billTo?.apply {
                if(name!!.isNotBlank()){
                    toTextView.text = "To: " + invoices[position].billTo?.name
                }
            }

            numberTextView.text = invoices[position].number
            dateTextView.text = invoices[position].dateTime
            paidTextView.text = if(invoices[position].isPaid) "Paid" else "Not Paid"

            val decimalFormat = DecimalFormat()
            totalTextView.text = currency + " " + decimalFormat.format(invoices[position].total)

            itemView.setOnClickListener {
                listener.onClick(invoices[position])
            }
        }
    }

    interface OnClickListener{
        fun onClick(invoice: Invoice)
    }
}

class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    val toTextView: TextView = itemView.findViewById(R.id.toTextView)
    val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
    val paidTextView: TextView = itemView.findViewById(R.id.paidTextView)
    val numberTextView: TextView = itemView.findViewById(R.id.numberTextView)
    val totalTextView: TextView = itemView.findViewById(R.id.totalTextView)
}