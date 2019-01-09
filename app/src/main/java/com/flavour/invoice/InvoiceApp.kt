package com.flavour.invoice

import android.app.Application
import com.flavour.invoice.model.Business
import com.flavour.invoice.model.InvoiceCharge
import com.flavour.invoice.model.InvoiceItem
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmList

open class InvoiceApp: Application(){
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        val config = RealmConfiguration.Builder().schemaVersion(1).migration { realm, oldVersion, newVersion ->
            val schema = realm.schema
            if (oldVersion == 0L) {
                schema.create("Business")
                    .addField("name", String::class.java)
                    .addField("address", String::class.java)
                    .addField("email", String::class.java)
                    .addField("phone", String::class.java)

                schema.create("InvoiceCharge")
                    .addField("id", Int::class.java)
                    .addField("name", String::class.java)
                    .addField("value", Double::class.java)

                schema.create("InvoiceItem")
                    .addField("id", Int::class.java)
                    .addField("name", String::class.java)
                    .addField("value", Double::class.java)
                    .addField("discountValue", Double::class.java)

                schema.create("Invoice")
                    .addField("id", Int::class.java)
                    .addRealmObjectField("billTo", schema.get("Business"))
                    .addField("isPaid", Boolean::class.java)
                    .addField("dateTime", String::class.java)
                    .addField("dueDateTime", String::class.java)
                    .addField("number", String::class.java)
                    .addField("total", Int::class.java)
                    .addRealmListField("items", schema.get("InvoiceItem"))
                    .addRealmListField("charges", schema.get("InvoiceCharge"))
            }
        }.build()

        Realm.setDefaultConfiguration(config)
    }
}