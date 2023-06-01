package com.raghav.digitalpaymentsbook.ui.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.raghav.digitalpaymentsbook.data.model.AnalyticsData
import com.raghav.digitalpaymentsbook.data.model.enums.AnalyticsType
import com.raghav.digitalpaymentsbook.databinding.FragmentOrderStatBinding
import com.raghav.digitalpaymentsbook.viewmodel.AnalyticsViewModel
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderStatFragment(private val isSentTypeFrag : Boolean) : Fragment() {


    var _binding: FragmentOrderStatBinding? = null
    val binding: FragmentOrderStatBinding
        get() = _binding!!


    lateinit var viewModel : AnalyticsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderStatBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel =  ViewModelProvider(requireActivity())[AnalyticsViewModel::class.java]

        viewModel.ordersIsBarChartType.observe(viewLifecycleOwner){
            binding.barChartOrder.isVisible = it
            binding.pieChartOrder.isVisible = !it
        }

        barChartCustomisation()
        pieChartCustomisation()

        if(isSentTypeFrag){
            viewModel.ordersByUserData.observe(viewLifecycleOwner){
                when(viewModel.type){
                    AnalyticsType.week -> {
                        setOrderWeeklyBarGraph(it)
                        setOrderWeeklyPieChart(it)
                    }
                    AnalyticsType.month -> {
                        setOrderMonthlyBarGraph(it)
                        setOrderMonthlyPieChart(it)
                    }
                    AnalyticsType.year -> {
                        setOrderYearlyBarGraph(it)
                        setOrderYearlyPieChart(it)
                    }
                }
            }
        }else{
            viewModel.ordersByOtherData.observe(viewLifecycleOwner){
                when(viewModel.type){
                    AnalyticsType.week -> {
                        setOrderWeeklyBarGraph(it)
                        setOrderWeeklyPieChart(it)
                    }
                    AnalyticsType.month -> {
                        setOrderMonthlyBarGraph(it)
                        setOrderMonthlyPieChart(it)
                    }
                    AnalyticsType.year -> {
                        setOrderYearlyBarGraph(it)
                        setOrderYearlyPieChart(it)
                    }
                }
            }
        }


    }

    private fun setOrderWeeklyBarGraph(data : List<AnalyticsData>) {

        binding.barChartOrder.invalidate()
//        binding.xlabel.text = "Week days"

        val entries: MutableList<BarEntry> = mutableListOf()
        val weekdaysEntries: MutableList<String> = mutableListOf()

        lifecycleScope.launch {

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
            binding.barChartOrder.xAxis.valueFormatter = IndexAxisValueFormatter(weekdaysEntries)

            val data = BarData(dataSets)
            data.setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            })
            binding.barChartOrder.data = data

        }
    }

    private fun setOrderWeeklyPieChart(data : List<AnalyticsData>) {

        binding.pieChartOrder.invalidate()
//        binding.xlabel.text = "Week days"

        val entries: MutableList<PieEntry> = mutableListOf()
        lifecycleScope.launch {


            // loop adding one day in each iteration
            data.forEachIndexed { i, it ->
                if (it.count != 0) {
                    entries.add(PieEntry(it.count.toFloat(), getWeekName(it.key), i))
                }
            }

            val set = PieDataSet(entries, "Election Results")
            set.valueTextSize = 10f
            set.setColors(*ColorTemplate.PASTEL_COLORS)
            set.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
            val data = PieData(set)
            binding.pieChartOrder.data = data

        }
    }

    private fun setOrderMonthlyBarGraph(data : List<AnalyticsData>) {

        binding.barChartOrder.invalidate()
//        binding.xlabel.text = "Week days"

        val entries: MutableList<BarEntry> = mutableListOf()
        val monthsEntries: MutableList<String> = mutableListOf()

        lifecycleScope.launch {

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
            binding.barChartOrder.xAxis.valueFormatter = IndexAxisValueFormatter(monthsEntries)

            val data = BarData(dataSets)
            data.setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            })
            binding.barChartOrder.data = data

        }
    }

    private fun setOrderMonthlyPieChart(data : List<AnalyticsData>) {

        binding.pieChartOrder.invalidate()
//        binding.xlabel.text = "Week days"

        val entries: MutableList<PieEntry> = mutableListOf()
        //index is weekday, list contains data to fetch the words of that day
        val listOfWords: MutableMap<Int, MutableList<String>> = mutableMapOf()
        lifecycleScope.launch {

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
                    return value.toInt().toString()
                }
            }
            val data = PieData(set)
            binding.pieChartOrder.data = data

        }
    }

    private fun setOrderYearlyBarGraph(data : List<AnalyticsData>) {

        binding.barChartOrder.invalidate()
//        binding.xlabel.text = "Week days"
        val entries: MutableList<BarEntry> = mutableListOf()
        val yearEntries: MutableList<String> = mutableListOf()

        lifecycleScope.launch {

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
            binding.barChartOrder.xAxis.valueFormatter = IndexAxisValueFormatter(yearEntries)

            val data = BarData(dataSets)
            data.setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            })
            binding.barChartOrder.data = data
        }
    }

    private fun setOrderYearlyPieChart(data : List<AnalyticsData>) {

        binding.pieChartOrder.invalidate()
//        binding.xlabel.text = "Week days"

        val entries: MutableList<PieEntry> = mutableListOf()
        lifecycleScope.launch {

            val sdf3: DateFormat = SimpleDateFormat("yyyy")
            val cal: Calendar = Calendar.getInstance()
            // get starting date
            cal.add(Calendar.YEAR, -3)

            // loop adding one day in each iteration
            data.forEachIndexed { i, it ->
                cal.add(Calendar.YEAR, 1)
                if (it.count != 0) {
                    entries.add(PieEntry(it.count.toFloat(), it.key.toString(), i))
                }
            }

            val set = PieDataSet(entries, "Election Results")
            set.valueTextSize = 10f
            set.setColors(*ColorTemplate.PASTEL_COLORS)
            set.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
            val data = PieData(set)
            binding.pieChartOrder.data = data
        }
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
        binding.barChartOrder.setTouchEnabled(true)
        //                binding.barChartOrder.setDragEnabled(true);
        binding.barChartOrder.setScaleEnabled(false)
        binding.barChartOrder.setPinchZoom(false)
        binding.barChartOrder.setDrawGridBackground(false)

////to hide background lines
        binding.barChartOrder.xAxis.setDrawGridLines(false)
        binding.barChartOrder.axisLeft.setDrawGridLines(false)
        binding.barChartOrder.axisRight.setDrawGridLines(false)

//to hide right Y and top X border
        val rightYAxis: YAxis = binding.barChartOrder.axisRight
        rightYAxis.isEnabled = false
        val xAxis: XAxis = binding.barChartOrder.xAxis
        xAxis.granularity = 1f
        xAxis.isEnabled = true
        xAxis.setDrawGridLines(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM


        binding.barChartOrder.animateX(500)
        binding.barChartOrder.animateY(1000)
        binding.barChartOrder.legend.isEnabled = false
        binding.barChartOrder.description.isEnabled = false

        binding.barChartOrder.axisLeft.spaceBottom = 0f
        binding.barChartOrder.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }
        binding.barChartOrder.xAxis.textColor = Color.WHITE
        binding.barChartOrder.axisLeft.textColor = Color.WHITE

    }

    private fun pieChartCustomisation(){
//customization
        binding.pieChartOrder.setTouchEnabled(false)
        binding.pieChartOrder.animateX(500)
        binding.pieChartOrder.animateY(1000)
        binding.pieChartOrder.legend.isEnabled = false
        binding.pieChartOrder.description.isEnabled = false

        binding.pieChartOrder.setEntryLabelColor(Color.WHITE)
    }

}