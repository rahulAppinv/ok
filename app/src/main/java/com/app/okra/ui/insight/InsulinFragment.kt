package com.app.okra.ui.insight

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.app.okra.R
import com.app.okra.base.BaseFragmentWithoutNav
import com.app.okra.base.BaseViewModel
import com.app.okra.data.repo.BloodGlucoseRepoImpl
import com.app.okra.extension.viewModelFactory
import com.app.okra.ui.my_account.setting.measurement.CustomSpinnerAdapter
import com.app.okra.utils.AppConstants
import com.app.okra.utils.getMealTime
import com.app.okra.utils.navigateToLogin
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.android.synthetic.main.fragment_insulin.chart
import kotlinx.android.synthetic.main.fragment_insulin.iv_this_month
import kotlinx.android.synthetic.main.fragment_insulin.iv_this_week
import kotlinx.android.synthetic.main.fragment_insulin.iv_today
import kotlinx.android.synthetic.main.fragment_insulin.rl_this_month
import kotlinx.android.synthetic.main.fragment_insulin.rl_this_week
import kotlinx.android.synthetic.main.fragment_insulin.rl_today
import kotlinx.android.synthetic.main.fragment_insulin.tv_no_chart
import kotlinx.android.synthetic.main.fragment_insulin.tv_this_month
import kotlinx.android.synthetic.main.fragment_insulin.tv_this_week
import kotlinx.android.synthetic.main.fragment_insulin.tv_today

class InsulinFragment : BaseFragmentWithoutNav() {

    private val type : String = AppConstants.INSULIN
    private var time : String = AppConstants.TODAY

    private val viewModel by lazy {
        ViewModelProvider(this,
            viewModelFactory {
                BloodGlucoseViewModel(BloodGlucoseRepoImpl(apiServiceAuth))
            }
        ).get(BloodGlucoseViewModel::class.java)
    }

    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_insulin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData()
        setObserver()
        setListener()
    }

    private fun getData() {
        viewModel.prepareRequest(type,AppConstants.BEFORE_MEAL,time)
        viewModel.getInsight()
    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, observeError = false)
        viewModel._insightLiveData.observe(viewLifecycleOwner) { it ->
            it.data?.let {
                if (it.insightData?.size!! > 0) {
                    chart.visibility = View.VISIBLE
                    tv_no_chart.visibility = View.GONE
                    setCharts(it.insightData!!)
                } else {
                    chart.visibility = View.INVISIBLE
                    tv_no_chart.visibility = View.VISIBLE
                }
            }
        }

        viewModel._errorObserver.observe(viewLifecycleOwner){
            val data = it.getContent()
            data?.message?.let { it1 -> showToast(it1) }

            if (data?.message == "Your login session has been expired.") {
                navigateToLogin(requireActivity())
                requireActivity().finish()
            }

        }
    }

    private fun setListener() {
        val textGreenColor =
            activity?.let { it1 -> ContextCompat.getColor(it1, R.color.green_1) } ?: 0
        val textGreyColor =
            activity?.let { it1 -> ContextCompat.getColor(it1, R.color.grey_3) } ?: 0
        val textWhiteColor =
            activity?.let { it1 -> ContextCompat.getColor(it1, R.color.bg_grey) } ?: 0

        rl_today.setOnClickListener {
            tv_today.setTextColor(textGreenColor)
            iv_today.backgroundTintList =
                ColorStateList.valueOf(textGreenColor)
            tv_this_week.setTextColor(textGreyColor)
            iv_this_week.backgroundTintList =
                ColorStateList.valueOf(textWhiteColor)
            tv_this_month.setTextColor(textGreyColor)
            iv_this_month.backgroundTintList =
                ColorStateList.valueOf(textWhiteColor)
            time = AppConstants.TODAY
            getData()
        }
        rl_this_week.setOnClickListener {
            tv_this_week.setTextColor(textGreenColor)
            iv_this_week.backgroundTintList =
                ColorStateList.valueOf(textGreenColor)
            tv_today.setTextColor(textGreyColor)
            iv_today.backgroundTintList =
                ColorStateList.valueOf(textWhiteColor)
            tv_this_month.setTextColor(textGreyColor)
            iv_this_month.backgroundTintList =
                ColorStateList.valueOf(textWhiteColor)
            time = AppConstants.WEEK
            getData()
        }
        rl_this_month.setOnClickListener {
            tv_this_month.setTextColor(textGreenColor)
            iv_this_month.backgroundTintList =
                ColorStateList.valueOf(textGreenColor)
            tv_this_week.setTextColor(textGreyColor)
            iv_this_week.backgroundTintList =
                ColorStateList.valueOf(textWhiteColor)
            tv_today.setTextColor(textGreyColor)
            iv_today.backgroundTintList =
                ColorStateList.valueOf(textWhiteColor)
            time = AppConstants.MONTH
            getData()
        }
    }

    private fun setCharts(graphInfo: ArrayList<Float>) {
        chart.clear()
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setDrawGridBackground(false)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)

        val xAxis: XAxis = chart.xAxis
        val array = arrayOfNulls<Int>(graphInfo.size)
        for (i in 0 until graphInfo.size)
            array[i] = i+1
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                if(value<0){
                    return ""
                }
                if(value>array.size-1){
                    return ""
                }
                return array[value.toInt()].toString()
            }
        }
        xAxis.disableGridDashedLine()
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1.0f
        xAxis.isGranularityEnabled = true
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        val yAxis: YAxis = chart.axisLeft
        // disable dual axis (only use LEFT axis)
        chart.axisRight.isEnabled = false
        yAxis.disableGridDashedLine()
        yAxis.setDrawAxisLine(false)

        if(graphInfo.size>0)
            setData(graphInfo.size, graphInfo)
    }

    private fun setData(count: Int, list: ArrayList<Float>) {
        val values: ArrayList<Entry> = ArrayList()
        for (i in 0 until count) {
            list[i].toFloat().let { Entry(i.toFloat(), it) }.let { values.add(it) }
        }
        val set1: LineDataSet
        if (chart.data != null && chart.data.dataSetCount > 0) {
            set1 = chart.data.getDataSetByIndex(0) as LineDataSet
            set1.values = values
            set1.notifyDataSetChanged()
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            set1 = LineDataSet(values, "")
            set1.setDrawIcons(false)
            set1.cubicIntensity = 0.2f
            set1.mode = LineDataSet.Mode.CUBIC_BEZIER

            // draw dashed line
            set1.disableDashedLine()

            // black lines and points
            set1.color = ContextCompat.getColor(requireContext(),R.color.green_1)
            set1.setCircleColor(ContextCompat.getColor(requireContext(),R.color.green_1))

            // line thickness and point size
            set1.lineWidth = 3f

            // draw points as solid circles
            set1.setDrawCircleHole(false)
            if(values.size>1) {
                set1.setDrawCircles(false)
            }else{
                set1.setDrawCircles(true)
                set1.circleRadius = 5.0f
            }

            // customize legend entry
            set1.formLineWidth = 0f
            set1.formSize = 0f

            // text size of values
            set1.valueTextSize = 0f

            // set the filled area
            set1.setDrawFilled(false)

            val dataSets: ArrayList<ILineDataSet> = ArrayList()
            dataSets.add(set1) // add the data sets

            val data = LineData(dataSets)

            chart.data = data
        }
    }

}