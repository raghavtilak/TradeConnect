package com.raghav.digitalpaymentsbook.ui.activity

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.raghav.digitalpaymentsbook.adapter.ProductAdapter
import com.raghav.digitalpaymentsbook.data.model.*
import com.raghav.digitalpaymentsbook.data.model.apis.RetailerSignIn
import com.raghav.digitalpaymentsbook.data.model.enums.TransactionStatus
import com.raghav.digitalpaymentsbook.data.model.enums.UserRole
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.ActivityAddProductBinding
import com.raghav.digitalpaymentsbook.ui.dialog.LoadingDialog
import com.raghav.digitalpaymentsbook.ui.viewmodel.AddProductViewmodel
import com.raghav.digitalpaymentsbook.util.Constants
import com.raghav.digitalpaymentsbook.util.GsonUtils
import com.raghav.digitalpaymentsbook.util.getRetailer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import java.util.Date

class AddProductActivity : AppCompatActivity(), ProductAdapter.OnRemoveClickListener {

    lateinit var binding: ActivityAddProductBinding
    lateinit var viewmodel: AddProductViewmodel
    val handler = CoroutineExceptionHandler { _, throwable -> Log.d("TAG","ERROR=${throwable.message}") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewmodel = ViewModelProvider(this)[AddProductViewmodel::class.java]
        val adapter = ProductAdapter(this)
        binding.recyclerview.layoutManager = LinearLayoutManager(this, VERTICAL,false)
        binding.recyclerview.adapter = adapter

        viewmodel.productsList.observe(this) {
            Log.d("TAG","products=$it")
            adapter.submitList(it.toMutableList())
        }
        viewmodel.totalPrice.observe(this){
            binding.totalAmount.text=it.toString()
        }
        viewmodel.paid.observe(this){
            binding.userPaid.text=it.toString()
        }
        viewmodel.due.observe(this){
            binding.dueAmount.text=it.toString()
        }

        binding.editTextPaidPrice.doOnTextChanged { text, _,_,_ ->
            if(text.isNullOrBlank()){
                viewmodel.updatePaidPrice(0)
            }else{
                val paid = text.toString().toInt()
                if(viewmodel.totalPrice.value!! !=0 && paid>viewmodel.totalPrice.value!!){
                    Toast.makeText(
                        this@AddProductActivity,
                        "Paid amount can't be greater than total amount",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.editTextPaidPrice.text!!.clear()
                }else{
                    viewmodel.updatePaidPrice(paid)
                }

            }
        }

        binding.addTransaction.setOnClickListener {

            if(validateAddTransaction()){
                val dialog = LoadingDialog()
                dialog.isCancelable=false
                dialog.show(supportFragmentManager,"loading")

                lifecycleScope.launch(handler) {

                    val customerName = binding.editTextCustomerName.text.toString()
                    val customerPhone = binding.editTextCustomerPhone.text.toString()
                    val transactionName = binding.editTextTransactionName.text.toString()

                    val r= getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE).getRetailer()
                    val transaction = Transaction(
                        Date(System.currentTimeMillis()),
                        if(viewmodel.due.value!!>0) TransactionStatus.active else TransactionStatus.inactive,
                        viewmodel.totalPrice.value!!,
                        viewmodel.paid.value!!,
                        viewmodel.due.value!!,
                        r.id,customerName,customerPhone.toLong(),
                        "",
                        viewmodel.productsList(),
                        transactionName,
                        null
                    )

                    Log.d("TAG","transaction= ${GsonUtils.gson.toJson(transaction)}")


                    val result = RetrofitHelper.getInstance(this@AddProductActivity).addTransaction(transaction,transaction.retailer.toHexString())

                    if(result.isSuccessful){
                        dialog.dismiss()
                        Log.d("TAG","prod= ${result.body()}")
                        Toast.makeText(applicationContext,"Transaction added!",Toast.LENGTH_SHORT).show()
                        finish()
                    }else{
                        Log.d("TAG","Error body=${result.body()}, ${result.isSuccessful}, ${result.errorBody()}")
                        dialog.dismiss()
                        Toast.makeText(this@AddProductActivity,"Some error occurred",Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
        binding.addProduct.setOnClickListener {
            if(validateAddProduct()){

                binding.TextFieldProduct.isErrorEnabled=false
                binding.TextFieldProductPrice.isErrorEnabled=false


                val name = binding.editTextProduct.text!!.toString()
                val price = binding.editTextProductPrice.text!!.toString().toInt()

                val p = Product(name,price)
                viewmodel.addProduct(p)
                Log.d("TAG","products=$p")

            }
        }

        binding.searchCard.setOnClickListener {
            if(binding.editTextCustomerPhone.text.isNullOrBlank() || binding.editTextCustomerPhone.text!!.length<10){
                binding.TextFieldCustomerPhone.isErrorEnabled = true
                binding.TextFieldCustomerPhone.error = "Please enter a valid phone number"
            }else {
                val dialog = LoadingDialog()
                dialog.isCancelable=false
                dialog.show(supportFragmentManager,"loading")

                binding.TextFieldCustomerPhone.isErrorEnabled = false
                lifecycleScope.launch(handler) {
                    val result =
                        RetrofitHelper.getInstance(this@AddProductActivity).getUser(
                            null, "${binding.editTextCustomerPhone.text!!}".substring(3))


                    if (result.isSuccessful && result.body() != null) {
                        Log.d("TAG", "got response= ${result.body()}")
                        val user = result.body()
                        when(user!!.role){
                            UserRole.Retailer -> {
                                Toast.makeText(
                                    this@AddProductActivity,
                                    "This number belongs to a retailer!",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                            UserRole.Customer -> {
                                with(binding){
                                    editTextCustomerName.text = Editable.Factory.getInstance().newEditable(user.name)
                                    editTextCustomerName.isEnabled = false
                                    editTextCustomerPhone.isEnabled = false
                                    searchCard.isEnabled = false
                                    nestedScrollView.post { nestedScrollView.smoothScrollTo(0, TextFieldCustomerPhone.bottom) }
                                }
                            }
                        }
                        dialog.dismiss()
                    }else{
                        Log.d("TAG", "got error= ${result.body()}")

                        Toast.makeText(
                            this@AddProductActivity,
                            "No user found for this number.",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    }
                }
            }

        }

    }

    private fun validateAddTransaction(): Boolean {
        if(binding.editTextTransactionName.text.isNullOrBlank()){
            binding.TextFieldTransactionName.isErrorEnabled=true
            binding.TextFieldTransactionName.error="This is required"
            return false
        }
        if(binding.editTextPaidPrice.text.isNullOrBlank()){
            binding.TextFieldPaidPrice.isErrorEnabled=true
            binding.TextFieldPaidPrice.error="Required"
            return false
        }
        if(binding.editTextCustomerName.text.isNullOrBlank()){
            binding.TextFieldCustomerName.isErrorEnabled=true
            binding.TextFieldCustomerName.error="Required"
            return false
        }
        if(viewmodel.productsListSize()==0){
            Toast.makeText(
                this@AddProductActivity,
                "Please add at least one product",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    private fun validateAddProduct(): Boolean {
        if(binding.editTextProduct.text.isNullOrBlank()){
            binding.TextFieldProduct.isErrorEnabled=true
            binding.TextFieldProduct.error="This is required"
            return false
        }
        if(binding.editTextProductPrice.text.isNullOrBlank()){
            binding.TextFieldProductPrice.isErrorEnabled=true
            binding.TextFieldProductPrice.error="This is required"
            return false
        }
        return true
    }

    override fun onItemRemove(product: Product) {
        viewmodel.removeProduct(product)
    }
}