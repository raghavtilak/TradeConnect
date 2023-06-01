package com.raghav.digitalpaymentsbook.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import com.raghav.digitalpaymentsbook.adapter.ConnectionTabAdapter
import com.raghav.digitalpaymentsbook.data.model.enums.ConnectionStatus
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.ActivityMyInvitationsBinding
import com.raghav.digitalpaymentsbook.ui.dialog.LoadingDialog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MyInvitationsActivity : AppCompatActivity() {
    lateinit var binding: ActivityMyInvitationsBinding

    val loadingDialog = LoadingDialog()
    val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        Log.d(
            "TAG",
            "ERROR=${throwable.message} , ${throwable.printStackTrace()}"
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyInvitationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateUi()

        binding.swipeRefresh.setOnRefreshListener {
            updateUi()
            binding.swipeRefresh.isRefreshing = false
        }

    }

    private fun updateUi(){
        lifecycleScope.launch(handler){
            loadingDialog.show(this@MyInvitationsActivity.supportFragmentManager,"loading")

            val job = async { RetrofitHelper.getInstance(this@MyInvitationsActivity).getMyConnections(
                ConnectionStatus.pending) }

            val response = job.await()

            if(response.isSuccessful && response.body()!=null){

                val sent = response.body()!!.filter { it.isCreatedByUser }
                val received = response.body()!!.filter { !it.isCreatedByUser }


                val adapter = ConnectionTabAdapter(this@MyInvitationsActivity,sent,received)
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
                    this@MyInvitationsActivity,
                    "Can't load connections",
                    Toast.LENGTH_SHORT
                ).show()
                loadingDialog.dismiss()
            }

        }
    }
}