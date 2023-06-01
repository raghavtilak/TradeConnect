package com.raghav.digitalpaymentsbook.ui.fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.raghav.digitalpaymentsbook.data.model.Connection
import com.raghav.digitalpaymentsbook.data.model.enums.ConnectionStatus
import com.raghav.digitalpaymentsbook.data.model.Retailer
import com.raghav.digitalpaymentsbook.databinding.FragmentRetailerDetailsBinding
import org.bson.types.ObjectId

class RetailerDetailsFragment(
    val retailer: Retailer,
    val status: ConnectionStatus,
    val connection: Connection?,
    val onUpdateStatus: (status: ConnectionStatus, connId:ObjectId?) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentRetailerDetailsBinding? = null
    val binding: FragmentRetailerDetailsBinding
        get() = _binding!!

    constructor(retailer: Retailer):this(retailer,ConnectionStatus.accepted,null,{ status, connId ->  })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRetailerDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bottomSheet = view.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)

        connection?.let {
            binding.accept.isVisible = !it.isCreatedByUser
            binding.decline.isVisible = !it.isCreatedByUser
        }


        binding.name.text = "Name:${retailer.name}"
        binding.email.text = "Email:${retailer.email}"
        binding.phone.text = "Phone:${retailer.phone}"
        binding.address.text = "Address:${retailer.address}"
        binding.businessName.text = "Business Name:${retailer.businessName}"

        if (status != ConnectionStatus.pending){
            binding.linear.isVisible = false
        }else{
            binding.decline.setOnClickListener {
                onUpdateStatus(ConnectionStatus.rejected, connection?.id)
                dismiss()
            }
            binding.accept.setOnClickListener {
                Log.d("TAG","CONN ID RDF= ${connection?.id}")

                onUpdateStatus(ConnectionStatus.accepted, connection?.id)
                dismiss()
            }
        }
        //TODO: show business type
        binding.businessType.text = "Business Type:${retailer.businessType}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}