package com.flavour.invoice.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class InvoiceItem(
    @PrimaryKey var id: Int = 0,
    var name: String = "",
    var value: Double = 0.0,
    var discountValue: Double = 0.0
): RealmObject()