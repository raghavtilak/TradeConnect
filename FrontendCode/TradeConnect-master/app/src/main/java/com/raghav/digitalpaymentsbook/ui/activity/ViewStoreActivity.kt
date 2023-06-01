package com.raghav.digitalpaymentsbook.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.raghav.digitalpaymentsbook.AndroidDownloader
import com.raghav.digitalpaymentsbook.adapter.StoreItemAdapter
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.databinding.ActivityStoreBinding
import com.raghav.digitalpaymentsbook.databinding.DialogDownloadSheetBinding
import com.raghav.digitalpaymentsbook.ui.dialog.LoadingDialog
import com.raghav.digitalpaymentsbook.ui.fragment.BatchsDetailContainerFragment
import com.raghav.digitalpaymentsbook.util.Constants
import com.raghav.digitalpaymentsbook.util.PreferenceManager
import com.raghav.digitalpaymentsbook.util.getAuthToken
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.poi.ss.usermodel.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import kotlin.math.floor
import kotlin.math.roundToInt

class ViewStoreActivity : AppCompatActivity() {
    lateinit var binding: ActivityStoreBinding

    lateinit var adapter: StoreItemAdapter

    val loadingDialog = LoadingDialog()

    val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {

            val jsonObject = JSONObject()
            jsonObject.put("batches", convertExcelToJson(it))
            val body =
                jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

            Log.d("TAG","BAT= ${jsonObject}")
            lifecycleScope.launch {
                val job = async {
                    RetrofitHelper.getInstance(this@ViewStoreActivity)
                        .addBatchesInBulk(body)
                }
                val res = job.await()
                if (res.isSuccessful && res.body() != null) {
                    Toast.makeText(this@ViewStoreActivity, res.body()!!.message, Toast.LENGTH_SHORT)
                        .show()
                }else{
                    Toast.makeText(this@ViewStoreActivity, res.body()!!.error, Toast.LENGTH_SHORT)
                        .show()
                }
                loadingDialog.dismiss()
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = StoreItemAdapter {

            loadingDialog.show(supportFragmentManager, "loading")

            lifecycleScope.launch {
                val res =
                    RetrofitHelper.getInstance(this@ViewStoreActivity).getBatchesById(it.batchIds)
                if (res.isSuccessful && res.body() != null) {
                    loadingDialog.dismiss()
                    val frag = BatchsDetailContainerFragment(true)
                    val bundle = Bundle()
                    bundle.putParcelableArrayList("batches", ArrayList(res.body()!!))
                    frag.arguments = bundle
                    frag.show(supportFragmentManager, "batchDetails")
                } else {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@ViewStoreActivity,
                        "Batch details couldn't be load",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = adapter


        updateList()

        binding.swipeRefresh.setOnRefreshListener {
            updateList()
            binding.swipeRefresh.isRefreshing = false
        }



        binding.bulkAdd.visibility = View.GONE
        binding.bulkAddTV.visibility = View.GONE
        binding.singleAdd.visibility = View.GONE
        binding.singleAddTV.visibility = View.GONE


        var isAllFabsVisible = false


        binding.addProducts.shrink()

        binding.addProducts.setOnClickListener {
            if (!isAllFabsVisible) {

                binding.bulkAdd.show()
                binding.bulkAddTV.visibility = View.VISIBLE
                binding.singleAdd.show()
                binding.singleAddTV.visibility = View.VISIBLE

                binding.addProducts.extend()

                isAllFabsVisible = true
            } else {

                binding.bulkAdd.hide()
                binding.bulkAddTV.visibility = View.GONE
                binding.singleAdd.hide()
                binding.singleAddTV.visibility = View.GONE

                binding.addProducts.shrink()

                isAllFabsVisible = false
            }
        }

        binding.bulkAdd.setOnClickListener {
            showSheetDownloadDialog()
        }
        binding.singleAdd.setOnClickListener {
            startActivity(Intent(this, AddStockActivity::class.java))
        }
    }

    private fun updateList() {
        lifecycleScope.launch {

            loadingDialog.show(supportFragmentManager, "loading")

            val result =
                RetrofitHelper.getInstance(this@ViewStoreActivity).myStore()
            if (result.isSuccessful && result.body() != null) {
                val list = result.body()!!
                adapter.submitList(list)
                Log.d("TAG", "custo her ${result.body()}")
                loadingDialog.dismiss()

            } else {
                loadingDialog.dismiss()
                Toast.makeText(
                    this@ViewStoreActivity,
                    "Some error occurred. Couldn't get your store details.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("TAG", "custo her ${result.body()}")
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun convertExcelToJson(uri: Uri): JSONArray {

        loadingDialog.show(supportFragmentManager, "loading")

        try {
            val inputStream: InputStream = contentResolver.openInputStream(uri)!!
            val output = FileOutputStream(File("${filesDir.absoluteFile}/bulk.xlsx"))
            inputStream.copyTo(output, 4 * 1024)
            inputStream.close()

            val file = FileInputStream(File("${filesDir.absoluteFile}/bulk.xlsx"))

            val workbook: Workbook = WorkbookFactory.create(file)

            val sheet: Sheet = workbook.getSheetAt(0)
            val headerRow: Row = sheet.getRow(0)

            val jsonArray = JSONArray()

            for (i in 1 until sheet.physicalNumberOfRows) {
                val currentRow: Row = sheet.getRow(i)
                val jsonObject = JSONObject()

                for (j in 0 until headerRow.physicalNumberOfCells) {
                    val cell: Cell = currentRow.getCell(j)
                    val columnName = headerRow.getCell(j).stringCellValue
//                    when (cell.cellType) {
//                        CellType.NUMERIC -> jsonObject.put(columnName, cell.numericCellValue)
//                        CellType.STRING -> jsonObject.put(columnName, cell.stringCellValue)
//                        CellType.BOOLEAN -> jsonObject.put(columnName, cell.booleanCellValue)
//                        CellType.BLANK -> jsonObject.put(columnName, "")
//                        else -> jsonObject.put(columnName, "")
//                    }
                    when (j) {
                        2, 5, 6, 7 -> {
                            jsonObject.put(columnName, cell.numericCellValue)
                            Log.d("TAG", "XL ${j}=${cell.numericCellValue}")
                        }
                        3, 4 -> {
                            jsonObject.put(
                                columnName,
                                SimpleDateFormat("yyyy-MM-dd").format(cell.dateCellValue)
                            )
                            Log.d("TAG", "XL ${j}=${cell.dateCellValue}")
                        }
                        0, 1 -> {
                            when (cell.cellType) {
                                CellType.NUMERIC -> jsonObject.put(
                                    columnName,
                                    floor(cell.numericCellValue).toString()
                                )
                                CellType.STRING -> jsonObject.put(columnName, cell.stringCellValue)
                                CellType.BOOLEAN -> jsonObject.put(
                                    columnName,
                                    cell.booleanCellValue.toString()
                                )
                                CellType.BLANK -> jsonObject.put(columnName, "")
                                else -> jsonObject.put(columnName, "")
                            }
                        }
                    }

                }
                jsonArray.put(jsonObject)
            }
            return jsonArray
        } catch (e: Exception) {
            Toast.makeText(this, "Some error occurred", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            loadingDialog.dismiss()
        }
        return JSONArray()
    }

    private fun showSheetDownloadDialog() {

        val builder = MaterialAlertDialogBuilder(this)
        var alertDialog: AlertDialog? = null


        val binding = DialogDownloadSheetBinding.inflate(layoutInflater)
        builder.setView(binding.root)

        binding.accept.setOnClickListener {
            val downloader = AndroidDownloader(this)
            downloader.downloadFile("${Constants.BASE_URL}retailer/sample_sheet",PreferenceManager.getInstance(this).getAuthToken())
            Toast.makeText(this, "File download started..", Toast.LENGTH_SHORT).show()
            alertDialog?.dismiss()
        }
        binding.decline.setOnClickListener {
            launcher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            alertDialog!!.dismiss()
        }
        alertDialog = builder.show()
        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

}