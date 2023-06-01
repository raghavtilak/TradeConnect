package com.raghav.digitalpaymentsbook.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.raghav.digitalpaymentsbook.adapter.BatchAdapter
import com.raghav.digitalpaymentsbook.adapter.spinner.BatchSpinnerAdapter
import com.raghav.digitalpaymentsbook.adapter.spinner.ProductSpinnerAdapter
import com.raghav.digitalpaymentsbook.adapter.spinner.RetailerSpinnerAdapter
import com.raghav.digitalpaymentsbook.data.model.Batch
import com.raghav.digitalpaymentsbook.data.model.Retailer
import com.raghav.digitalpaymentsbook.data.model.apis.RetailerProduct
import com.raghav.digitalpaymentsbook.data.model.enums.ConnectionStatus
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.ActivityCreateOrderBinding
import com.raghav.digitalpaymentsbook.databinding.ItemBatchBinding
import com.raghav.digitalpaymentsbook.databinding.ItemRetailerBinding
import com.raghav.digitalpaymentsbook.ui.dialog.LoadingDialog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject


class CreateOrderActivity : AppCompatActivity() {

    lateinit var binding: ActivityCreateOrderBinding
    val loadingDialog = LoadingDialog()

    val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        Log.d(
            "TAG",
            "ERROR=${throwable.message} ${throwable.printStackTrace()}"
        )
    }
    var orderBatchAdapter: BatchAdapter? = null

    lateinit var retAdapter : RetailerSpinnerAdapter
    lateinit var prodAdapter : ProductSpinnerAdapter
    lateinit var batchAdapter : BatchSpinnerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)


        retAdapter = RetailerSpinnerAdapter(binding.chooseRet)
        prodAdapter = ProductSpinnerAdapter(binding.chooseProduct)
        batchAdapter = BatchSpinnerAdapter(binding.chooseBatch)

        binding.chooseRet.apply {
            setSpinnerAdapter(retAdapter)
            setItems(mutableListOf<Retailer>())
            getSpinnerRecyclerView().layoutManager = LinearLayoutManager(context)
//            selectItemByIndex(0) // select a default item.
            lifecycleOwner = this@CreateOrderActivity
        }

        binding.chooseProduct.apply {
            setSpinnerAdapter(prodAdapter)
            setItems(mutableListOf<RetailerProduct>())
            getSpinnerRecyclerView().layoutManager = LinearLayoutManager(context)
//            selectItemByIndex(0) // select a default item.
            lifecycleOwner = this@CreateOrderActivity
        }

        binding.chooseBatch.apply {
            setSpinnerAdapter(batchAdapter)
            setItems(mutableListOf<Batch>())
            getSpinnerRecyclerView().layoutManager = LinearLayoutManager(context)
//            selectItemByIndex(0) // select a default item.
            lifecycleOwner = this@CreateOrderActivity
        }



        lifecycleScope.launch(handler) {
            loadingDialog.show(supportFragmentManager, "loading")
            val job =
                async {
                    RetrofitHelper.getInstance(this@CreateOrderActivity)
                        .getMyConnections(ConnectionStatus.accepted)
                }
            val response = job.await()
            if (response.isSuccessful && response.body() != null) {
                loadingDialog.dismiss()
                if (response.body()!!.isEmpty()) {
                    Toast.makeText(
                        this@CreateOrderActivity,
                        "You don't have any connections, to create an order",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    binding.chooseRet.setItems(response.body()!!.map { it.user }.toMutableList())

                    updateUi()
                }
            } else {
                Toast.makeText(
                    this@CreateOrderActivity,
                    "Some error occurred. Couldn't load your connections.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("TAG", "Couldn't load connections")
            }
        }


    }

    private fun updateUi() {

        binding.chooseProduct.setOnSpinnerItemSelectedListener<RetailerProduct> { oldIndex, oldItem, newIndex, newItem ->
            binding.chooseBatch.setItems(newItem.batches)
            binding.productDetails.isVisible = false
            binding.chooseBatch.selectItemByIndex(0)

        }

        binding.chooseRet.setOnSpinnerItemSelectedListener<Retailer> {
                oldIndex, oldItem, newIndex, newItem ->
            loadingDialog.show(supportFragmentManager, "loading")

            binding.productDetails.isVisible = false
            orderBatchAdapter?.submitList(mutableListOf())
            binding.editTextQuantity.text?.clear()
            prodAdapter.setItems(mutableListOf())
            batchAdapter.setItems(mutableListOf())

            lifecycleScope.launch(handler) {
                val job =
                    async {
                        RetrofitHelper.getInstance(this@CreateOrderActivity)
                            .getRetailerProducts(newItem.id)
                    }
                val response = job.await()
                if (response.isSuccessful && response.body() != null) {

                    if (response.body()!!.isEmpty()) {
                        Toast.makeText(
                            this@CreateOrderActivity,
                            "Retailer doesn't have any products",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadingDialog.dismiss()
                        batchAdapter.setItems(mutableListOf())
                        prodAdapter.setItems(mutableListOf())
                    }else{

                        when(response.body()!!.size){
                            in 1..4 -> binding.chooseProduct.spinnerPopupHeight = 500
                            in 5..10 -> binding.chooseProduct.spinnerPopupHeight = 700
                            else -> binding.chooseProduct.spinnerPopupHeight = 1000
                        }
                        prodAdapter.setItems(response.body()!!)

                        loadingDialog.dismiss()
                    }
//                        prodAdapter.clear()
//                        prodAdapter.addAll(response.body()!!)


                } else {
                    Toast.makeText(
                        this@CreateOrderActivity,
                        "Can't load products",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadingDialog.dismiss()
                }
            }
        }

        binding.chooseBatch.setOnSpinnerItemSelectedListener<Batch> { oldIndex, oldItem, newIndex, newItem ->

            binding.TextFieldQuantity.isErrorEnabled = false
            binding.productDetails.isVisible = true
            with(binding) {
                val c = newItem
                productName.text = c.productName
                quantity.text = "Quantity: ${c.quantity}"
                mrp.text = "MRP: ₹${c.MRP}"
                batchNo.text = "Batch No: ${c.batchNo}"
            }
        }


        orderBatchAdapter = BatchAdapter {
            val nl= mutableListOf<Batch>()
            nl.addAll(orderBatchAdapter?.currentList!!)
            nl.remove(it)
            orderBatchAdapter?.submitList(nl)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = orderBatchAdapter


        binding.addBatch.setOnClickListener {
            if (binding.editTextQuantity.text.isNullOrBlank()) {
                binding.TextFieldQuantity.error = "Quantity required"
                binding.TextFieldQuantity.isErrorEnabled = true
            } else if (binding.editTextQuantity.text.toString()
                    .toInt() > batchAdapter.spinnerItems[binding.chooseBatch.selectedIndex].quantity
            ) {
                Toast.makeText(
                    this,
                    "The quantity should be less than available quantity",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val nl= mutableListOf<Batch>()
                nl.addAll(orderBatchAdapter!!.currentList)
                val item = batchAdapter.spinnerItems[binding.chooseBatch.selectedIndex]
                val batch = item.copy(
                    id = item.id,
                    batchNo = item.batchNo,
                    MRP = item.MRP,
                    mfg = item.mfg,
                    expiry = item.expiry,
                    productName = item.productName,
                    quantity = binding.editTextQuantity.text.toString().toInt(),
                    buyingPrice = item.buyingPrice,
                    sellingPrice = item.sellingPrice,
                    isUpdateAllowed = item.isUpdateAllowed
                )
                nl.add(batch)
                orderBatchAdapter?.submitList(nl)
            }
        }
        binding.createOrder.setOnClickListener {

            if (orderBatchAdapter!!.itemCount == 0) {
                Toast.makeText(this, "There are no products", Toast.LENGTH_SHORT).show()
            } else {


                lifecycleScope.launch(handler) {

                    val order = JSONObject()
                    order.put(
                        "receiverId",
                        retAdapter.spinnerItems[binding.chooseRet.selectedIndex].id.toHexString()
                    )
                    order.put(
                        "totalAmount",
                        orderBatchAdapter!!.currentList.sumOf { it.quantity * it.sellingPrice })

                    val batches = JSONArray()
                    orderBatchAdapter!!.currentList.forEach {
                        val batch = JSONObject()
                        batch.put("batchNo", it.batchNo)
                        batch.put("quantity", it.quantity)
                        batch.put("price", it.sellingPrice)
                        batches.put(batch)
                    }
                    order.put("batches", batches)

                    val body =
                        order.toString().toRequestBody("application/json".toMediaTypeOrNull())

                    val job =
                        async {
                            RetrofitHelper.getInstance(this@CreateOrderActivity)
                                .createOrder(body)
                        }
                    val response = job.await()
                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(
                            this@CreateOrderActivity,
                            response.body()?.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@CreateOrderActivity,
                            response.body()?.error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }
        }
    }
}