package com.raghav.digitalpaymentsbook.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.raghav.digitalpaymentsbook.data.model.Customer
import com.raghav.digitalpaymentsbook.data.model.Retailer
import com.raghav.digitalpaymentsbook.data.model.Transaction

class TransactionViewmodel : ViewModel() {

    var customer : Customer? = null
    var retailer : Retailer? = null
    val pendingList = MutableLiveData<MutableList<Transaction>>()
    val settledList = MutableLiveData<MutableList<Transaction>>()

    init {
        pendingList.value = mutableListOf()
        settledList.value = mutableListOf()
    }

    fun addPendingTransaction(transaction: Transaction){
        pendingList.value?.add(transaction)
        pendingList.value = pendingList.value
    }
    fun removePendingTransaction(transaction: Transaction){
        pendingList.value?.remove(transaction)
        pendingList.value = pendingList.value
    }

    fun addSettledTransaction(transaction: Transaction){
        settledList.value?.add(transaction)
        settledList.value = settledList.value
    }
    fun removeSettledTransaction(transaction: Transaction){
        settledList.value?.remove(transaction)
        settledList.value = settledList.value
    }

}