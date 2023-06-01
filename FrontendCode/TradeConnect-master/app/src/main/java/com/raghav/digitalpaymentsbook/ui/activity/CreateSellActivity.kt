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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.raghav.digitalpaymentsbook.adapter.BatchAdapter
import com.raghav.digitalpaymentsbook.adapter.spinner.BatchSpinnerAdapter
import com.raghav.digitalpaymentsbook.adapter.spinner.ProductSpinnerAdapter
import com.raghav.digitalpaymentsbook.adapter.spinner.RetailerSpinnerAdapter
import com.raghav.digitalpaymentsbook.data.model.Batch
import com.raghav.digitalpaymentsbook.data.model.Product
import com.raghav.digitalpaymentsbook.data.model.Retailer
import com.raghav.digitalpaymentsbook.data.model.apis.RetailerProduct
import com.raghav.digitalpaymentsbook.data.model.enums.ConnectionStatus
import com.raghav.digitalpaymentsbook.data.model.enums.UserRole
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.ActivityCreateSellBinding
import com.raghav.digitalpaymentsbook.databinding.ItemBatchBinding
import com.raghav.digitalpaymentsbook.databinding.ItemRetailerBinding
import com.raghav.digitalpaymentsbook.ui.dialog.LoadingDialog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject


class CreateSellActivity : AppCompatActivity() {

    lateinit var binding: ActivityCreateSellBinding
    val loadingDialog = LoadingDialog()

    val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        Log.d(
            "TAG",
            "ERROR=${throwable.message} ${throwable.printStackTrace()}"
        )
    }

    var role : UserRole = UserRole.Retailer
//    lateinit var prodAdapter: ArrayAdapter<RetailerProduct>

    var orderBatchAdapter: BatchAdapter? = null

    lateinit var retAdapter : RetailerSpinnerAdapter
    lateinit var prodAdapter : ProductSpinnerAdapter
    lateinit var batchAdapter : BatchSpinnerAdapter

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateSellBinding.inflate(layoutInflater)
        setContentView(binding.root)

        role = intent.getSerializableExtra("role") as UserRole

        when(role){
            UserRole.Retailer -> {
                binding.custGroup.isVisible = false
                binding.chooseRetCard.isVisible = true

            }
            UserRole.Customer -> {
                binding.chooseRetCard.isVisible = false
                binding.custGroup.isVisible = true

                binding.chooseProductCard.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    topToBottom = binding.customerEmailCard.id
                }
            }
        }


        retAdapter = RetailerSpinnerAdapter(binding.chooseRet)
        prodAdapter = ProductSpinnerAdapter(binding.chooseProduct)
        batchAdapter = BatchSpinnerAdapter(binding.chooseBatch)

        binding.chooseRet.apply {
            setSpinnerAdapter(retAdapter)
            setItems(mutableListOf<Retailer>())
            getSpinnerRecyclerView().layoutManager = LinearLayoutManager(context)
//            selectItemByIndex(0) // select a default item.
            lifecycleOwner = this@CreateSellActivity
        }

        binding.chooseProduct.apply {
            setSpinnerAdapter(prodAdapter)
            setItems(mutableListOf<RetailerProduct>())
            getSpinnerRecyclerView().layoutManager = LinearLayoutManager(context)
//            selectItemByIndex(0) // select a default item.
            lifecycleOwner = this@CreateSellActivity
        }

        binding.chooseBatch.apply {
            setSpinnerAdapter(batchAdapter)
            setItems(mutableListOf<Batch>())
            getSpinnerRecyclerView().layoutManager = LinearLayoutManager(context)
//            selectItemByIndex(0) // select a default item.
            lifecycleOwner = this@CreateSellActivity
        }
        
        lifecycleScope.launch(handler) {
            loadingDialog.show(supportFragmentManager, "loading")
            val job1 =
                async {
                    RetrofitHelper.getInstance(this@CreateSellActivity)
                        .getMyConnections(ConnectionStatus.accepted)
                }
            val job2 =
                async {
                    RetrofitHelper.getInstance(this@CreateSellActivity)
                        .getMyProducts()
                }

            val res1 = job1.await()
            val res2 = job2.await()

            if (res1.isSuccessful && res1.body() != null) {
                loadingDialog.dismiss()
                if (res1.body()!!.isEmpty()) {
                    Toast.makeText(
                        this@CreateSellActivity,
                        "You don't have any connections, to create a sell",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    binding.chooseRet.setItems(res1.body()!!.map { it.user }.toMutableList())

                    if (res2.isSuccessful && res2.body() != null){
                        when(res2.body()!!.size){
                            in 1..4 -> binding.chooseProduct.spinnerPopupHeight = 500
                            in 5..10 -> binding.chooseProduct.spinnerPopupHeight = 700
                            else -> binding.chooseProduct.spinnerPopupHeight = 1000
                        }
                        prodAdapter.setItems(res2.body()!!)
                        updateUi()
                    }else{
                        Toast.makeText(
                            this@CreateSellActivity,
                            "You don't have any products, to create a sell",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this@CreateSellActivity,
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

        binding.productDetails.isVisible = false
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

        var orderBatchAdapter: BatchAdapter? = null
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

            when(role){
                UserRole.Retailer -> {
                    if (orderBatchAdapter.itemCount == 0) {
                        Toast.makeText(this, "There are no products", Toast.LENGTH_SHORT).show()
                    }else if(binding.editTextPaid.text.isNullOrBlank()){
                        binding.TextFieldPaid.isErrorEnabled = true
                        binding.TextFieldPaid.error = "This amount is required"
//                Toast.makeText(this, "There are no products", Toast.LENGTH_SHORT).show()
                    }else if(binding.editTextPaid.text.toString().toInt() > orderBatchAdapter.currentList.sumOf { it.sellingPrice }){
                        Toast.makeText(this, "Paid amount can't be greater than total sell amount", Toast.LENGTH_LONG).show()

                    }
                    else {


                        lifecycleScope.launch(handler) {

                            val order = JSONObject()
                            order.put(
                                "toRetailerId",
                                retAdapter.spinnerItems[binding.chooseRet.selectedIndex].id.toHexString()
                            )
                            order.put(
                                "totalPrice",
                                orderBatchAdapter.currentList.sumOf { it.quantity * it.sellingPrice })

                            order.put(
                                "paid",
                                binding.editTextPaid.text.toString().toInt())


                            val batches = JSONArray()
                            orderBatchAdapter.currentList.forEach {
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
                                    RetrofitHelper.getInstance(this@CreateSellActivity)
                                        .createSell(body)
                                }
                            val response = job.await()
                            if (response.isSuccessful && response.body() != null) {
                                Toast.makeText(
                                    this@CreateSellActivity,
                                    response.body()?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } else {
                                Toast.makeText(
                                    this@CreateSellActivity,
                                    response.body()?.error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    }
                }
                UserRole.Customer -> {
                    if (orderBatchAdapter.itemCount == 0) {
                        Toast.makeText(this, "There are no products", Toast.LENGTH_SHORT).show()
                    }else if(binding.editTextPaid.text.isNullOrBlank()){
                        binding.TextFieldPaid.isErrorEnabled = true
                        binding.TextFieldPaid.error = "This amount is required"
//                Toast.makeText(this, "There are no products", Toast.LENGTH_SHORT).show()
                    }else if(binding.editTextPaid.text.toString().toInt() > orderBatchAdapter.currentList.sumOf { it.sellingPrice }){
                        Toast.makeText(this, "Paid amount can't be greater than total sell amount", Toast.LENGTH_LONG).show()

                    }
                    else if(binding.editTextCustName.text.isNullOrBlank()){
                        binding.TextFieldCustName.isErrorEnabled = true
                        binding.TextFieldCustName.error = "Name required"
                    }
                    else if(binding.editTextCustEmail.text.isNullOrBlank()){
                        binding.TextFieldCustEmail.isErrorEnabled = true
                        binding.TextFieldCustEmail.error = "Email is required"
                    }
                    else {


                        lifecycleScope.launch(handler) {

                            val order = JSONObject()
                            order.put(
                                "isCustomerSale",
                                true
                            )
                            order.put(
                                "totalPrice",
                                orderBatchAdapter.currentList.sumOf {it.quantity * it.sellingPrice })

                            //TODO Paid
                            order.put(
                                "paid",
                                binding.editTextPaid.text.toString().toInt())

                            order.put(
                                "customerEmail",
                                binding.editTextCustEmail.text.toString())

                            order.put(
                                "customerName",
                                binding.editTextCustName.text.toString())


                            val batches = JSONArray()
                            orderBatchAdapter.currentList.forEach {
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
                                    RetrofitHelper.getInstance(this@CreateSellActivity)
                                        .createSell(body)
                                }
                            val response = job.await()
                            if (response.isSuccessful && response.body() != null) {
                                Toast.makeText(
                                    this@CreateSellActivity,
                                    response.body()?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } else {
                                Toast.makeText(
                                    this@CreateSellActivity,
                                    response.body()?.error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    }
                }
            }

        }
    }
}