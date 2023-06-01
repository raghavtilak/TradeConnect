package com.raghav.digitalpaymentsbook.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.raghav.digitalpaymentsbook.data.model.Customer
import com.raghav.digitalpaymentsbook.data.model.Retailer
import com.raghav.digitalpaymentsbook.data.model.Transaction

class MainViewmodel : ViewModel() {

    val customerList = MutableLiveData<MutableList<Customer>>()
    val retailerList = MutableLiveData<MutableList<Retailer>>()

    init {
        customerList.value = mutableListOf()
    }

    fun addRetailer(retailer: Retailer){
        retailerList.value?.add(retailer)
        retailerList.value = retailerList.value
    }
    fun removeRetailer(retailer: Retailer){
        retailerList.value?.remove(retailer)
        retailerList.value = retailerList.value
    }

    fun addCustomer(customer: Customer){
        customerList.value?.add(customer)
        customerList.value = customerList.value
    }
    fun removeCustomer(customer: Customer){
        customerList.value?.remove(customer)
        customerList.value = customerList.value
    }
}