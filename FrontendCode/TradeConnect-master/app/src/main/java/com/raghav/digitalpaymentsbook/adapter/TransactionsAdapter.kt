package com.raghav.digitalpaymentsbook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.raghav.digitalpaymentsbook.data.model.Transaction
import com.raghav.digitalpaymentsbook.databinding.ItemCustomerBinding
import com.raghav.digitalpaymentsbook.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat

class TransactionsAdapter(val settled:Boolean) : ListAdapter<Transaction, TransactionsAdapter.ViewHolder>(COMPARATOR) {

    class ViewHolder(val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root)

    companion object COMPARATOR : DiffUtil.ItemCallback<Transaction>(){
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem==newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.apply {
            val c = currentList[holder.adapterPosition]
            name.text = c.transactionName
            date.text = SimpleDateFormat("dd/MM/yyyy").format(c.createdAt)
            due.text = c.due.toString()
            due.isVisible = !settled
            pay.isVisible = !settled
        }
    }

    interface OnItemClickListener{
        fun onItemClick(transaction: Transaction)
    }

}