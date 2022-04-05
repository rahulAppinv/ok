package com.app.okra.ui.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.okra.R
import com.app.okra.base.BaseFragmentWithoutNav
import com.app.okra.base.BaseViewModel
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.data.repo.HomeRepoImpl
import com.app.okra.extension.navigationOnly
import com.app.okra.extension.viewModelFactory
import com.app.okra.models.GraphInfo
import com.app.okra.models.MealData
import com.app.okra.models.UserInfo
import com.app.okra.ui.DashBoardActivity
import com.app.okra.ui.add_meal.AddMealActivity
import com.app.okra.ui.add_medication.MedicationDetailsFragment
import com.app.okra.ui.connected_devices.ConnectionStatusFragment
import com.app.okra.ui.logbook.meal.MealLogsAdapter
import com.app.okra.ui.notification.NotificationActivity
import com.app.okra.utils.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*

class HomeFragment : BaseFragmentWithoutNav(), Listeners.ItemClickListener {

    private var avgGlucose: Double? = null
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var mealLogsAdapter: MealLogsAdapter
    private var hashMapKeyList = ArrayList<String>()

    private var hashMapMealLog = LinkedHashMap<String, ArrayList<MealData>>()
  
    private var time:String = AppConstants.TODAY

    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    private val viewModel by lazy {
        ViewModelProvider(this,
            viewModelFactory {
                HomeViewModel(HomeRepoImpl(apiServiceAuth))
            }
        ).get(HomeViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
        getData()
        setView()
        setListener()
    }

    private fun setView() {
        tv_name.text = PreferenceManager.getString(AppConstants.Pref_Key.NAME)
        val textToSet = "${getString(R.string.time)} (hr.)"
        tv_time.text = textToSet
    }

    override fun onResume() {
        super.onResume()
        updateAvgGlucoseValue(avgGlucose)
    }


    private fun updateAvgGlucoseValue(averageGlucose: Double?) {
        if(averageGlucose!=null){

            val bloodGlucoseUnit = PreferenceManager.getString(AppConstants.Pref_Key.BLOOD_GLUCOSE_UNIT)

            var valueToSet  = ""
            var valueInDouble  = 0.0
            if(!bloodGlucoseUnit.isNullOrEmpty()) {
                if (bloodGlucoseUnit == AppConstants.MM_OL) {
                    valueInDouble =  convertMGDLtoMMOL(averageGlucose.toFloat()).toDouble()
                    valueToSet = String.format("%.2f", valueInDouble)

                }else{
                    valueToSet = String.format("%.2f", averageGlucose.toBigDecimal())
                }
            }else{
                valueToSet = String.format("%.2f", averageGlucose.toBigDecimal())
            }

            tvAvgBgValue.text =valueToSet
        }

    }

    private fun getData() {
        viewModel.dashboardInfo(time)
        viewModel.stripeInfo()
    }



    private fun setObserver() {
        setBaseObservers(viewModel, this, observeError = false)
        viewModel._stripeInfoLiveData.observe(viewLifecycleOwner) { it ->
            it.data?.let {
                tvTestLogged.text = it.totalTestLog
                tvMealLogged.text = it.totalMealLog
            }
        }

        viewModel._dashboardLiveData.observe(viewLifecycleOwner) { it ->
            swipe_home.isRefreshing = false
            it.data?.let {
                tvTotalTestValue.text = it.totalTest

                updateUserData(it.userInfo)
                avgGlucose =it.avgBloodGlucose
                updateAvgGlucoseValue(it.avgBloodGlucose)

                if(it.avgInsulin!=null) {
                    tvInsulinValue.text = String.format("%.2f", it.avgInsulin!!.toBigDecimal())
                }
                val hyperHypoText = "${it.hyper_hypes?.hyper} / ${it.hyper_hypes?.hypos}"
                tvHyperValue.text = hyperHypoText

                if(it.Est_HbA1c!=null) {
                    tvHbaValue.text = String.format("%.2f",it.Est_HbA1c!!.toBigDecimal())
                }

                if(it.carbsCount!=null) {
                    val valueToSet = String.format("%.2f", it.carbsCount!!.toBigDecimal())
                    tvCarbsValue.text =  valueToSet
                }
                if (it.foodLogs?.size!! > 0) {
                    tv_food_log.visibility = View.VISIBLE
                    rv_meal_list.visibility = View.VISIBLE
                    it.foodLogs?.let { it1 -> prepareDateWiseData(it1) }
                    setAdapter()
                } else {
                    tv_food_log.visibility = View.GONE
                    rv_meal_list.visibility = View.GONE
                }
                if(it.graphInfo?.size!! > 0) {
                    chart.visibility = View.VISIBLE
                    tv_no_chart.visibility = View.GONE
                    setCharts(it.graphInfo!!)
                }else {
                    chart.visibility = View.INVISIBLE
                    tv_no_chart.visibility = View.VISIBLE
                }
            }
        }

        viewModel._errorObserver.observe(viewLifecycleOwner) {
            swipe_home.isRefreshing = false
            val data = it.getContent()
            data?.message?.let { it1 -> showToast(it1) }

            if (data?.message == getString(R.string.your_login_session_has_been_expired)) {
                navigateToLogin(requireActivity())
                requireActivity().finish()
            }
        }

        EventLiveData.eventLiveData.observe(viewLifecycleOwner){ event ->
            if((requireActivity() as DashBoardActivity).currentFragment() == 0) {
                event.peekContent().let {
                    if (!it.type.isNullOrEmpty() &&
                        (it.type == ConnectionStatusFragment::class.java.simpleName
                                || it.type == AddMealActivity::class.java.simpleName
                                || it.type == MedicationDetailsFragment::class.java.simpleName)) {
                        event.update()
                        getData()
                    }
                }
            }
        }
    }

    private fun updateUserData(userInfo: UserInfo?) {
        userInfo?.let{
            it.isApproved?.let {
                PreferenceManager.putBoolean(AppConstants.Pref_Key.IS_APPROVED, it)
            }
            it.isVerify?.let {
                PreferenceManager.putBoolean(AppConstants.Pref_Key.IS_VERIFIED, it)
            }
        }
    }

    private fun setAdapter() {
        mealLogsAdapter = MealLogsAdapter(this, hashMapKeyList, hashMapMealLog)
        layoutManager = LinearLayoutManager(requireContext())
        rv_meal_list.layoutManager = layoutManager
        rv_meal_list.adapter = mealLogsAdapter
    }

    private fun prepareDateWiseData(testLogData: ArrayList<MealData>) {
        hashMapMealLog.clear()
        hashMapKeyList.clear()
        val hashMap = LinkedHashMap<String, ArrayList<MealData>>()
        if (testLogData.isNotEmpty()) {
            for ((index, data) in testLogData.withIndex()) {
                val date = data.date
                date?.let {
                    val dateToSet = getDateFromISOInString(it, formatYouWant = "dd/MM/yyyy")

                    val list: java.util.ArrayList<MealData> = if (hashMap.containsKey(dateToSet)) {
                        hashMap[dateToSet] as ArrayList<MealData>
                    } else {
                        ArrayList()
                    }
                    list.add(data)
                    hashMap[dateToSet] = list
                }
            }
        }
        hashMapMealLog.putAll(hashMap)
        hashMapKeyList.addAll(hashMap.keys.toList())
    }

    private fun setListener() {
        ivNotification.setOnClickListener {
            requireActivity().navigationOnly(NotificationActivity())
        }
        ivTestRefresh.setOnClickListener {
            viewModel.stripeInfo()
        }
        tvTestLoggedText.setOnClickListener {
            (activity as DashBoardActivity).loadFragment(1, AppConstants.TEST_LOG)
        }
        tvTestLogged.setOnClickListener {
            (activity as DashBoardActivity).loadFragment(1,  AppConstants.TEST_LOG)
        }
        tvMealLogged.setOnClickListener {
            (activity as DashBoardActivity).loadFragment(1,  AppConstants.MEAL_LOG)
        }
        tvMealLoggedText.setOnClickListener {
            (activity as DashBoardActivity).loadFragment(1, AppConstants.MEAL_LOG)
        }

        ivMealRefresh.setOnClickListener {
            viewModel.stripeInfo()
        }

        rl_today.setOnClickListener {
            handleTabsBackground(0)
            time= AppConstants.TODAY
            viewModel.dashboardInfo(time)
            val timeToSet = "${getString(R.string.time)} (hr.)"
            tv_time.text = timeToSet
        }

        rl_this_week.setOnClickListener {
            handleTabsBackground(1)
            time= AppConstants.WEEK
            viewModel.dashboardInfo(time)
            val timeToSet = "${getString(R.string.time)} (Weekday)"
            tv_time.text = timeToSet
        }

        rl_this_month.setOnClickListener {
            handleTabsBackground(2)
            time= AppConstants.MONTH
            viewModel.dashboardInfo(time)
            val timeToSet = "${getString(R.string.time)} (Day of month)"
            tv_time.text = timeToSet
        }

        swipe_home.setOnRefreshListener {
            getData()
        }
    }

    private fun handleTabsBackground(value: Int) {
        val textGreenColor =
            activity?.let { it1 -> ContextCompat.getColor(it1, R.color.green_1) } ?: 0
        val textGreyColor =
            activity?.let { it1 -> ContextCompat.getColor(it1, R.color.grey_3) } ?: 0
        val textWhiteColor =
            activity?.let { it1 -> ContextCompat.getColor(it1, R.color.bg_grey) } ?: 0

        when (value) {
            0 -> {
                tv_today.setTextColor(textGreenColor)
                iv_today.backgroundTintList =
                    ColorStateList.valueOf(textGreenColor)
                tv_this_week.setTextColor(textGreyColor)
                iv_this_week.backgroundTintList =
                    ColorStateList.valueOf(textWhiteColor)
                tv_this_month.setTextColor(textGreyColor)
                iv_this_month.backgroundTintList =
                    ColorStateList.valueOf(textWhiteColor)
            }
            1 -> {
                tv_this_week.setTextColor(textGreenColor)
                iv_this_week.backgroundTintList =
                    ColorStateList.valueOf(textGreenColor)
                tv_today.setTextColor(textGreyColor)
                iv_today.backgroundTintList =
                    ColorStateList.valueOf(textWhiteColor)
                tv_this_month.setTextColor(textGreyColor)
                iv_this_month.backgroundTintList =
                    ColorStateList.valueOf(textWhiteColor)
            }
            else -> {
                tv_this_month.setTextColor(textGreenColor)
                iv_this_month.backgroundTintList =
                    ColorStateList.valueOf(textGreenColor)
                tv_this_week.setTextColor(textGreyColor)
                iv_this_week.backgroundTintList =
                    ColorStateList.valueOf(textWhiteColor)
                tv_today.setTextColor(textGreyColor)
                iv_today.backgroundTintList =
                    ColorStateList.valueOf(textWhiteColor)
            }
        }
    }

    override fun onSelect(o: Any?, o1: Any?) {

    }

    override fun onUnSelect(o: Any?, o1: Any?) {

    }

    private fun setCharts(graphInfo: ArrayList<GraphInfo>) {
        chart.clear()
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setDrawGridBackground(false)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)

        val xAxis: XAxis = chart.xAxis
        val array = arrayOfNulls<String>(graphInfo.size)
        when (time) {
            AppConstants.TODAY -> {
                for(i in 0 until graphInfo.size) {
                   // array[i] = graphInfo[i].hours + "hr"
                    array[i] = graphInfo[i].hours
                }
            }
            AppConstants.WEEK -> {
                for(i in 0 until graphInfo.size) {
                    var dayOfWeek = ""
                    val firstLetter = graphInfo[i].day?.substring(0,1)
                    val secondLetter = graphInfo[i].day?.substring(1,2)?.toLowerCase(Locale.ROOT)

                    dayOfWeek = firstLetter+secondLetter
                    array[i] = dayOfWeek
                }
            }
            AppConstants.MONTH -> {
                for(i in 0 until graphInfo.size)
                    array[i] = "#"+graphInfo[i]._id.toString()
            }
        }

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

    private fun setData(count: Int, list: ArrayList<GraphInfo>) {
        val values: ArrayList<Entry> = ArrayList()
        for (i in 0 until count) {
            list[i].bloodGlucose?.toFloat()?.let {
                println("::: This Value: ${i.toFloat()}")
                Entry(i.toFloat(), it)
            }?.let {
                values.add(it)
            }
        }
        val set1: LineDataSet
        if (chart.data != null && chart.data.dataSetCount > 0) {
            println("::: data set count: ${chart.data.dataSetCount}")
            set1 = chart.data.getDataSetByIndex(0) as LineDataSet
            set1.values = values
            set1.notifyDataSetChanged()
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
         //   println("::: New LineData Value: ${values.size}")

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