package com.raghav.digitalpaymentsbook.ui.activity

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.*
import com.google.android.material.tabs.TabLayoutMediator
import com.raghav.digitalpaymentsbook.R
import com.raghav.digitalpaymentsbook.adapter.OrdersStatTabAdapterTabAdapter
import com.raghav.digitalpaymentsbook.adapter.SalesStatTabAdapter
import com.raghav.digitalpaymentsbook.data.model.enums.AnalyticsType
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.ActivityAnalyticsBinding
import com.raghav.digitalpaymentsbook.ui.dialog.LoadingDialog
import com.raghav.digitalpaymentsbook.ui.fragment.DatePickerFragment
import com.raghav.digitalpaymentsbook.viewmodel.AnalyticsViewModel
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsActivity : AppCompatActivity() {

    lateinit var binding: ActivityAnalyticsBinding

    val loading = LoadingDialog()

    val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        Log.d(
            "TAG",
            "ERROR=${throwable.message} , ${throwable.printStackTrace()}"
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel = ViewModelProvider(this)[AnalyticsViewModel::class.java]

        binding.chooseType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("SimpleDateFormat")
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                Log.d("TAG","HIHIHIHIH")
                if (position == 1) {
                    binding.chooseType.updateLayoutParams<LinearLayout.LayoutParams> {
                        this.marginStart = -40
                    }
                } else {
                    binding.chooseType.updateLayoutParams<LinearLayout.LayoutParams> {
                        this.marginStart = -70
                    }
                }
                loading.show(supportFragmentManager, "loading")
                lifecycleScope.launch(handler) {


                    val job1 = async {
                        RetrofitHelper.getInstance(this@AnalyticsActivity)
                            .getOrderAnalytics(
                                SimpleDateFormat("yyyy-MM-dd").format(Date()),
                                when (position) {
                                    0 -> {
                                        AnalyticsType.week
                                    }
                                    1 -> {
                                        AnalyticsType.month
                                    }
                                    else -> {
                                        AnalyticsType.year
                                    }
                                },
                                when (position) {
                                    0 -> {
                                        7
                                    }
                                    1 -> {
                                        186
                                    }
                                    else -> {
                                        1068
                                    }
                                },
                                true
                            )
                    }
                    val job2 = async {
                        RetrofitHelper.getInstance(this@AnalyticsActivity)
                            .getOrderAnalytics(
                                SimpleDateFormat("yyyy-MM-dd").format(Date()),
                                when (position) {
                                    0 -> {
                                        AnalyticsType.week
                                    }
                                    1 -> {
                                        AnalyticsType.month
                                    }
                                    else -> {
                                        AnalyticsType.year
                                    }
                                },
                                when (position) {
                                    0 -> {
                                        7
                                    }
                                    1 -> {
                                        186
                                    }
                                    else -> {
                                        1068
                                    }
                                },
                                false
                            )
                    }

                    val job3 = async {
                        RetrofitHelper.getInstance(this@AnalyticsActivity)
                            .getSalesAnalytics(
                                SimpleDateFormat("yyyy-MM-dd").format(Date()),
                                when (position) {
                                    0 -> {
                                        AnalyticsType.week
                                    }
                                    1 -> {
                                        AnalyticsType.month
                                    }
                                    else -> {
                                        AnalyticsType.year
                                    }
                                },
                                when (position) {
                                    0 -> {
                                        7
                                    }
                                    1 -> {
                                        186
                                    }
                                    else -> {
                                        1068
                                    }
                                },
                                true
                            )
                    }
                    val job4 = async {
                        RetrofitHelper.getInstance(this@AnalyticsActivity)
                            .getSalesAnalytics(
                                SimpleDateFormat("yyyy-MM-dd").format(Date()),
                                when (position) {
                                    0 -> {
                                        AnalyticsType.week
                                    }
                                    1 -> {
                                        AnalyticsType.month
                                    }
                                    else -> {
                                        AnalyticsType.year
                                    }
                                },
                                when (position) {
                                    0 -> {
                                        7
                                    }
                                    1 -> {
                                        186
                                    }
                                    else -> {
                                        1068
                                    }
                                },
                                false
                            )
                    }

                    val (res1, res2) = awaitAll(job1, job2)
                    if (res1.isSuccessful && res1.body() != null &&
                        res2.isSuccessful && res2.body() != null
                    ) {

                        when (position) {
                            0 -> {
                                viewModel.type = AnalyticsType.week
                            }
                            1 -> {
                                viewModel.type = AnalyticsType.month
                            }
                            else -> {
                                viewModel.type = AnalyticsType.year
                            }
                        }
                        viewModel.ordersByUserData.value = res1.body()!!.toMutableList()
                        viewModel.ordersByOtherData.value = res2.body()!!.toMutableList()

                        loading.dismiss()
                    } else {
                        loading.dismiss()
                        Toast.makeText(
                            this@AnalyticsActivity,
                            "Couldn't load orders analytics",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("TAG", "Couldn't load order analytics")
                    }


                    val (res3, res4) = awaitAll(job3, job4)
                    if (res3.isSuccessful && res3.body() != null &&
                        res4.isSuccessful && res4.body() != null
                    ) {

                        when (position) {
                            0 -> {
                                viewModel.type = AnalyticsType.week
                            }
                            1 -> {
                                viewModel.type = AnalyticsType.month
                            }
                            else -> {
                                viewModel.type = AnalyticsType.year
                            }
                        }

                        Log.d("TAG","AA RES3 SIZE="+res3.body()!!.size)
                        viewModel.salesByUserData.value = res3.body()!!.toMutableList()
                        viewModel.salesByOtherData.value = res4.body()!!.toMutableList()

                        Log.d("TAG","AA VM SIZE="+ viewModel.salesByOtherData.value!!.size +","+ viewModel.salesByUserData.value!!.size)

                        loading.dismiss()
                    } else {
                        loading.dismiss()
                        Toast.makeText(
                            this@AnalyticsActivity,
                            "Couldn't load sales analytics",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("TAG", "Couldn't load sales analytics")
                    }


                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        binding.calendarOrder.setOnClickListener {

            val newFragment: DialogFragment = DatePickerFragment { _, year, month, day ->
                val date = "$year-" +
                        (if (month.toString().length < 2) "0${month + 1}" else "${month + 1}") +
                        "-" + (if (day.toString().length < 2) "0${day}" else "$day")


                loading.show(supportFragmentManager, "loading")
                lifecycleScope.launch(handler) {
                    val job1 = async {
                        RetrofitHelper.getInstance(this@AnalyticsActivity)
                            .getOrderAnalytics(
                                date,
                                AnalyticsType.week,
                                7,
                                true
                            )
                    }
                    val job2 = async {
                        RetrofitHelper.getInstance(this@AnalyticsActivity)
                            .getOrderAnalytics(
                                date,
                                AnalyticsType.week,
                                7,
                                false
                            )
                    }

                    val (res1, res2) = awaitAll(job1, job2)
                    if (res1.isSuccessful && res1.body() != null &&
                        res2.isSuccessful && res2.body() != null
                    ) {

                        viewModel.type = AnalyticsType.week

                        viewModel.ordersByUserData.value = res1.body()!!
                        viewModel.ordersByOtherData.value = res2.body()!!

                        loading.dismiss()
                    } else {
                        loading.dismiss()
                        Toast.makeText(
                            this@AnalyticsActivity,
                            "Couldn't load orders analytics",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("TAG", "Couldn't load order analytics")
                    }
                }
            }
            newFragment.show(supportFragmentManager, "datePicker")
        }

        binding.calendarSales.setOnClickListener {
            val newFragment: DialogFragment = DatePickerFragment { _, year, month, day ->
                val date = "$year-" +
                        (if (month.toString().length < 2) "0${month + 1}" else "${month + 1}") +
                        "-" + (if (day.toString().length < 2) "0${day}" else "$day")


                loading.show(supportFragmentManager, "loading")
                lifecycleScope.launch(handler) {
                    val job1 = async {
                        RetrofitHelper.getInstance(this@AnalyticsActivity)
                            .getSalesAnalytics(
                                date,
                                AnalyticsType.week,
                                7,
                                true
                            )
                    }
                    val job2 = async {
                        RetrofitHelper.getInstance(this@AnalyticsActivity)
                            .getSalesAnalytics(
                                date,
                                AnalyticsType.week,
                                7,
                                false
                            )
                    }

                    val (res1, res2) = awaitAll(job1, job2)
                    if (res1.isSuccessful && res1.body() != null &&
                        res2.isSuccessful && res2.body() != null
                    ) {

                        viewModel.type = AnalyticsType.week

                        viewModel.salesByUserData.value = res1.body()!!
                        viewModel.salesByOtherData.value = res2.body()!!

                        loading.dismiss()
                    } else {
                        loading.dismiss()
                        Toast.makeText(
                            this@AnalyticsActivity,
                            "Couldn't load sales analytics",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("TAG", "Couldn't load sales analytics")
                    }
                }
            }
            newFragment.show(supportFragmentManager, "datePicker")
        }

        binding.barGraphChoiceSales.setOnClickListener {
            binding.barGraphChoiceSales.imageTintList =
                ColorStateList.valueOf(resources.getColor(R.color.colorSecondary))
            binding.circleGraphChoiceSales.imageTintList =
                ColorStateList.valueOf(resources.getColor(R.color.disabled))
            viewModel.salesIsBarChartType.value = true
        }

        binding.circleGraphChoiceSales.setOnClickListener {
            binding.circleGraphChoiceSales.imageTintList =
                ColorStateList.valueOf(resources.getColor(R.color.colorSecondary))
            binding.barGraphChoiceSales.imageTintList =
                ColorStateList.valueOf(resources.getColor(R.color.disabled))
            viewModel.salesIsBarChartType.value = false

        }

        val adapterOrder = OrdersStatTabAdapterTabAdapter(this@AnalyticsActivity)
        binding.viewpagerOrder.adapter = adapterOrder
        TabLayoutMediator(binding.tabLayoutOrder, binding.viewpagerOrder) { tab, position ->
            tab.text = when (position) {
                0 -> "Received"
                else -> "Sent"
            }
        }.attach()

        val adapterSales = SalesStatTabAdapter(this@AnalyticsActivity)
        binding.viewpagerSales.adapter = adapterSales
        TabLayoutMediator(binding.tabLayoutSales, binding.viewpagerSales) { tab, position ->
            tab.text = when (position) {
                0 -> "Received"
                else -> "Created"
            }
        }.attach()

        binding.barGraphChoiceOrder.setOnClickListener {
            binding.barGraphChoiceOrder.imageTintList =
                ColorStateList.valueOf(resources.getColor(R.color.colorSecondary))
            binding.circleGraphChoiceOrder.imageTintList =
                ColorStateList.valueOf(resources.getColor(R.color.disabled))
            viewModel.ordersIsBarChartType.value = true
        }

        binding.circleGraphChoiceOrder.setOnClickListener {
            binding.circleGraphChoiceOrder.imageTintList =
                ColorStateList.valueOf(resources.getColor(R.color.colorSecondary))
            binding.barGraphChoiceOrder.imageTintList =
                ColorStateList.valueOf(resources.getColor(R.color.disabled))
            viewModel.ordersIsBarChartType.value = false
        }

    }

}