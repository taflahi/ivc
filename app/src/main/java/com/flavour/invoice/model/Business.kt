package com.flavour.invoice.model

import io.realm.RealmObject

open class Business(
    var name: String? = "",
    var address: String? = "",
    var email: String? = "",
    var phone: String? = ""
) : RealmObject()