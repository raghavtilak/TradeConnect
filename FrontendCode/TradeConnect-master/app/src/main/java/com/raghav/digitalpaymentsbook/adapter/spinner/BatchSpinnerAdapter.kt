package com.raghav.digitalpaymentsbook.adapter.spinner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.raghav.digitalpaymentsbook.data.model.Batch
import com.raghav.digitalpaymentsbook.databinding.ItemBatchBinding
import com.raghav.digitalpaymentsbook.databinding.ItemRetailerBinding
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener
import com.skydoves.powerspinner.PowerSpinnerInterface
import com.skydoves.powerspinner.PowerSpinnerView

class BatchSpinnerAdapter(
    powerSpinnerView: PowerSpinnerView
) : RecyclerView.Adapter<BatchSpinnerAdapter.IconSpinnerViewHolder>(),
    PowerSpinnerInterface<Batch> {

    private val NO_SELECTED_INDEX: Int = -1
    override var index: Int = powerSpinnerView.selectedIndex
    override val spinnerView: PowerSpinnerView = powerSpinnerView
    override var onSpinnerItemSelectedListener: OnSpinnerItemSelectedListener<Batch>? = null

    val spinnerItems: MutableList<Batch> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconSpinnerViewHolder {
        val binding =
            ItemBatchBinding.inflate(
                LayoutInflater.from(parent.context), parent,
                false
            )
        return IconSpinnerViewHolder(binding).apply {
            binding.root.setOnClickListener {
                val position = adapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                    ?: return@setOnClickListener
                notifyItemSelected(position)
            }
        }
    }

    override fun onBindViewHolder(holder: IconSpinnerViewHolder, position: Int) {
        holder.bind(spinnerItems[position], spinnerView)
    }

    override fun setItems(itemList: List<Batch>) {
        this.spinnerItems.clear()
        this.spinnerItems.addAll(itemList)
        notifyDataSetChanged()
    }

    override fun notifyItemSelected(index: Int) {
        if (index == NO_SELECTED_INDEX) return
        val oldIndex = this.index
        this.index = index
        this.spinnerView.notifyItemSelected(index, this.spinnerItems[index].batchNo)
        this.onSpinnerItemSelectedListener?.onItemSelected(
            oldIndex = oldIndex,
            oldItem = oldIndex.takeIf { it != NO_SELECTED_INDEX }?.let { spinnerItems[oldIndex] },
            newIndex = index,
            newItem = this.spinnerItems[index]
        )
    }

    override fun getItemCount() = this.spinnerItems.size

    class IconSpinnerViewHolder(private val binding: ItemBatchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Batch, spinnerView: PowerSpinnerView) {
            // do something using a custom item.
            with(binding) {
                val c = item
                productName.text = c.productName
                quantity.text = "Quantity: ${c.quantity}"
                mrp.text = "MRP: ₹${c.MRP}"
                sellPrice.text = "Sell Price: ₹${c.sellingPrice}"
                batchNo.text = "Batch No: ${c.batchNo}"
            }
        }
    }
}