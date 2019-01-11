package com.flavour.invoice.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.joda.time.DateTime

open class Invoice(
    @PrimaryKey var id: Int? = 0,
    var billTo: Business? = null,
    var isPaid: Boolean = false,
    var dateTime: String? = null,
    var dueDateTime: String? = null,
    var number: String? = null,
    var items: RealmList<InvoiceItem>? = null,
    var charges: RealmList<InvoiceCharge>? = null,
    var total: Double = 0.0
): RealmObject()