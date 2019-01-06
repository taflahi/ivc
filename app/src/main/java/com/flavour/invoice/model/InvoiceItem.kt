package com.flavour.invoice.model

data class InvoiceItem(
    var name: String,
    var value: Double,
    var discount: String,
    var discountValue: Double
)