package com.raghav.digitalpaymentsbook.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.raghav.digitalpaymentsbook.adapter.RetailerAdapter
import com.raghav.digitalpaymentsbook.data.model.Connection
import com.raghav.digitalpaymentsbook.data.model.enums.ConnectionStatus
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.FragmentConnectionsBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.bson.types.ObjectId
import org.json.JSONObject

class ConnectionsFragment(private val connections: List<Connection>) : Fragment() {

    var _binding: FragmentConnectionsBinding? = null
    val binding: FragmentConnectionsBinding
        get() = _binding!!

    val handler =
        CoroutineExceptionHandler { _, throwable ->
            Log.d("TAG", "ERROR=${throwable.message}" + throwable.printStackTrace())

        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectionsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())

        val adapter = RetailerAdapter { retailer ->

            if(connections.isNotEmpty()) {

                Log.d("TAG","RET ID= ${retailer.id}")
                var connection:Connection? =null
                connections.forEach {
                    Log.d("TAG","CONN ID= ${it.id}")
                    if(retailer.id == it.user.id){
                        Log.d("TAG","true")
                        connection = it
                    }
                }

                Log.d("TAG","CHECK ID= ${connection?.id}")


                val frag = RetailerDetailsFragment(retailer, ConnectionStatus.pending, connection) { status, connId ->
                    lifecycleScope.launch(handler) {
                        val job = async {
                            val jsonObject = JSONObject()
                            jsonObject.put("status", status.name)
                            val body = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
                            Log.d("TAG","CONN ID= $connId")
                            RetrofitHelper.getInstance(requireActivity()).updateConnectionReq(connId!!,body)
                        }
                        val res = job.await()
                        if (res.isSuccessful) {
                            Toast.makeText(
                                requireActivity(),
                                res.body()?.message,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }else{
                            Toast.makeText(
                                requireActivity(),
                                res.body()?.error,
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
        adapter.submitList(connections.map { it.user })



    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}