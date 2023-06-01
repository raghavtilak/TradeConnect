package com.raghav.digitalpaymentsbook.adapter

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.raghav.digitalpaymentsbook.data.model.Batch
import com.raghav.digitalpaymentsbook.data.model.Test
import com.raghav.digitalpaymentsbook.ui.fragment.BatchDetailFragment


class BatchDetailTabAdapter(
    fragmentActivity: FragmentActivity,
    private val batches:List<Batch>,
    private val showUpdateOption :Boolean
) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return batches.size
    }

    override fun createFragment(position: Int): Fragment {

        val b = Bundle()
        b.putParcelable("batch", batches[position])
        val frag: Fragment = BatchDetailFragment(showUpdateOption)
        frag.arguments = b
        return frag


    }
}