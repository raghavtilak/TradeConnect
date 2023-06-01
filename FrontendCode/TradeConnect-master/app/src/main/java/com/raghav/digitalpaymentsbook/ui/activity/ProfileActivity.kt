package com.raghav.digitalpaymentsbook.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.raghav.digitalpaymentsbook.R
import com.raghav.digitalpaymentsbook.data.model.enums.UserRole
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.ActivityProfileBinding
import com.raghav.digitalpaymentsbook.ui.dialog.LoadingDialog
import com.raghav.digitalpaymentsbook.util.PreferenceManager
import com.raghav.digitalpaymentsbook.util.getCustomer
import com.raghav.digitalpaymentsbook.util.typeOfUser
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class ProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityProfileBinding

    val loading = LoadingDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        when(PreferenceManager.getInstance(this).typeOfUser()){
            UserRole.Retailer -> {
                binding.analytics.setOnClickListener {
                    startActivity(Intent(this, AnalyticsActivity::class.java))
                }

                loading.show(supportFragmentManager,"loading")
                lifecycleScope.launch {
                    val job =
                        async { RetrofitHelper.getInstance(this@ProfileActivity).getMyProfile() }
                    val response = job.await()
                    if (response.isSuccessful && response.body() != null) {

                        val p = response.body()!!
                        with(binding) {
                            name.text = p.name
                            noOfConnections.text = p.totalConnections.toString()
                            businessName.text = p.businessName
                            businessType.text = p.businessType
                            email.text = p.email
                            phone.text = p.phone
                            address.text = p.address
                            totalSales.text = p.totalSales.toString()
                            createdOrders.text = p.createdOrders.toString()
                            receivedOrders.text = p.receivedOrders.toString()
                            loading.dismiss()
                        }

                    } else {
                        loading.dismiss()
                        Toast.makeText(
                            this@ProfileActivity,
                            "Couldn't load your profile",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("TAG", "Couldn't load profile types")
                    }
                }
            }
            UserRole.Customer -> {
                binding.retGroup.isVisible = false
                binding.analytics.isVisible = false
                binding.linear2.isVisible = false
                binding.linear3.isVisible = false
                binding.linear1.isVisible = false

                val c = PreferenceManager.getInstance(this).getCustomer()
                binding.email.text = c.email
                binding.name.text = c.name
                binding.phone.text = c.phone
                binding.address.text = c.address

                binding.retImage.setImageDrawable(resources.getDrawable(R.drawable.customer))
            }
            null -> {
                Toast.makeText(
                    this,
                    "Something is wrong, try clearing app data",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }
}