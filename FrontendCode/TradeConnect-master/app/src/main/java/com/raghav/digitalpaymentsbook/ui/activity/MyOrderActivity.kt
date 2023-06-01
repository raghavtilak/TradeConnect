package com.raghav.digitalpaymentsbook.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.raghav.digitalpaymentsbook.adapter.OrderAdapter
import com.raghav.digitalpaymentsbook.data.model.enums.OrderStatus
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.ActivityMyOrderBinding
import com.raghav.digitalpaymentsbook.ui.dialog.LoadingDialog
import com.raghav.digitalpaymentsbook.ui.fragment.OrderDetailsFragment
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MyOrderActivity : AppCompatActivity() {
    lateinit var binding: ActivityMyOrderBinding
    val loadingDialog = LoadingDialog()
    lateinit var adapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pendingOrders.setOnClickListener {
            startActivity(Intent(this@MyOrderActivity, PendingOrdersActivity::class.java))
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = OrderAdapter {
            val frag = OrderDetailsFragment(it) {}
            frag.show(this@MyOrderActivity.supportFragmentManager, "orderdetails")
        }

        updateList()

        binding.createOrder.setOnClickListener {
            startActivity(Intent(this, CreateOrderActivity::class.java))
        }
        binding.swipeRefresh.setOnRefreshListener {
            updateList()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun updateList() {
        lifecycleScope.launch {
            loadingDialog.show(this@MyOrderActivity.supportFragmentManager, "loading")
            val job1 = async {
                RetrofitHelper.getInstance(this@MyOrderActivity).myOrders(OrderStatus.inactive)
            }


            val res1 = job1.await()

            if (res1.isSuccessful && res1.body() != null) {
                adapter.submitList(res1.body())
            } else {
                Toast.makeText(
                    this@MyOrderActivity,
                    "Some error occurred. Couldn't load your orders.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            binding.recyclerView.adapter = adapter
            loadingDialog.dismiss()

        }
    }
}