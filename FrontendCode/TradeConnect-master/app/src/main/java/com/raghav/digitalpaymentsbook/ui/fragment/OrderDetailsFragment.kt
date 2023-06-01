package com.raghav.digitalpaymentsbook.ui.fragment

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.raghav.digitalpaymentsbook.R
import com.raghav.digitalpaymentsbook.data.model.Order
import com.raghav.digitalpaymentsbook.data.model.enums.OrderStatus
import com.raghav.digitalpaymentsbook.databinding.FragmentOrderDetailsBinding
import java.text.SimpleDateFormat

class OrderDetailsFragment(
    val order: Order,
    val onUpdateStatus: (status: OrderStatus) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FragmentOrderDetailsBinding? = null
    val binding: FragmentOrderDetailsBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOrderDetailsBinding.inflate(inflater, container, false)

        return binding.root
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bottomSheet = view.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)

//        val arg = arguments?.getParcelable<Order>("order")
//        arg?.let { order ->


        with(binding) {

            accept.isVisible = !order.isCreatedByUser || order.status != OrderStatus.declined
            decline.isVisible = !order.isCreatedByUser || order.status != OrderStatus.declined

            createdAt.text = SimpleDateFormat("dd/MM/yyyy").format(order.createdAt)
            total.text = "Total: ₹${order.total}"
            businessName.text = "Business Name: ${order.user.businessName}"
            name.text = "Name: ${order.user.name}"
            email.text = "Email: ${order.user.email}"

                if(order.isCreatedByUser)
                    icon.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_baseline_arrow_outward_24))
                else
                    icon.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_baseline_arrow_inward_24))

            type.text = if(order.isCreatedByUser) "Sent to" else "Received From"

            batchDetail.setOnClickListener {
                val frag = BatchsDetailContainerFragment(showUpdateOption = false)
                val bundle = Bundle()
                bundle.putParcelableArrayList("batches", ArrayList(order.batches))
                frag.arguments = bundle
                frag.show(parentFragmentManager, "batchDetails")
            }


//            }

            if (order.status != OrderStatus.active) {
                linear.isVisible = false
            } else {
                decline.setOnClickListener {
                    onUpdateStatus(OrderStatus.declined)
                    dismiss()
                }
                accept.setOnClickListener {
                    onUpdateStatus(OrderStatus.inactive)
                    dismiss()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}