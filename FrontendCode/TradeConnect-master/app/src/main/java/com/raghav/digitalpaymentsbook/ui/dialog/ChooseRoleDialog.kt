package com.raghav.digitalpaymentsbook.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.raghav.digitalpaymentsbook.databinding.DialogChooseRoleBinding
import com.raghav.digitalpaymentsbook.util.setupWidthToMatchParent

class ChooseRoleDialog(
    val title: String,
    val onRetailerClick: () -> Unit,
    val onCustomerClick: () -> Unit
) : DialogFragment() {

    var _binding: DialogChooseRoleBinding? = null
    val binding: DialogChooseRoleBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogChooseRoleBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        setWidthPercent(65)
        setupWidthToMatchParent()

        binding.title.text = title
        binding.customerBtn.setOnClickListener {

            onCustomerClick()
            dialog?.dismiss()
        }
        binding.retailerBtn.setOnClickListener {

            onRetailerClick()
            dialog?.dismiss()
        }
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}