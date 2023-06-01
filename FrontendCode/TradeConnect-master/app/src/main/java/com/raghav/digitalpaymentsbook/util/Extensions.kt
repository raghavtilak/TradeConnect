package com.raghav.digitalpaymentsbook.util

import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Rect
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.raghav.digitalpaymentsbook.data.model.Customer
import com.raghav.digitalpaymentsbook.data.model.Retailer
import com.raghav.digitalpaymentsbook.data.model.enums.UserRole

/**
 * Call this method (in onActivityCreated or later) to set
 * the width of the dialog to a percentage of the current
 * screen width.
 */
fun DialogFragment.setWidthPercent(percentage: Int) {
    val percent = percentage.toFloat() / 100
    val dm = Resources.getSystem().displayMetrics
    val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
    val percentWidth = rect.width() * percent
    dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
}

fun DialogFragment.setupWidthToMatchParent() {
    dialog?.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}

fun SharedPreferences.add(customer: Customer){
    with(this.edit()) {
        val g = GsonUtils.gson
        val json = g.toJson(customer)
        putString(UserRole.Customer.name, json)
        apply()
    }
}

fun SharedPreferences.add(retailer: Retailer){
    with(this.edit()) {
        val g = GsonUtils.gson
        val json = g.toJson(retailer)
        putString(UserRole.Retailer.name, json)
        apply()
    }
}

fun SharedPreferences.getCustomer(): Customer {
    val json = this.getString(UserRole.Customer.name, "")
    val g = GsonUtils.gson
    return g.fromJson(json, Customer::class.java)
}

fun SharedPreferences.getRetailer(): Retailer {
    val json = this.getString(UserRole.Retailer.name, "")
    val g = GsonUtils.gson
    return g.fromJson(json, Retailer::class.java)
}

//return true if exists
fun SharedPreferences.userExist(): Boolean {
    return (this.getString(UserRole.Customer.name,"").equals("") || this.getString(UserRole.Retailer.name,"").equals(""))
}

fun SharedPreferences.typeOfUser(): UserRole? {
    return if(!this.getString(UserRole.Customer.name,"").equals("")) UserRole.Customer
    else if(!this.getString(UserRole.Retailer.name,"").equals("")) UserRole.Retailer
    else null
}

fun SharedPreferences.saveAuthToken(token : String) {
    with(this.edit()) {
        putString(Constants.TOKEN, token)
        apply()
    }
}
fun SharedPreferences.getAuthToken(): String {
    return this.getString(Constants.TOKEN, "")!!
}