package com.raghav.digitalpaymentsbook.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.raghav.digitalpaymentsbook.adapter.RetailerAdapter
import com.raghav.digitalpaymentsbook.data.model.enums.ConnectionStatus
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.ActivityMyConnectionsBinding
import com.raghav.digitalpaymentsbook.ui.dialog.LoadingDialog
import com.raghav.digitalpaymentsbook.ui.fragment.AddConnectionFragment
import com.raghav.digitalpaymentsbook.ui.fragment.RetailerDetailsFragment
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MyConnectionsActivity : AppCompatActivity() {
    lateinit var binding: ActivityMyConnectionsBinding
    val loadingDialog = LoadingDialog()
    lateinit var adapter: RetailerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyConnectionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addConnection.setOnClickListener {
            val frag = AddConnectionFragment()
            frag.show(
                supportFragmentManager,
                "TAG"
            )
        }

        binding.invitation.setOnClickListener {
            startActivity(Intent(this@MyConnectionsActivity, MyInvitationsActivity::class.java))
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = RetailerAdapter {
            val frag = RetailerDetailsFragment(it)
            frag.show(
                this@MyConnectionsActivity.supportFragmentManager,
                "TAG"
            )
        }

        updateList()

        binding.swipeRefresh.setOnRefreshListener {
            updateList()
            binding.swipeRefresh.isRefreshing = false
        }

    }

    private fun updateList() {
        lifecycleScope.launch {
            loadingDialog.show(this@MyConnectionsActivity.supportFragmentManager, "loading")
            val job = async {
                RetrofitHelper.getInstance(this@MyConnectionsActivity)
                    .getMyConnections(ConnectionStatus.accepted)
            }
            val response = job.await()
            if (response.isSuccessful && response.body() != null) {
                adapter.submitList(response.body()!!.map { it.user })
            }else{
                Toast.makeText(
                    this@MyConnectionsActivity,
                    "Some error occurred. Couldn't load your connections.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            binding.recyclerView.adapter = adapter
            loadingDialog.dismiss()

        }
    }
}