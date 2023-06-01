package com.raghav.digitalpaymentsbook.ui.dialog

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.DialogAddCustomerBinding
import com.raghav.digitalpaymentsbook.ui.viewmodel.MainViewmodel
import kotlinx.coroutines.launch

class AddCustomerDialog : BottomSheetDialogFragment() {

    var _binding : DialogAddCustomerBinding? = null
    val binding
    get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = DialogAddCustomerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bottomSheet = view.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)

        val viewmodel = ViewModelProvider(requireActivity())[MainViewmodel::class.java]

        binding.editTextSearch.setOnEditorActionListener{ v, actionId, event ->

            if(binding.editTextSearch.text?.length!=10){
                Toast.makeText(requireActivity(),"Invalid phone number",Toast.LENGTH_SHORT).show()
                return@setOnEditorActionListener false
            }else{
                val loadingDialog = LoadingDialog()
                loadingDialog.isCancelable = false
                loadingDialog.show(requireActivity().supportFragmentManager,"loading")
                val phone = binding.editTextSearch.text?.toString()!!
                return@setOnEditorActionListener true
            }
        }

    }

    /*
    * OPENING BOTTOM SHEET
    * AppAddBottomSheetFragment addBottomDialogFragment = new
                    AppAddBottomSheetFragment();
            addBottomDialogFragment.show(requireActivity().getSupportFragmentManager(),
                    "TAG");
    *
    * */

    fun showBottomSheet() {
        if ((this.dialog as BottomSheetDialog?)!!.behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            (this.dialog as BottomSheetDialog?)!!.behavior.state =
                BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}