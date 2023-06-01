package com.raghav.digitalpaymentsbook.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.raghav.digitalpaymentsbook.adapter.RetailerAdapter
import com.raghav.digitalpaymentsbook.data.model.enums.UserRole
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.ActivityMainBinding
import com.raghav.digitalpaymentsbook.ui.dialog.LoadingDialog
import com.raghav.digitalpaymentsbook.ui.viewmodel.MainViewmodel
import com.raghav.digitalpaymentsbook.util.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {
//    , CustomerAdapter.OnItemClickListener,
//    RetailerAdapter.OnItemClickListener {

    lateinit var binding: ActivityMainBinding

    val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        Log.d(
            "TAG",
            "ERROR=${throwable.message} ${throwable.printStackTrace()}"
        )
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            Toast.makeText(
                this,
                "This permission is required to get notifications for orders and connection requests",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        )

        val fb = FirebaseAuth.getInstance()
        if (fb.currentUser == null || !PreferenceManager.getInstance(this).userExist()) {
            if (fb.currentUser != null) {
                fb.signOut()
                fb.currentUser!!.delete().addOnCompleteListener {
                    startActivity(Intent(this@MainActivity, SignInActivity::class.java))
                    finish()
                }
            } else {
                startActivity(Intent(this@MainActivity, SignInActivity::class.java))
                finish()
            }

        } else {

            when (PreferenceManager.getInstance(this).typeOfUser()) {
                UserRole.Retailer -> {
                    showForRetailer()
                }
                UserRole.Customer -> {
                    showForCustomer()
                }
                null -> {
                    Toast.makeText(
                        this,
                        "Something is wrong, try clearing app data",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            binding.profile.setOnClickListener {
                startActivity(Intent(this@MainActivity, ProfileActivity::class.java))

            }

            askNotificationPermission()
                lifecycleScope.launch { getFcmToken() }
        }

    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Toast.makeText(
                    this,
                    "This permission is required to get notifications for orders and connection requests",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    private fun showForCustomer() {
        binding.retGroup.isVisible = false
        binding.recyclerView.isVisible = true
        binding.businessName.text = "Retailers"

        val adapter = RetailerAdapter{
            startActivity(Intent(this,MyTransactionsActivity::class.java).putExtra("retailer",it))
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            val loadingDialog = LoadingDialog()
            loadingDialog.show(supportFragmentManager,"loading")
            lifecycleScope.launch(handler) {

                val result =
                    RetrofitHelper.getInstance(this@MainActivity)
                        .getRetailerOfACustomers(PreferenceManager.getInstance(this@MainActivity).getCustomer().email)
                if (result.isSuccessful && result.body() != null) {
                    val list = result.body()!!
                    adapter.submitList(list)
                    Log.d("TAG", "custo her ${result.body()}")
                    loadingDialog.dismiss()
                } else {
                    Toast.makeText(this@MainActivity, "Couldn't load retailers", Toast.LENGTH_SHORT)
                        .show()
                    loadingDialog.dismiss()
                    Log.d("TAG", "custo her ${result.body()}")
                }
            }
        }
        lifecycleScope.launch(handler) {

            val result =
                RetrofitHelper.getInstance(this@MainActivity)
                    .getRetailerOfACustomers(PreferenceManager.getInstance(this@MainActivity).getCustomer().email)
            if (result.isSuccessful && result.body() != null) {
                val list = result.body()!!
                adapter.submitList(list)
                Log.d("TAG", "custo her ${result.body()}")
            } else {
                Toast.makeText(this@MainActivity, "Couldn't load retailers", Toast.LENGTH_SHORT)
                    .show()
                Log.d("TAG", "custo her ${result.body()}")
            }
        }

    }

    private fun showForRetailer() {

        binding.businessName.text =
            PreferenceManager.getInstance(this).getRetailer().businessName

        binding.retGroup.isVisible = true
        binding.recyclerView.isVisible = false

        binding.viewStore.setOnClickListener {
            startActivity(Intent(this@MainActivity, ViewStoreActivity::class.java))

        }
        binding.myOrder.setOnClickListener {
            startActivity(Intent(this@MainActivity, MyOrderActivity::class.java))

        }
        binding.mySells.setOnClickListener {
            startActivity(Intent(this@MainActivity, MySellsActivity::class.java))

        }
        binding.myConnections.setOnClickListener {
            startActivity(Intent(this@MainActivity, MyConnectionsActivity::class.java))

        }

    }

/*
    override fun onItemClick(customer: Customer) {
        startActivity(
            Intent(this, TransactionsActivity::class.java)
                .putExtra(UserRole.Customer.name, customer)
        )
    }

    override fun onItemClick(retailer: Retailer) {
        startActivity(
            Intent(this, TransactionsActivity::class.java)
                .putExtra(UserRole.Retailer.name, retailer)
        )
    }

*/


    private suspend fun getFcmToken(): String {
        return try {
            Log.d("TAG", "Got token : ${FirebaseMessaging.getInstance().token.await()}")
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.w("TAG", "Fetching FCM registration token failed" + e.printStackTrace())
            ""
        }
    }

}