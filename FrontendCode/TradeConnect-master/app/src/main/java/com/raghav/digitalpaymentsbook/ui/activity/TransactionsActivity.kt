package com.raghav.digitalpaymentsbook.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import com.raghav.digitalpaymentsbook.adapter.TransactionsTabAdapter
import com.raghav.digitalpaymentsbook.data.model.enums.TransactionStatus
import com.raghav.digitalpaymentsbook.data.model.enums.UserRole
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.ActivityTransactionsBinding
import com.raghav.digitalpaymentsbook.ui.viewmodel.TransactionViewmodel
import com.raghav.digitalpaymentsbook.util.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class TransactionsActivity : AppCompatActivity() {

    lateinit var binding: ActivityTransactionsBinding
    lateinit var viewmodel: TransactionViewmodel
    val handler = CoroutineExceptionHandler { coroutineContext, throwable -> Log.d("TAG","ERROR=${throwable.message}") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = TransactionsTabAdapter(this)
        binding.viewpager.adapter= adapter
        TabLayoutMediator(binding.tabLayout,binding.viewpager){
            tab, position ->
            tab.text = when(position){
                0 -> "Pending"
                else -> "Settled"
            }
        }.attach()

        viewmodel = ViewModelProvider(this)[TransactionViewmodel::class.java]

        when(PreferenceManager.getInstance(this).typeOfUser()){
            UserRole.Retailer -> {
                viewmodel.retailer = PreferenceManager.getInstance(this).getRetailer()
                viewmodel.customer = intent.getParcelableExtra(UserRole.Customer.name)
                Log.d("TAG","ta if ret, cust= ${viewmodel.customer}, ret = ${viewmodel.retailer} ")

                showForRetailer()
            }
            UserRole.Customer -> {
                viewmodel.customer = PreferenceManager.getInstance(this).getCustomer()
                viewmodel.retailer = intent.getParcelableExtra(UserRole.Retailer.name)
                Log.d("TAG","ta if cust, cust= ${viewmodel.customer}, ret = ${viewmodel.retailer} ")

                showForCustomer()
            }
            else -> {

            }
        }

        lifecycleScope.launch(handler){
            val result = RetrofitHelper.getInstance(this@TransactionsActivity)
                .getAllTransactions(viewmodel.retailer!!.id.toHexString()
                ,viewmodel.customer!!.phone.toLong())
            if(result.isSuccessful && result.body()!=null){
                Log.d("TAG","transaction:${result.body()}")
                val list = result.body()!!
                list.forEach {
                    when(it.status){
                        TransactionStatus.active -> viewmodel.addPendingTransaction(it)
                        TransactionStatus.inactive -> viewmodel.addSettledTransaction(it)
                    }
                }
            }else{
                Log.d("TAG","error reposd'pasdsz:${result.body()}")
            }
        }

    }

    private fun showForRetailer() {


    }

    private fun showForCustomer() {

    }
}