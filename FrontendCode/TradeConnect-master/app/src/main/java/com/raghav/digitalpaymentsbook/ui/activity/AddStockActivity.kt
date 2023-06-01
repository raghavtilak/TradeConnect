package com.raghav.digitalpaymentsbook.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.ActivityAddStockBinding
import com.raghav.digitalpaymentsbook.ui.dialog.LoadingDialog
import com.raghav.digitalpaymentsbook.ui.fragment.DatePickerFragment
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class AddStockActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddStockBinding

    val loadingDialog = LoadingDialog()

    val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        Log.d(
            "TAG",
            "ERROR=${throwable.message} , ${throwable.printStackTrace()}"
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStockBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.addStock.setOnClickListener {

            if (validate()) {
                loadingDialog.show(supportFragmentManager, "loading")

                val batchNo = binding.editTextBatchNo.text.toString()
                val MRP = binding.editTextMrp.text.toString()
                val mfg = binding.mfgTV.text.toString()
                val expiry = binding.expTV.text.toString()
                val productName = binding.editTextProductName.text.toString()
                val quantity = binding.editTextQuantity.text.toString()
                val buyingPrice = binding.editTextBuyPrice.text.toString()
                val sellingPrice = binding.editTextSellPrice.text.toString()

                val jsonObject = JSONObject()
                jsonObject.put("batchNo", batchNo)
                jsonObject.put("MRP", MRP)
                jsonObject.put("mfg", mfg)
                jsonObject.put("expiry", expiry)
                jsonObject.put("productName", productName)
                jsonObject.put("quantity", quantity)
                jsonObject.put("buyingPrice", buyingPrice)
                jsonObject.put("sellingPrice", sellingPrice)
                val body =
                    jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

                lifecycleScope.launch {
                    val job = async {
                        RetrofitHelper.getInstance(this@AddStockActivity)
                            .addBatchToInventory(body)
                    }
                    val res = job.await()
                    if (res.isSuccessful && res.body() != null) {
                        Toast.makeText(this@AddStockActivity, "Added stock successfully", Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    }else{
                        Toast.makeText(this@AddStockActivity, "Couldn't add stock. Some error occurred", Toast.LENGTH_SHORT)
                            .show()
                    }
                    loadingDialog.dismiss()
                }
            }
        }

        binding.mfgTV.setOnClickListener {
            val newFragment: DialogFragment = DatePickerFragment { _, year, month, day ->
                binding.mfgTV.text = Editable.Factory.getInstance().newEditable(
                    "$year-" +
                            (if (month.toString().length < 2) "0${month + 1}" else "${month + 1}") +
                            "-" + (if (day.toString().length < 2) "0${day}" else "$day"))
            }
            newFragment.show(supportFragmentManager, "datePicker")

        }

        binding.expTV.setOnClickListener {
            val newFragment: DialogFragment = DatePickerFragment { _, year, month, day ->
                binding.expTV.text =Editable.Factory.getInstance().newEditable(
                    "$year-" +
                            (if (month.toString().length < 2) "0${month + 1}" else "${month + 1}") +
                            "-" + (if (day.toString().length < 2) "0${day}" else "$day"))
            }
            newFragment.show(supportFragmentManager, "datePicker")
        }

        binding.searchCard.setOnClickListener {
            if (!binding.editTextBatchNo.text.isNullOrBlank()) {
                loadingDialog.show(supportFragmentManager,"loading")
                lifecycleScope.launch(handler) {
                    val job = async {
                        RetrofitHelper.getInstance(this@AddStockActivity)
                            .findBatch(binding.editTextBatchNo.text.toString())
                    }
                    val res = job.await()
                    if (res.isSuccessful && res.body() != null) {
                        with(res.body()!!) {
                            binding.editTextProductName.text = Editable.Factory.getInstance().newEditable(this.productName)
                            binding.editTextMrp.text = Editable.Factory.getInstance().newEditable(this.MRP.toString())
                            binding.editTextQuantity.text = Editable.Factory.getInstance().newEditable(this.quantity.toString())
                            binding.editTextBuyPrice.text = Editable.Factory.getInstance().newEditable(this.buyingPrice.toString())
                            binding.editTextSellPrice.text = Editable.Factory.getInstance().newEditable(this.sellingPrice.toString())
                            binding.mfgTV.text = Editable.Factory.getInstance().newEditable(SimpleDateFormat("yyyy-MM-dd").format(this.mfg!!))
                            binding.expTV.text = Editable.Factory.getInstance().newEditable(SimpleDateFormat("yyyy-MM-dd").format(this.expiry!!))
                        }
                    }else{
                        Toast.makeText(this@AddStockActivity, "No batch found", Toast.LENGTH_SHORT)
                            .show()
                    }
                    loadingDialog.dismiss()
                }
            }
        }

    }

    private fun validate(): Boolean {

        if (binding.editTextBatchNo.text.isNullOrBlank() ||
            binding.editTextProductName.text.isNullOrBlank() ||
            binding.editTextMrp.text.isNullOrBlank() ||
            binding.editTextQuantity.text.isNullOrBlank() ||
            binding.editTextBuyPrice.text.isNullOrBlank() ||
            binding.editTextSellPrice.text.isNullOrBlank()
        ) {
            return false
        }

        if (binding.mfgTV.text.isNullOrBlank() || binding.expTV.text.isNullOrBlank()) {
            return false
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val mfg = LocalDate.parse(binding.mfgTV.text, formatter)
        val exp = LocalDate.parse(binding.expTV.text, formatter)

        if (mfg > exp) return false

        return true
    }

}