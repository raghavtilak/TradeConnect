package com.raghav.digitalpaymentsbook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.raghav.digitalpaymentsbook.data.model.Retailer
import com.raghav.digitalpaymentsbook.databinding.ItemRetailerBinding

class RetailerAdapter(private val onItemClickListener: (retailer: Retailer)->Unit) : ListAdapter<Retailer, RetailerAdapter.ViewHolder>(COMPARATOR) {

    class ViewHolder(val binding: ItemRetailerBinding) : RecyclerView.ViewHolder(binding.root)

    companion object COMPARATOR : DiffUtil.ItemCallback<Retailer>(){
        override fun areItemsTheSame(oldItem: Retailer, newItem: Retailer): Boolean {
            return oldItem.phone == newItem.phone
        }

        override fun areContentsTheSame(oldItem: Retailer, newItem: Retailer): Boolean {
            return oldItem==newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRetailerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.apply {
            val c = currentList[holder.adapterPosition]

            retailerName.text = c.name
            businessName.text = c.businessName
            phone.text = c.phone
            avatarText.text = if(c.businessName.contains(" "))
                "${c.businessName.split(" ")[0][0]}${c.businessName.split(" ")[1][0]}"
            else
                c.businessName[0].toString()

            root.setOnClickListener {
                onItemClickListener(c)
            }
        }
    }


}