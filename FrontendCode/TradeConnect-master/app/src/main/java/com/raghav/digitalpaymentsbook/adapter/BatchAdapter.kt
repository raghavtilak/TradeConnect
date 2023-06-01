package com.raghav.digitalpaymentsbook.adapter

import androidx.core.view.isVisible
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.raghav.digitalpaymentsbook.data.model.Batch
import com.raghav.digitalpaymentsbook.databinding.ItemCustomerBinding
import com.raghav.digitalpaymentsbook.databinding.ItemBatchBinding

class BatchAdapter(private val onRemoveListener: (Batch: Batch)->Unit) : ListAdapter<Batch, BatchAdapter.ViewHolder>(COMPARATOR) {

    class ViewHolder(val binding: ItemBatchBinding) : RecyclerView.ViewHolder(binding.root)

    companion object COMPARATOR : DiffUtil.ItemCallback<Batch>(){
        override fun areItemsTheSame(oldItem: Batch, newItem: Batch): Boolean {
            return oldItem.batchNo == newItem.batchNo
        }

        override fun areContentsTheSame(oldItem: Batch, newItem: Batch): Boolean {
            return oldItem==newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBatchBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.apply {
            val p = currentList[holder.adapterPosition]
            productName.text = p.productName
            batchNo.text = "Batch No: ${p.batchNo}"
            quantity.text = "Quantity: ${p.quantity}"
            mrp.text = "MRP: ₹${p.MRP}"
            sellPrice.text = "Sell Price: ₹${p.sellingPrice}"

            removeBtn.isVisible = true
            removeBtn.setOnClickListener {
                onRemoveListener(p)
            }
        }
    }
}