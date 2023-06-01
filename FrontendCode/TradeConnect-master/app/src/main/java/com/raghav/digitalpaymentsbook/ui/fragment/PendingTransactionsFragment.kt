package com.raghav.digitalpaymentsbook.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.raghav.digitalpaymentsbook.R
import com.raghav.digitalpaymentsbook.adapter.TransactionsAdapter
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.FragmentPendingTransactionsBinding
import com.raghav.digitalpaymentsbook.databinding.FragmentSettledTransactionsBinding
import com.raghav.digitalpaymentsbook.ui.viewmodel.TransactionViewmodel
import kotlinx.coroutines.launch

class PendingTransactionsFragment : Fragment() {

    var _binding: FragmentPendingTransactionsBinding? =null
    val binding : FragmentPendingTransactionsBinding
        get() = _binding!!

    lateinit var viewmodel: TransactionViewmodel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentPendingTransactionsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewmodel = ViewModelProvider(requireActivity())[TransactionViewmodel::class.java]
        val adapter = TransactionsAdapter(false)
        viewmodel.pendingList.observe(requireActivity()){
            adapter.submitList(it.toMutableList())
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        binding.recyclerView.adapter=adapter


    }

    fun pay(){

    }
    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }
}