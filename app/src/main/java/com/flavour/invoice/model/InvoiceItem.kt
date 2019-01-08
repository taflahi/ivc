package com.flavour.invoice.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

data class InvoiceItem(
    @PrimaryKey var id: Int,
    var name: String,
    var value: Double,
    var discountValue: Double
): RealmObject()