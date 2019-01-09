package com.flavour.invoice.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class InvoiceCharge(
    @PrimaryKey var id: Int = 0,
    var name: String = "",
    var value: Double = 0.0
): RealmObject()