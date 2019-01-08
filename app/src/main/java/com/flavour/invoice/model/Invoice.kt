package com.flavour.invoice.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.joda.time.DateTime

data class Invoice(
    @PrimaryKey var id: Int,
    var billTo: Business? = null,
    var isPaid: Boolean? = null,
    var dateTime: DateTime? = null,
    var dueDateTime: DateTime? = null,
    var number: String? = null,
    var items: MutableList<InvoiceItem> = emptyList<InvoiceItem>().toMutableList(),
    var charges: MutableList<InvoiceCharge> = emptyList<InvoiceCharge>().toMutableList(),
    var total: Int = 0
): RealmObject()