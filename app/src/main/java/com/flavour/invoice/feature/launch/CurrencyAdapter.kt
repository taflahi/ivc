package com.flavour.invoice.feature.launch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flavour.invoice.R

class CurrencyAdapter(private val currencies: List<String>) :
        RecyclerView.Adapter<ViewHolder>() {

    lateinit var listener: OnClickListener

    override fun getItemCount(): Int {
        return currencies.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_currency_item, parent, false) as View

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = currencies[position]
        holder.itemView.setOnClickListener {
            listener.onClick(currencies[position])
        }
    }

    interface OnClickListener{
        fun onClick(text: String)
    }
}

class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    val textView: TextView = itemView.findViewById(R.id.textView)
}