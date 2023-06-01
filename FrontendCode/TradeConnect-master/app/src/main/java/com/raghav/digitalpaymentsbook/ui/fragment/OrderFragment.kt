package com.raghav.digitalpaymentsbook.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.raghav.digitalpaymentsbook.adapter.OrderAdapter
import com.raghav.digitalpaymentsbook.adapter.RetailerAdapter
import com.raghav.digitalpaymentsbook.data.model.Order
import com.raghav.digitalpaymentsbook.data.model.enums.ConnectionStatus
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.FragmentOrderBinding
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class OrderFragment(private val orders: List<Order>) : Fragment() {

    var _binding: FragmentOrderBinding? = null
    val binding: FragmentOrderBinding
        get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())

        val adapter = OrderAdapter { order ->

            if(orders.isNotEmpty()) {

                val frag = OrderDetailsFragment(order) { status ->
                    lifecycleScope.launch {
                        val job = async {
                            val jsonObject = JSONObject()
                            jsonObject.put("status", status.name)
                            val body = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
                            RetrofitHelper.getInstance(requireActivity()).updateOrder(order.id, body)
                        }
                        val res = job.await()
                        if (res.isSuccessful) {
                            Toast.makeText(
                                requireActivity(),
                                res.body()?.message,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
                frag.show(
                    requireActivity().supportFragmentManager,
                    "TAG"
                )
            }
        }
        binding.recyclerView.adapter = adapter
        adapter.submitList(orders)

    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}