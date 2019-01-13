package com.flavour.invoice.html

import android.content.Context
import android.util.Log
import com.flavour.invoice.model.Business
import com.flavour.invoice.model.Invoice
import java.math.RoundingMode
import java.text.DecimalFormat
import org.xml.sax.InputSource
import java.io.*
import java.nio.charset.StandardCharsets


open class Generator{

    val itemTemplate = "<tr class=\":class\">\n" +
            "            <td>\n" +
            "                :name\n" +
            "            </td>\n" +
            "\n" +
            "            <td>\n" +
            "                :value\n" +
            "            </td>\n" +
            "        </tr>"

    fun generateHtml(invoice: Invoice, myBusiness: Business, currency: String, context: Context, isPaid: Boolean = false) : String{
        val template = BufferedReader(InputStreamReader(context.assets.open("template1.html")))

        var content = ""
        var line = template.readLine()
        while(line != null){
            content += (line + "\n")
            line = template.readLine()
        }

        template.close()

        // replace invoice details
        content = content.replace(":invoice_number", invoice.number.toString())
        content = content.replace(":invoice_date", invoice.dateTime.toString())
        content = content.replace(":invoice_due_date", invoice.dueDateTime.toString())

        // replace business details
        content = content.replace(":business_name", myBusiness.name.toString())
        content = content.replace(":business_email", myBusiness.email.toString())
        content = content.replace(":business_phone", myBusiness.phone.toString())

        var address = myBusiness.address.toString()
        address = address.replace("\n", "<br/>")
        content = content.replace(":business_address", address)

        val billName = if(invoice.billTo?.name != null) invoice.billTo?.name!! else ""
        val billEmail = if(invoice.billTo?.email != null) invoice.billTo?.email!! else ""
        val billPhone = if(invoice.billTo?.phone != null) invoice.billTo?.phone!! else ""
        var billAddress = if(invoice.billTo?.address != null) invoice.billTo?.address!! else ""

        // replace bill details
        content = content.replace(":billed_name", billName.toString())
        content = content.replace(":billed_email", billEmail.toString())
        content = content.replace(":billed_phone", billPhone.toString())

        billAddress = billAddress.replace("\n", "<br/>")
        content = content.replace(":billed_address", billAddress)

        // replace items
        var items = ""
        var total = 0.0
        invoice.items?.forEachIndexed { index, invoiceItem ->
            total += (invoiceItem.value - invoiceItem.discountValue)
            var itemClass = "item"
            var discountClass = "item discount"
            if(index == invoice.items!!.size - 1){
                if(invoiceItem.discountValue > 0.0){
                    discountClass = "item discount last"
                } else {
                    itemClass = "item last"
                }
            }

            var row = itemTemplate
            row = row.replace(":name", invoiceItem.name)
            row = row.replace(":value", formatNumber(currency, invoiceItem.value))
            row = row.replace(":class", itemClass)

            items += (row + "\n")

            // discount
            if(invoiceItem.discountValue > 0.0){
                var rowDiscount = itemTemplate
                rowDiscount = rowDiscount.replace(":name", "&nbsp;&nbsp;&nbsp;&nbsp;Discount")
                rowDiscount = rowDiscount.replace(":value", formatNumber(currency, invoiceItem.discountValue, positive = false))
                rowDiscount = rowDiscount.replace(":class", discountClass)

                items += (rowDiscount + "\n")
            }
        }

        content = content.replace(":items", items)
        content = content.replace(":subtotal", formatNumber(currency, total))

        // replace charges
        var charges = ""
        invoice.charges?.forEachIndexed { index, invoiceCharge ->
            var itemClass = "item"

            if(index == invoice.charges!!.size - 1){
                itemClass = "item last"
            }

            var row = itemTemplate
            row = row.replace(":name", invoiceCharge.name)
            row = row.replace(":value", formatNumber(currency, invoiceCharge.value))
            row = row.replace(":class", itemClass)

            charges += (row + "\n")
        }

        content = content.replace(":charges", charges)
        content = content.replace(":total", formatNumber(currency, invoice.total))

        var paid = ""
        if(isPaid) paid = "<div class=\"centre\"><strong>PAID</strong></div>"
        content = content.replace(":paid", paid)

        return content
    }

    fun formatNumber(currency: String, value: Double, positive: Boolean = true): String{
        val df = DecimalFormat("###,###.00")
        df.roundingMode = RoundingMode.CEILING

        if(positive)
            return currency + " " + df.format(value)
        else
            return "- " + currency + " " + df.format(value)
    }
}