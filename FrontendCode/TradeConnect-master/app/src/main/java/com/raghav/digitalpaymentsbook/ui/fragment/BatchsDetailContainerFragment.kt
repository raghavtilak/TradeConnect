package com.raghav.digitalpaymentsbook.ui.fragment

import android.R
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.raghav.digitalpaymentsbook.AndroidDownloader
import com.raghav.digitalpaymentsbook.adapter.BatchDetailTabAdapter
import com.raghav.digitalpaymentsbook.data.model.Batch
import com.raghav.digitalpaymentsbook.data.model.SellItem
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.DialogDownloadSheetBinding
import com.raghav.digitalpaymentsbook.databinding.DialogUpdateSellBinding
import com.raghav.digitalpaymentsbook.databinding.FragmentBatchsDetailContainerBinding
import com.raghav.digitalpaymentsbook.ui.dialog.LoadingDialog
import com.raghav.digitalpaymentsbook.util.PreferenceManager
import com.raghav.digitalpaymentsbook.util.getAuthToken
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class BatchsDetailContainerFragment(val showUpdateOption:Boolean, val sellItem:SellItem?=null) : BottomSheetDialogFragment() {

    private var _binding: FragmentBatchsDetailContainerBinding? = null
    val binding: FragmentBatchsDetailContainerBinding
        get() = _binding!!

    var batches: ArrayList<Batch>? = null

    val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        Log.d(
            "TAG",
            "ERROR=${throwable.message} ${throwable.printStackTrace()}"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBatchsDetailContainerBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bottomSheet = view.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)

        batches = arguments?.getParcelableArrayList("batches")
        batches?.let {
            val adapter = BatchDetailTabAdapter(requireActivity(), it, showUpdateOption)
            binding.viewpager.adapter = adapter
            binding.viewpager.offscreenPageLimit = 5

            TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, position ->
                tab.text = "Batch ${position+1}"
            }.attach()

        }

        if(sellItem!=null && sellItem.isCreatedByUser){
            binding.updateSell.isVisible = true
            binding.updateSell.setOnClickListener {
                showSellUpdateDialog(sellItem)
            }
        }
    }

    private fun showSellUpdateDialog(sellItem: SellItem) {

        val builder = MaterialAlertDialogBuilder(requireActivity())
        var alertDialog: AlertDialog? = null


        val binding = DialogUpdateSellBinding.inflate(layoutInflater)
        builder.setView(binding.root)

        binding.total.text = "Total: ₹${sellItem.totalPrice.toString()}"
        binding.due.text = "Current Due: ₹${sellItem.due.toString()}"

        binding.accept.setOnClickListener {
            if(binding.editTextPaid.text.isNullOrBlank()){
                binding.TextFieldPaid.isErrorEnabled = true
                binding.TextFieldPaid.error = "This can't be empty"
            }else if(binding.editTextPaid.text.toString().toInt() > sellItem.due){
                binding.TextFieldPaid.isErrorEnabled = true
                binding.TextFieldPaid.error = "Paid amount can't be greater than due"
            }else{
                val loadingDialog = LoadingDialog()
                loadingDialog.show(parentFragmentManager,"loading")

                viewLifecycleOwner.lifecycleScope.launch(handler){
                    lifecycleScope.launch(handler) {
                        val jo = JSONObject()
                        jo.put("totalPrice", sellItem.totalPrice)
                        jo.put("paid", binding.editTextPaid.text.toString().toInt())

                        val body =
                            jo.toString().toRequestBody("application/json".toMediaTypeOrNull())

                        val job =
                            async { RetrofitHelper.getInstance(requireActivity()).updateSell(sellItem.id,body) }
                        val response = job.await()
                        if (response.isSuccessful && response.body() != null) {
                            Toast.makeText(requireActivity(), response.body()!!.message, Toast.LENGTH_SHORT).show()
                            alertDialog?.dismiss()
                            loadingDialog.dismiss()
                        } else {
                            Toast.makeText(requireActivity(), response.body()!!.error, Toast.LENGTH_SHORT).show()
                            loadingDialog.dismiss()
                            Log.d("TAG", "Couldn't update the sell")
                        }
                    }
                }

            }
        }
        binding.decline.setOnClickListener {
            alertDialog!!.dismiss()
        }
        alertDialog = builder.show()
        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}