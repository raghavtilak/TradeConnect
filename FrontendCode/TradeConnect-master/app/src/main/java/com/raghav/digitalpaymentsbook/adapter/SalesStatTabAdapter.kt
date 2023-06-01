package com.raghav.digitalpaymentsbook.adapter;

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.raghav.digitalpaymentsbook.data.model.AnalyticsData
import com.raghav.digitalpaymentsbook.data.model.Order
import com.raghav.digitalpaymentsbook.ui.fragment.OrderStatFragment
import com.raghav.digitalpaymentsbook.ui.fragment.SalesStatFragment

class SalesStatTabAdapter(
    fragmentActivity: FragmentActivity
) :
    FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                SalesStatFragment(false)
            }
            else -> {
                SalesStatFragment(true)
            }
//            else -> {
//                ConnectionsFragment(ConnectionStatus.rejected)
//
//            }
        }
    }

    override fun getItemCount(): Int {
        return 2
    }
}