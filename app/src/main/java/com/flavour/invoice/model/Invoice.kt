package com.flavour.invoice.model

import org.joda.time.DateTime

data class Invoice(
    var billTo: Business,
    var isPaid: Boolean,
    var dateTime: DateTime,
    var dueDateTime: DateTime,
    var number: String,
    var items: MutableList<InvoiceItem>,
    var charges: MutableList<InvoiceCharge>
)