package com.raghav.digitalpaymentsbook.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import com.raghav.digitalpaymentsbook.adapter.ConnectionTabAdapter
import com.raghav.digitalpaymentsbook.adapter.OrdersTabAdapter
import com.raghav.digitalpaymentsbook.data.model.enums.ConnectionStatus
import com.raghav.digitalpaymentsbook.data.model.enums.OrderStatus
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.ActivityPendingOrdersBinding
import com.raghav.digitalpaymentsbook.ui.dialog.LoadingDialog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class PendingOrdersActivity : AppCompatActivity() {

    lateinit var binding: ActivityPendingOrdersBinding
    val loadingDialog = LoadingDialog()

    val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        Log.d(
            "TAG",
            "ERROR=${throwable.message} , ${throwable.printStackTrace()}"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateUi()

        binding.swipeRefresh.setOnRefreshListener {
            updateUi()
            binding.swipeRefresh.isRefreshing = false
        }
    }
    private fun updateUi(){
        lifecycleScope.launch(handler){
            loadingDialog.show(this@PendingOrdersActivity.supportFragmentManager,"loading")

            val job1 = async { RetrofitHelper.getInstance(this@PendingOrdersActivity).myOrders(
                OrderStatus.active) }
            val job2 = async { RetrofitHelper.getInstance(this@PendingOrdersActivity).myOrders(
                OrderStatus.declined) }
            val job3 = async { RetrofitHelper.getInstance(this@PendingOrdersActivity).myOrders(
                OrderStatus.cancelled) }

            val (res1,res2,res3) = awaitAll(job1,job2,job3)

            if(res1.isSuccessful && res1.body()!=null &&
                res2.isSuccessful && res2.body()!=null &&
                res3.isSuccessful && res3.body()!=null){

                val orders = (res1.body()!! + res2.body()!! + res3.body()!!).groupBy { it.isCreatedByUser }

                val byUser = orders[true]?: listOf()
                val byOther = orders[false]?: listOf()

                val adapter = OrdersTabAdapter(this@PendingOrdersActivity,byUser,byOther)
                binding.viewpager.adapter= adapter
                TabLayoutMediator(binding.tabLayout,binding.viewpager){
                        tab, position ->
                    tab.text = when(position){
                        0 -> "Received"
                        else-> "Sent"
//                else -> "Rejected"
                    }
                }.attach()
                loadingDialog.dismiss()
            }else{
                Toast.makeText(
                    this@PendingOrdersActivity,
                    "Can't load pending orders",
                    Toast.LENGTH_SHORT
                ).show()
                loadingDialog.dismiss()
            }

        }
    }
}