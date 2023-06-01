package com.raghav.digitalpaymentsbook.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.raghav.digitalpaymentsbook.data.model.AnalyticsData
import com.raghav.digitalpaymentsbook.data.model.enums.AnalyticsType
import com.raghav.digitalpaymentsbook.databinding.FragmentSalesStatBinding
import com.raghav.digitalpaymentsbook.viewmodel.AnalyticsViewModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class SalesStatFragment(private val isSentTypeFrag: Boolean) : Fragment() {


    var _binding: FragmentSalesStatBinding? = null
    val binding: FragmentSalesStatBinding
        get() = _binding!!


    lateinit var viewModel: AnalyticsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesStatBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("TAG", "VC ${isSentTypeFrag}")

        viewModel = ViewModelProvider(requireActivity())[AnalyticsViewModel::class.java]

        viewModel.salesIsBarChartType.observe(viewLifecycleOwner) {
            Log.d("TAG", "SF BC ${it}")

            binding.barChartSales.isVisible = it
            binding.pieChartSales.isVisible = !it
        }

        barChartCustomisation()
        pieChartCustomisation()

        if (isSentTypeFrag) {
            viewModel.salesByUserData.observe(viewLifecycleOwner) {
                Log.d("TAG", "SF SUD ${it.size}")
                when (viewModel.type) {
                    AnalyticsType.week -> {
                        setSalesWeeklyBarGraph(it)
                        setSalesWeeklyPieChart(it)
                    }
                    AnalyticsType.month -> {
                        setSalesMonthlyBarGraph(it)
                        setSalesMonthlyPieChart(it)
                    }
                    AnalyticsType.year -> {
                        setSalesYearlyBarGraph(it)
                        setSalesYearlyPieChart(it)
                    }
                }
            }
        } else {
            viewModel.salesByOtherData.observe(viewLifecycleOwner) {
                Log.d("TAG", "SF SOD ${viewModel.salesByOtherData.value?.size}")
                when (viewModel.type) {
                    AnalyticsType.week -> {
                        setSalesWeeklyBarGraph(it)
                        setSalesWeeklyPieChart(it)
                    }
                    AnalyticsType.month -> {
                        setSalesMonthlyBarGraph(it)
                        setSalesMonthlyPieChart(it)
                    }
                    AnalyticsType.year -> {
                        setSalesYearlyBarGraph(it)
                        setSalesYearlyPieChart(it)
                    }
                }
            }
        }

    }

    private fun setSalesWeeklyBarGraph(data: List<AnalyticsData>) {

        binding.barChartSales.invalidate()
//        binding.xlabel.text = "Week days"

        val entries: MutableList<BarEntry> = mutableListOf()
        val weekdaysEntries: MutableList<String> = mutableListOf()


        data.forEachIndexed { i, it ->
            entries.add(BarEntry(i.toFloat(), it.count.toFloat()))
            weekdaysEntries.add(getWeekName(it.key))
        }

        val dataSets: MutableList<IBarDataSet?> = mutableListOf()
        val set1 = BarDataSet(entries, "Income")
        set1.valueTextSize = 10f
        set1.setColors(*ColorTemplate.PASTEL_COLORS)
            set1.valueTextColor = Color.WHITE
        dataSets.add(set1)

        //String setter in x-Axis
        binding.barChartSales.xAxis.valueFormatter = IndexAxisValueFormatter(weekdaysEntries)

        val data = BarData(dataSets)
        data.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "₹${value.toInt()}"
            }
        })
        binding.barChartSales.data = data


    }

    private fun setSalesWeeklyPieChart(data: List<AnalyticsData>) {

        binding.pieChartSales.invalidate()
//        binding.xlabel.text = "Week days"

        val entries: MutableList<PieEntry> = mutableListOf()


        // loop adding one day in each iteration
        data.forEachIndexed { i, it ->
            if (it.count != 0) {
                entries.add(PieEntry(it.count.toFloat(), getWeekName(it.key)))
            }
        }

        val set = PieDataSet(entries, "Election Results")
        set.valueTextSize = 10f
        set.setColors(*ColorTemplate.PASTEL_COLORS)
        set.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "₹${value.toInt()}"
            }
        }
        val data = PieData(set)
        binding.pieChartSales.data = data
    }


    private fun setSalesMonthlyBarGraph(data: List<AnalyticsData>) {

        Log.d("TAG", "setSalesMonthlyBarGraph ${data}")


        binding.barChartSales.invalidate()
//        binding.xlabel.text = "Week days"

        val entries: MutableList<BarEntry> = mutableListOf()
        val monthsEntries: MutableList<String> = mutableListOf()




        data.forEachIndexed { i, it ->
            entries.add(BarEntry(i.toFloat(), it.count.toFloat()))
            monthsEntries.add(getMonthName(it.key))
        }

        val dataSets: MutableList<IBarDataSet?> = mutableListOf()
        val set1 = BarDataSet(entries, "Income")
        set1.valueTextSize = 10f
        set1.setColors(*ColorTemplate.PASTEL_COLORS)
            set1.valueTextColor = Color.WHITE
        dataSets.add(set1)

        //String setter in x-Axis
        binding.barChartSales.xAxis.valueFormatter = IndexAxisValueFormatter(monthsEntries)
        val data = BarData(dataSets)
        data.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "₹${value.toInt()}"
            }
        })
        binding.barChartSales.data = data


    }

    private fun setSalesMonthlyPieChart(data: List<AnalyticsData>) {

        binding.pieChartSales.invalidate()
//        binding.xlabel.text = "Week days"

        val entries: MutableList<PieEntry> = mutableListOf()


        // loop adding one day in each iteration
        data.forEachIndexed { i, it ->
            if (it.count != 0) {
                entries.add(PieEntry(it.count.toFloat(), getMonthName(it.key), i))
            }
        }

        val set = PieDataSet(entries, "Election Results")
        set.valueTextSize = 10f
        set.setColors(*ColorTemplate.PASTEL_COLORS)
        set.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "₹${value.toInt()}"
            }
        }
        val data = PieData(set)
        binding.pieChartSales.data = data


    }


    private fun setSalesYearlyBarGraph(data: List<AnalyticsData>) {

        binding.barChartSales.invalidate()
//        binding.xlabel.text = "Week days"

        //index is weekday, list contains data to fetch the words of that day
        val listOfWords: MutableMap<Int, MutableList<String>> = mutableMapOf()
        val entries: MutableList<BarEntry> = mutableListOf()
        val yearEntries: MutableList<String> = mutableListOf()

        data.forEachIndexed { i, it ->
            entries.add(BarEntry(i.toFloat(), it.count.toFloat()))
            yearEntries.add(it.key.toString())
        }

        val dataSets: MutableList<IBarDataSet?> = mutableListOf()
        val set1 = BarDataSet(entries, "Income")
        set1.valueTextSize = 10f
        set1.setColors(*ColorTemplate.PASTEL_COLORS)
            set1.valueTextColor = Color.WHITE
        dataSets.add(set1)

        //String setter in x-Axis
        binding.barChartSales.xAxis.valueFormatter = IndexAxisValueFormatter(yearEntries)

        val data = BarData(dataSets)
        data.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "₹${value.toInt()}"
            }
        })
        binding.barChartSales.data = data

    }


    private fun setSalesYearlyPieChart(data: List<AnalyticsData>) {

        binding.pieChartSales.invalidate()
//        binding.xlabel.text = "Week days"

        val entries: MutableList<PieEntry> = mutableListOf()

        // loop adding one day in each iteration
        data.forEachIndexed { i, it ->
            if (it.count != 0) {
                entries.add(PieEntry(it.count.toFloat(), it.key.toString(), i))
            }
        }

        val set = PieDataSet(entries, "Election Results")
        set.valueTextSize = 10f
        set.setColors(*ColorTemplate.PASTEL_COLORS)
        set.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "₹${value.toInt()}"
            }
        }
        val data = PieData(set)
        binding.pieChartSales.data = data

    }

    private fun getWeekName(i: Int): String {
        return when (i) {
            1 -> {
                "Sun"
            }
            2 -> {
                "Mon"
            }
            3 -> {
                "Tue"
            }
            4 -> {
                "Wed"
            }
            5 -> {
                "Thru"
            }
            6 -> {
                "Fri"
            }
            else -> {
                "Sat"
            }
        }
    }
    private fun getMonthName(i: Int): String {
        return when (i) {
            1 -> {
                "Jan"
            }
            2 -> {
                "Feb"
            }
            3 -> {
                "Mar"
            }
            4 -> {
                "Apr"
            }
            5 -> {
                "May"
            }
            6 -> {
                "Jun"
            }
            7 -> {
                "Jul"
            }
            8 -> {
                "Aug"
            }
            9 -> {
                "Sept"
            }
            10 -> {
                "Oct"
            }
            11 -> {
                "Nov"
            }
            else -> {
                "Dec"
            }
        }
    }

    private fun barChartCustomisation(){
        //customization
        binding.barChartSales.setTouchEnabled(true)
        //                binding.barChartSales.setDragEnabled(true);
        binding.barChartSales.setScaleEnabled(false)
        binding.barChartSales.setPinchZoom(false)
        binding.barChartSales.setDrawGridBackground(false)

////to hide background lines
        binding.barChartSales.xAxis.setDrawGridLines(false)
        binding.barChartSales.axisLeft.setDrawGridLines(false)
        binding.barChartSales.axisRight.setDrawGridLines(false)

//to hide right Y and top X border
        val rightYAxis: YAxis = binding.barChartSales.axisRight
        rightYAxis.isEnabled = false
        val xAxis: XAxis = binding.barChartSales.xAxis
        xAxis.granularity = 1f
        xAxis.isEnabled = true
        xAxis.setDrawGridLines(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM


        binding.barChartSales.animateX(500)
        binding.barChartSales.animateY(1000)
        binding.barChartSales.legend.isEnabled = false
        binding.barChartSales.description.isEnabled = false

        binding.barChartSales.axisLeft.spaceBottom = 0f
        binding.barChartSales.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "₹${value.toInt()}"
            }
        }
        binding.barChartSales.xAxis.textColor = Color.WHITE
        binding.barChartSales.axisLeft.textColor = Color.WHITE

    }
    private fun pieChartCustomisation(){
//customization
        binding.pieChartSales.setTouchEnabled(false)
        binding.pieChartSales.animateX(500)
        binding.pieChartSales.animateY(1000)
        binding.pieChartSales.legend.isEnabled = false
        binding.pieChartSales.description.isEnabled = false

        binding.pieChartSales.setEntryLabelColor(Color.WHITE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}