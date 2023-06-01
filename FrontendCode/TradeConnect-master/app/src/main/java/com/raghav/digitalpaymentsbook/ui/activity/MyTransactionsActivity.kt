package com.raghav.digitalpaymentsbook.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.raghav.digitalpaymentsbook.adapter.SellItemAdapter
import com.raghav.digitalpaymentsbook.data.model.enums.UserRole
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.ActivityMySellsBinding
import com.raghav.digitalpaymentsbook.databinding.ActivityMyTransactionsBinding
import com.raghav.digitalpaymentsbook.ui.dialog.ChooseRoleDialog
import com.raghav.digitalpaymentsbook.ui.dialog.LoadingDialog
import com.raghav.digitalpaymentsbook.ui.fragment.BatchsDetailContainerFragment
import com.raghav.digitalpaymentsbook.util.Constants
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class MyTransactionsActivity : AppCompatActivity() {
    lateinit var binding: ActivityMyTransactionsBinding
    lateinit var adapter: SellItemAdapter
    val loadingDialog = LoadingDialog()
    val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        Log.d(
            "TAG",
            "ERROR=${throwable.message} , ${throwable.printStackTrace()}"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.recyclerView.layoutManager = LinearLayoutManager(this@MyTransactionsActivity)

        adapter = SellItemAdapter {
            val frag = BatchsDetailContainerFragment(false)
            val bundle = Bundle()
            bundle.putParcelableArrayList("batches", ArrayList(it.batches))
            frag.arguments = bundle
            frag.show(supportFragmentManager, "batchDetails")
        }

        binding.recyclerView.adapter = adapter

        updateList()

        binding.swipeRefresh.setOnRefreshListener {
            updateList()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun updateList() {
        lifecycleScope.launch(handler) {

            loadingDialog.show(supportFragmentManager, "loading")

            val result =
                RetrofitHelper.getInstance(this@MyTransactionsActivity).getTransactionsOfCustomer()
            if (result.isSuccessful && result.body() != null) {
                val list = result.body()!!
                adapter.submitList(list)
                loadingDialog.dismiss()
            } else {
                loadingDialog.dismiss()
                Toast.makeText(
                    this@MyTransactionsActivity,
                    "Some error occurred. Couldn't get your sells.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("TAG", "custo her ${result.body()}")
            }
        }
    }
}