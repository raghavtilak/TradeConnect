package com.raghav.digitalpaymentsbook.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.raghav.digitalpaymentsbook.data.model.AnalyticsData
import com.raghav.digitalpaymentsbook.data.model.enums.AnalyticsType

class AnalyticsViewModel : ViewModel() {

    val ordersByUserData = MutableLiveData<MutableList<AnalyticsData>>()
    val salesByUserData= MutableLiveData<MutableList<AnalyticsData>>()

    val ordersByOtherData= MutableLiveData<MutableList<AnalyticsData>>()
    val salesByOtherData= MutableLiveData<MutableList<AnalyticsData>>()

    val salesIsBarChartType = MutableLiveData<Boolean>()
    val ordersIsBarChartType = MutableLiveData<Boolean>()

    var type = AnalyticsType.week

    init {
        ordersByUserData.value = mutableListOf()
        salesByUserData.value = mutableListOf()
        ordersByOtherData.value = mutableListOf()
        salesByOtherData.value = mutableListOf()

        salesIsBarChartType.value = true
        ordersIsBarChartType.value = true

    }
}