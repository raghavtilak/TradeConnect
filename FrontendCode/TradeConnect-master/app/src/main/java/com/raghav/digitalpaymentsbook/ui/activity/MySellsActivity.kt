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
import com.raghav.digitalpaymentsbook.ui.dialog.ChooseRoleDialog
import com.raghav.digitalpaymentsbook.ui.dialog.LoadingDialog
import com.raghav.digitalpaymentsbook.ui.fragment.BatchsDetailContainerFragment
import com.raghav.digitalpaymentsbook.util.Constants
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class MySellsActivity : AppCompatActivity() {
    lateinit var binding: ActivityMySellsBinding
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
        binding = ActivityMySellsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.recyclerView.layoutManager = LinearLayoutManager(this@MySellsActivity)

        adapter = SellItemAdapter {
            val frag = BatchsDetailContainerFragment(false,it)
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

        binding.addSell.setOnClickListener {
            val roleDialog = ChooseRoleDialog("Create Sell for",onCustomerClick = {
                startActivity(Intent(this,CreateSellActivity::class.java).putExtra("role",UserRole.Customer))
            }, onRetailerClick = {
                startActivity(Intent(this,CreateSellActivity::class.java).putExtra("role",UserRole.Retailer))
            })
            roleDialog.show(supportFragmentManager, "roleDialog")
            roleDialog.isCancelable = true
        }
    }

    private fun updateList() {
        lifecycleScope.launch(handler) {

            loadingDialog.show(supportFragmentManager, "loading")

            val result =
                RetrofitHelper.getInstance(this@MySellsActivity).mySells()
            if (result.isSuccessful && result.body() != null) {
                val list = result.body()!!
                adapter.submitList(list)
                loadingDialog.dismiss()
            } else {
                loadingDialog.dismiss()
                Toast.makeText(
                    this@MySellsActivity,
                    "Some error occurred. Couldn't get your sells.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("TAG", "custo her ${result.body()}")
            }
        }
    }
}