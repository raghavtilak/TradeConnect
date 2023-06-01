package com.raghav.digitalpaymentsbook.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.raghav.digitalpaymentsbook.ui.fragment.PendingTransactionsFragment
import com.raghav.digitalpaymentsbook.ui.fragment.SettledTransactionsFragment

class TransactionsTabAdapter(fragmentActivity: FragmentActivity)
    : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> {
                PendingTransactionsFragment()
            }
            else -> {
                SettledTransactionsFragment()
            }
        }
    }

    override fun getItemCount(): Int {
        return 2
    }
}