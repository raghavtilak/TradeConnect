package com.raghav.digitalpaymentsbook.adapter;

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.raghav.digitalpaymentsbook.data.model.Connection
import com.raghav.digitalpaymentsbook.ui.fragment.ConnectionsFragment

class ConnectionTabAdapter(
    fragmentActivity: FragmentActivity,
    private val sent: List<Connection>,
    private val received: List<Connection>

) :
    FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {

                ConnectionsFragment(received)
            }
            else -> {
                ConnectionsFragment(sent)
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