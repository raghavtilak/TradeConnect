package com.raghav.digitalpaymentsbook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.raghav.digitalpaymentsbook.data.model.Customer
import com.raghav.digitalpaymentsbook.databinding.ItemCustomerBinding

class CustomerAdapter(private val onItemClickListener: OnItemClickListener) : ListAdapter<Customer, CustomerAdapter.ViewHolder>(COMPARATOR) {

    class ViewHolder(val binding: ItemCustomerBinding) : RecyclerView.ViewHolder(binding.root)

    companion object COMPARATOR : DiffUtil.ItemCallback<Customer>(){
        override fun areItemsTheSame(oldItem: Customer, newItem: Customer): Boolean {
            return oldItem.phone == newItem.phone
        }

        override fun areContentsTheSame(oldItem: Customer, newItem: Customer): Boolean {
            return oldItem==newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.apply {
            val c = currentList[holder.adapterPosition]
            name.text = c.name
            phone.text = c.phone.toString()
            avatarText.text = if(c.name.contains(" "))
                "${c.name.split(" ")[0][0]}${c.name.split(" ")[1][0]}"
            else
                c.name[0].toString()
            root.setOnClickListener {
                onItemClickListener.onItemClick(c)
            }
        }
    }

    interface OnItemClickListener{
        fun onItemClick(customer: Customer)
    }

}