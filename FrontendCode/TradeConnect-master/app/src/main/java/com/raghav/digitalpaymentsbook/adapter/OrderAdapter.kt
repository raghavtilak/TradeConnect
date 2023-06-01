package com.raghav.digitalpaymentsbook.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.raghav.digitalpaymentsbook.R
import com.raghav.digitalpaymentsbook.data.model.Order
import com.raghav.digitalpaymentsbook.data.model.enums.OrderStatus
import com.raghav.digitalpaymentsbook.databinding.ItemOrderBinding
import java.text.SimpleDateFormat

class OrderAdapter(private val onItemClickListener: (Order: Order)->Unit) : ListAdapter<Order, OrderAdapter.ViewHolder>(COMPARATOR) {

    class ViewHolder(val binding: ItemOrderBinding,val parent: ViewGroup) : RecyclerView.ViewHolder(binding.root)

    companion object COMPARATOR : DiffUtil.ItemCallback<Order>(){
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem==newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding,parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.apply {
            val c = currentList[holder.adapterPosition]

            when(c.status){
                OrderStatus.active -> {
                    status.text = "Active"
                    status.setTextColor(ColorStateList.valueOf(Color.BLACK))
                    status.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                }
                OrderStatus.inactive -> {
                    status.isVisible = false
                }
                OrderStatus.cancelled -> {
                    status.text = "Cancelled"
                    status.setTextColor(ColorStateList.valueOf(Color.WHITE))
                    status.backgroundTintList = ColorStateList.valueOf(Color.RED)
                }
                OrderStatus.declined -> {
                    status.text = "Declined"
                    status.setTextColor(ColorStateList.valueOf(Color.WHITE))
                    status.backgroundTintList = ColorStateList.valueOf(Color.RED)
                }
            }

            if(c.isCreatedByUser)
                icon.setImageDrawable(holder.parent.context.getDrawable(R.drawable.ic_baseline_arrow_outward_24))
             else
                 icon.setImageDrawable(holder.parent.context.getDrawable(R.drawable.ic_baseline_arrow_inward_24))

            businessName.text = c.user.businessName
            createdDate.text = SimpleDateFormat("dd/MM/yyyy").format(c.createdAt)
            total.text = "₹ ${c.total}"

            root.setOnClickListener {
                onItemClickListener(c)
            }
        }
    }


}