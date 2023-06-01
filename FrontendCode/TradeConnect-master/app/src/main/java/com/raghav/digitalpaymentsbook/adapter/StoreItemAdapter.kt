package com.raghav.digitalpaymentsbook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.raghav.digitalpaymentsbook.data.model.StoreItem
import com.raghav.digitalpaymentsbook.databinding.ItemRetailerBinding
import com.raghav.digitalpaymentsbook.databinding.ItemStoreBinding

class StoreItemAdapter (private val onItemClickListener: (retailer: StoreItem)->Unit) : ListAdapter<StoreItem, StoreItemAdapter.ViewHolder>(COMPARATOR) {

    class ViewHolder(val binding: ItemStoreBinding) : RecyclerView.ViewHolder(binding.root)

    companion object COMPARATOR : DiffUtil.ItemCallback<StoreItem>(){
        override fun areItemsTheSame(oldItem: StoreItem, newItem: StoreItem): Boolean {
            return oldItem.sku == newItem.sku
        }

        override fun areContentsTheSame(oldItem: StoreItem, newItem: StoreItem): Boolean {
            return oldItem==newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStoreBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.apply {
            val c = currentList[holder.adapterPosition]
            productName.text = c.productName
            batchCount.text = "${c.batchIds.size} batch(s)"
            quantity.text = "Quantity: ${c.quantity}"
            sku.text = "SKU: ${c.sku}"

            root.setOnClickListener {
                onItemClickListener(c)
            }
        }
    }


}