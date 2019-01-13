package com.flavour.invoice.storage

import android.content.Context
import com.flavour.invoice.model.Business
import com.google.gson.GsonBuilder

class Preference(context: Context){
    val sharedPref = context.getSharedPreferences("BalanceSheet", Context.MODE_PRIVATE)

    fun setCurrency(text: String){
        sharedPref.edit().putString("CURRENCY", text).apply()
    }

    fun getCurrency(): String?{
        return sharedPref.getString("CURRENCY", "")
    }

    fun setMyBusiness(business: Business){
        val gson = GsonBuilder().create()
        val businessString = gson.toJson(business, Business::class.java)
        sharedPref.edit().putString("MYBUSINESS", businessString).apply()
    }

    fun getMyBusiness(): Business?{
        val businessString = sharedPref.getString("MYBUSINESS", "")
        if(businessString == null || businessString.isBlank()) return null

        val gson = GsonBuilder().create()
        return gson.fromJson(businessString, Business::class.java)
    }

    fun incrementSaveNumber(){
        val num = sharedPref.getInt("NUM", 0)
        sharedPref.edit().putInt("NUM", num + 1).apply()
    }

    fun getSaveNumber(): Int{
        return sharedPref.getInt("NUM", 0)
    }
}