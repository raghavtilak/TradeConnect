package com.raghav.digitalpaymentsbook.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.raghav.digitalpaymentsbook.data.model.Product

class AddProductViewmodel : ViewModel() {

    private val _productsList = MutableLiveData<MutableList<Product>>()
    val productsList : LiveData<MutableList<Product>>
    get() = _productsList

    val totalPrice = MutableLiveData<Int>()
    val paid= MutableLiveData<Int>()
    val due= MutableLiveData<Int>()

    init {
        _productsList.value = mutableListOf()
        totalPrice.value=0
        paid.value=0
        due.value=0
    }

    fun addProduct(product: Product){
        _productsList.value?.add(product)
        totalPrice.value=totalPrice.value?.plus(product.productPrice)
//        paid.value = paid.value?.plus(product.paid)
        due.value= totalPrice.value?.minus(paid.value!!)!!
        _productsList.value=_productsList.value
    }
    fun removeProduct(product: Product){
        _productsList.value?.remove(product)
        totalPrice.value=totalPrice.value?.minus(product.productPrice)
//        paid.value = paid.value?.minus(product.paid)
        due.value= totalPrice.value?.minus(paid.value!!)!!
        _productsList.value=_productsList.value
    }
    fun updatePaidPrice(value:Int){
        paid.value = value
        due.value= totalPrice.value?.minus(paid.value!!)!!
    }
    fun productsListSize() = _productsList.value!!.size
    fun productsList() = _productsList.value!!.toList()
}