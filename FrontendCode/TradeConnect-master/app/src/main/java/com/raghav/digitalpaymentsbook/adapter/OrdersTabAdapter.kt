package com.raghav.digitalpaymentsbook.adapter;

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.raghav.digitalpaymentsbook.data.model.Connection
import com.raghav.digitalpaymentsbook.data.model.Order
import com.raghav.digitalpaymentsbook.ui.fragment.ConnectionsFragment
import com.raghav.digitalpaymentsbook.ui.fragment.OrderFragment

class OrdersTabAdapter(
    fragmentActivity: FragmentActivity,
    private val sent: List<Order>,
    private val received: List<Order>

) :
    FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {

                OrderFragment(received)
            }
            else -> {
                OrderFragment(sent)
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