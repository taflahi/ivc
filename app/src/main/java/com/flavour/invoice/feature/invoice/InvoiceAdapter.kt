package com.flavour.invoice.feature.invoice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flavour.invoice.model.InvoiceCharge
import com.flavour.invoice.model.InvoiceItem
import com.flavour.invoice.R
import java.text.DecimalFormat

class InvoiceAdapter(private val items: List<InvoiceItem>, private val charges: List<InvoiceCharge>, private val currency: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var listener: OnClickListener

    override fun getItemCount(): Int {
        return items.size + charges.size + 2
    }

    override fun getItemViewType(position: Int): Int {
        if(position == 0 || position == items.size + 1){
            return 1
        } else {
            return 2
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == 1){
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_invoice_title, parent, false) as View
            return ViewHolderTitle(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_invoice_item, parent, false) as View
            return ViewHolderContent(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.apply {
            if(itemViewType == 1){
                if(position == 0){
                    var total = 0.0
                    items.forEach {
                        total += (it.value - it.discountValue)
                    }
                    val hold = this as ViewHolderTitle
                    val decimalFormat = DecimalFormat()
                    hold.itemTotalTextView.text = currency + " " + decimalFormat.format(total)
                    hold.itemNameTextView.text = "ITEMS"
                } else {
                    var total = 0.0
                    charges.forEach {
                        total += it.value
                    }
                    val hold = this as ViewHolderTitle
                    val decimalFormat = DecimalFormat()
                    hold.itemTotalTextView.text = currency + " " + decimalFormat.format(total)
                    hold.itemNameTextView.text = "CHARGES"
                }
            } else {
                if(position <= items.size){
                    val loc = position - 1
                    val hold = this as ViewHolderContent
                    hold.itemNameTextView.text = items[loc].name
                    val decimalFormat = DecimalFormat()
                    hold.itemPriceTextView.text = currency + " " + decimalFormat.format(items[loc].value)
                    hold.discountPriceTextView.text = currency + " " + decimalFormat.format(items[loc].discountValue)

                    if(items[loc].discountValue == 0.0){
                        hold.discountNameTextView.visibility = View.GONE
                        hold.discountPriceTextView.visibility = View.GONE
                    } else {
                        hold.discountNameTextView.visibility = View.VISIBLE
                        hold.discountPriceTextView.visibility = View.VISIBLE
                    }

                    hold.itemView.setOnClickListener {
                        listener.onClick(items[loc].id, 0)
                    }
                } else {
                    val loc = position - items.size - 2
                    val hold = this as ViewHolderContent
                    hold.itemNameTextView.text = charges[loc].name
                    val decimalFormat = DecimalFormat()
                    hold.itemPriceTextView.text = currency + " " + decimalFormat.format(charges[loc].value)
                    hold.discountNameTextView.visibility = View.GONE
                    hold.discountPriceTextView.visibility = View.GONE

                    hold.itemView.setOnClickListener {
                        listener.onClick(0, charges[loc].id)
                    }
                }
            }
        }
    }

    interface OnClickListener{
        fun onClick(itemId: Int, chargeId: Int)
    }
}

class ViewHolderTitle(itemView: View): RecyclerView.ViewHolder(itemView){
    val itemNameTextView: TextView = itemView.findViewById(R.id.itemNameTextView)
    val itemTotalTextView: TextView = itemView.findViewById(R.id.itemTotalTextView)
}

class ViewHolderContent(itemView: View): RecyclerView.ViewHolder(itemView){
    val itemNameTextView: TextView = itemView.findViewById(R.id.itemNameTextView)
    val itemPriceTextView: TextView = itemView.findViewById(R.id.itemPriceTextView)
    val discountNameTextView: TextView = itemView.findViewById(R.id.discountNameTextView)
    val discountPriceTextView: TextView = itemView.findViewById(R.id.discountPriceTextView)
}