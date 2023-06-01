package com.raghav.digitalpaymentsbook.ui.fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.raghav.digitalpaymentsbook.R
import com.raghav.digitalpaymentsbook.adapter.RetailerAdapter
import com.raghav.digitalpaymentsbook.data.model.apis.CreateConnection
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.FragmentAddConnectionBinding
import com.raghav.digitalpaymentsbook.databinding.FragmentConnectionsBinding
import com.raghav.digitalpaymentsbook.util.GsonUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

class AddConnectionFragment : BottomSheetDialogFragment() {

    private val debouncePeriod : Long = 500
    private var _binding: FragmentAddConnectionBinding? = null
    val binding: FragmentAddConnectionBinding
        get() = _binding!!

    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAddConnectionBinding.inflate(inflater, container, false)
        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bottomSheet = view.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)

        this.dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())

        val adapter = RetailerAdapter{

            viewLifecycleOwner.lifecycleScope.launch {
                val job = async { RetrofitHelper.getInstance(requireActivity()).createConnectionRequest(
                    CreateConnection(it.id,"none")) }
                val res = job.await()
                if(res.isSuccessful && res.body()!=null){
                    Toast.makeText(requireActivity(), res.body()!!.message, Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(requireActivity(), res.body()!!.error, Toast.LENGTH_SHORT).show()
                }
                dismiss()
            }
        }

        binding.recyclerview.adapter = adapter


        binding.editTextSearch.doOnTextChanged { text, start, before, count ->
            searchJob?.cancel()
            searchJob = viewLifecycleOwner.lifecycleScope.launch {
                text?.let {
                    delay(debouncePeriod)
                    val job = async { RetrofitHelper.getInstance(requireActivity()).getSearchedRetailers(it.toString()) }
                    val res = job.await()
                    if(res.isSuccessful && res.body()!=null){
                        adapter.submitList(res.body()!!)
                    }
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}