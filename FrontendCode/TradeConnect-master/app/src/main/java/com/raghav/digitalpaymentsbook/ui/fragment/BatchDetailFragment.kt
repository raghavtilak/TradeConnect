package com.raghav.digitalpaymentsbook.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.raghav.digitalpaymentsbook.data.model.Batch
import com.raghav.digitalpaymentsbook.databinding.FragmentBatchDetailBinding
import com.raghav.digitalpaymentsbook.ui.activity.UpdateBatchActivity
import java.text.SimpleDateFormat

class BatchDetailFragment(private val showUpdateOption: Boolean) : Fragment() {

    private var _binding: FragmentBatchDetailBinding? = null
    val binding: FragmentBatchDetailBinding
        get() = _binding!!

    var batch: Batch? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBatchDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        batch = arguments?.getParcelable("batch")

        batch?.let {


            binding.batchNo.text = "Batch No: ${it.batchNo}"
            binding.mrp.text = "MRP: ₹${it.MRP}"
            it.mfg?.let { mfg ->
                binding.mfgdate.text = "Mfg: ${SimpleDateFormat("dd/MM/yyyy").format(mfg)}"
            }
            it.expiry?.let { expdate ->
                binding.expdate.text = "Exp: ${SimpleDateFormat("dd/MM/yyyy").format(expdate)}"
            }
            binding.productName.text = "Product Name: ${it.productName}"
            binding.quantity.text = "Quantity: ${it.quantity}"
            it.buyingPrice?.let { buyPrice -> binding.buyPrice.text = "Buy Price: ₹${buyPrice}" }
            binding.sellPrice.text = "Sell Price: ₹${it.sellingPrice}"

            binding.editBatch.isVisible = showUpdateOption

            if (showUpdateOption)
                binding.editBatch.setOnClickListener { v ->
                    val i = Intent(requireActivity(), UpdateBatchActivity::class.java)
                    i.putExtra("batch", it)
                    startActivity(i)
                }


        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}