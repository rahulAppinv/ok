package com.app.okra.ui.logbook

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.app.okra.R
import com.app.okra.base.BaseFragmentWithoutNav
import com.app.okra.base.BaseViewModel
import com.app.okra.data.repo.TestLogsRepoImpl
import com.app.okra.extension.*
import com.app.okra.models.MealLogFilter
import com.app.okra.models.MedicationLogFilter
import com.app.okra.models.TestLogFilter
import com.app.okra.ui.DashBoardActivity
import com.app.okra.ui.logbook.meal.MealLogsFragment
import com.app.okra.ui.logbook.medication.MedicationLogsFragment
import com.app.okra.ui.logbook.test.TestLogsFragment
import com.app.okra.ui.logbook.test.TestLogsViewModel
import com.app.okra.utils.*
import com.app.okra.utils.AppConstants.Companion.THIS_MONTH
import com.app.okra.utils.AppConstants.Companion.THIS_WEEK
import com.app.okra.utils.AppConstants.Companion.TODAY
import com.app.okra.utils.AppConstants.Companion.TEST_LOG
import com.app.okra.utils.AppConstants.Companion.MEAL_LOG
import com.app.okra.utils.AppConstants.DateFormat.DATE_FORMAT_3
import com.app.okra.utils.AppConstants.DateFormat.DATE_FORMAT_7
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bottomsheet_logs_filter.*
import kotlinx.android.synthetic.main.bottomsheet_logs_filter.btnReset
import kotlinx.android.synthetic.main.bottomsheet_meal_logs_filter.*
import kotlinx.android.synthetic.main.bottomsheet_medication_filter.*
import kotlinx.android.synthetic.main.fragment_logbook.*
import java.util.*

class LogbookFragment : BaseFragmentWithoutNav(), Listeners.EventClickListener {

    private var mPagerAdapter: ViewPagerBottomBar? = null
    private var mYear: Int = 0
    private var mMonth: Int = 0
    private var mDay: Int = 0
    private var displayAll = false
    private var beforeMeal = false
    private var afterMeal = false
    private var postMedicine = false
    private var postWorkout = false
    private var controlSolution = false

    private var today = false
    private var thisWeek = false
    private var thisMonth = false

    private var all = false
    private var pills = false
    private var mg = false
    private var ml = false

    private var startDateForTest :Long=0L
    private var startDateForMeal :Long=0L
    private var startDateForMed :Long=0L

    private var testFilterData: TestLogFilter?=null
    private var mealFilterData: MealLogFilter?=null
    private var medFilterData: MedicationLogFilter?=null

    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    private val viewModel by lazy {
        ViewModelProvider(this,
                viewModelFactory {
                    TestLogsViewModel(TestLogsRepoImpl(apiServiceAuth))
                }
        ).get(TestLogsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_logbook, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        initListeners()
        manageFilterDotVisibility()
        subscribeUI()
    }

    private fun subscribeUI() {
        EventLiveData.eventLiveData.observe(viewLifecycleOwner){
            val data :EventLiveData.EventData? = it.getContent()

            data?.apply{
                if(type == DashBoardActivity.TAG) {
                    manageView(this.data.toString())
                }
            }
        }
    }


    private fun manageView(type: String?) {
        when(type){
            TEST_LOG -> viewPager.currentItem = 0
            MEAL_LOG -> viewPager.currentItem = 1
            else ->  viewPager.currentItem = 2
        }
    }

    private fun setupViewPager() {
        mPagerAdapter = activity?.supportFragmentManager?.let { ViewPagerBottomBar(it) }
        mPagerAdapter?.addFragment(TestLogsFragment(this))
        mPagerAdapter?.addFragment(MealLogsFragment(this))
        mPagerAdapter?.addFragment(MedicationLogsFragment(this))
        viewPager.adapter = mPagerAdapter
        viewPager.offscreenPageLimit = 1
        viewPager.beginFakeDrag()

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
            ) {}

            override fun onPageSelected(position: Int) {
                handleTabsBackground(position)
            }
        })
    }

    private fun initListeners() {
        rl_test_logs.setOnClickListener {
            handleTabsBackground(0)
            viewPager.currentItem = 0
            manageFilterDotVisibility() // on test click
        }
        rl_meal_logs.setOnClickListener {
            handleTabsBackground(1)
            viewPager.currentItem = 1
            manageFilterDotVisibility() // on meal click
        }
        rl_medication.setOnClickListener {
            handleTabsBackground(2)
            viewPager.currentItem = 2
            manageFilterDotVisibility() // on med click

        }
        ivFilter.setOnClickListener {
            when (viewPager.currentItem) {
                0 -> showTestBottomSheetDialog()
                1 -> showMealBottomSheetDialog()
                else -> showMedicationBottomSheetDialog()
            }
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
                tv_test_logs.setTextColor(textGreenColor)
                iv_test_logs.backgroundTintList =
                        ColorStateList.valueOf(textGreenColor)
                tv_meal_logs.setTextColor(textGreyColor)
                iv_meal_logs.backgroundTintList =
                        ColorStateList.valueOf(textWhiteColor)
                tv_medication.setTextColor(textGreyColor)
                iv_medication.backgroundTintList =
                        ColorStateList.valueOf(textWhiteColor)

            }
            1 -> {
                tv_meal_logs.setTextColor(textGreenColor)
                iv_meal_logs.backgroundTintList =
                        ColorStateList.valueOf(textGreenColor)
                tv_test_logs.setTextColor(textGreyColor)
                iv_test_logs.backgroundTintList =
                        ColorStateList.valueOf(textWhiteColor)
                tv_medication.setTextColor(textGreyColor)
                iv_medication.backgroundTintList =
                        ColorStateList.valueOf(textWhiteColor)
            }
            2 -> {
                tv_medication.setTextColor(textGreenColor)
                iv_medication.backgroundTintList =
                        ColorStateList.valueOf(textGreenColor)
                tv_meal_logs.setTextColor(textGreyColor)
                iv_meal_logs.backgroundTintList =
                        ColorStateList.valueOf(textWhiteColor)
                tv_test_logs.setTextColor(textGreyColor)
                iv_test_logs.backgroundTintList =
                        ColorStateList.valueOf(textWhiteColor)
            }
        }
    }

    private fun setupFullHeight(bottomSheet: BottomSheetDialog) {
        val parentLayout =
                bottomSheet.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        parentLayout?.let { it ->
            val behaviour = BottomSheetBehavior.from(it)
            val layoutParams = it.layoutParams
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            it.layoutParams = layoutParams
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    // Filter - Test
    private fun showTestBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.apply {
            setContentView(R.layout.bottomsheet_logs_filter)
            setupFullHeight(bottomSheetDialog)
            show()

            resetTestLogFilter()
            setTestFilterData(this)
            manageResetButton_Test(tvFromDate,tvToDate, btnReset)

            ivDisplayAll.setOnClickListener {
                ivDisplayAll.isSelected = !ivDisplayAll.isSelected
                ivBeforeMeal.isSelected = false
                ivAfterMeal.isSelected = false
                ivPostMedicine.isSelected = false
                ivPostWorkout.isSelected = false
                ivControlSolution.isSelected = false

                displayAll= ivDisplayAll.isSelected
                beforeMeal = false
                afterMeal = false
                postMedicine = false
                postWorkout = false
                controlSolution = false

                manageResetButton_Test(tvFromDate,tvToDate, btnReset)

            }
            ivBeforeMeal.setOnClickListener {
                beforeMeal = !ivBeforeMeal.isSelected
                ivBeforeMeal.isSelected = !ivBeforeMeal.isSelected

                ivDisplayAll.isSelected = false
                displayAll = false
                manageResetButton_Test(tvFromDate,tvToDate, btnReset)

            }
            ivAfterMeal.setOnClickListener {
                afterMeal = !ivAfterMeal.isSelected
                ivAfterMeal.isSelected = !ivAfterMeal.isSelected

                ivDisplayAll.isSelected = false
                displayAll = false
                manageResetButton_Test(tvFromDate,tvToDate, btnReset)

            }
            ivPostMedicine.setOnClickListener {
                postMedicine = !ivPostMedicine.isSelected
                ivPostMedicine.isSelected = !ivPostMedicine.isSelected

                ivDisplayAll.isSelected = false
                displayAll = false
                manageResetButton_Test(tvFromDate,tvToDate, btnReset)

            }
            ivPostWorkout.setOnClickListener {
                postWorkout = !ivPostWorkout.isSelected
                ivPostWorkout.isSelected = !ivPostWorkout.isSelected

                ivDisplayAll.isSelected = false
                displayAll = false
                manageResetButton_Test(tvFromDate,tvToDate, btnReset)

            }
            ivControlSolution.setOnClickListener {
                controlSolution = !ivControlSolution.isSelected
                ivControlSolution.isSelected = !ivControlSolution.isSelected

                ivDisplayAll.isSelected = false
                displayAll = false
                manageResetButton_Test(tvFromDate,tvToDate, btnReset)

            }
            tvFromDate.setOnClickListener {
                tvToDate.text = ""
                selectDate(tvFromDate, true, dateCallback = { dateInMillis: Long, isStartDate: Boolean ->
                    if (isStartDate) {
                        startDateForTest = dateInMillis
                    }
                    manageResetButton_Test(tvFromDate,tvToDate, btnReset)
                })

            }

            tvToDate.setOnClickListener {
                selectDate(tvToDate, false,
                        minDateLimit = startDateForTest,
                        dateCallback = { dateInMillis: Long, isStartDate: Boolean ->
                            manageResetButton_Test(tvFromDate,tvToDate, btnReset)
                        })
            }

            btnApplyFilter.setOnClickListener {
                if (mPagerAdapter?.position == 0) {
                    val testLogFragment = mPagerAdapter?.getItem(0) as TestLogsFragment
                    val toDate = tvToDate.text.toString().trim()
                    val fromDate = tvFromDate.text.toString().trim()


                    if(fromDate.isEmpty() && toDate.isNotEmpty()){
                        showToast(MessageConstants.Errors.please_enter_from_date)
                        return@setOnClickListener
                    }

                    if(toDate.isEmpty() && fromDate.isNotEmpty()){
                        showToast(MessageConstants.Errors.please_enter_to_date)
                        return@setOnClickListener
                    }
                    var filterTiming =""

                    testFilterData= TestLogFilter().apply {
                        this.toDate = toDate
                        this.fromDate = fromDate
                        filterTiming = getSelectedFilterTiming(this)
                        checkFilterApplied()
                    }

                    manageFilterDotVisibility()

                    testLogFragment.getData(
                            pageNo = 1,
                            testingTime = filterTiming,
                            fromDate = fromDate,
                            toDate = toDate,
                    )
                    bottomSheetDialog.dismiss()
                }
            }
            btnReset.setOnClickListener {
                testFilterData = null
                tvFromDate.text = ""
                tvToDate.text = ""
                ivDisplayAll.isSelected = false
                ivBeforeMeal.isSelected = false
                ivAfterMeal.isSelected = false
                ivPostMedicine.isSelected = false
                ivPostWorkout.isSelected = false
                ivControlSolution.isSelected = false
                resetTestLogFilter()
                manageFilterDotVisibility()
                manageResetButton_Test(tvFromDate,tvToDate, btnReset)
            }
        }
    }

    // Filter - Test -RESET Button
    private fun manageResetButton_Test(tvFromDate: AppCompatTextView,
                                       tvToDate: AppCompatTextView,
                                       btnReset: AppCompatButton) {
        val fromDate = tvFromDate.text.toString()
        val toDate = tvToDate.text.toString()

        if(fromDate.isEmpty() && toDate.isEmpty() && !displayAll
                && !beforeMeal && !afterMeal && !postMedicine
                && !postWorkout && !controlSolution
        ){
            btnReset.beDisable()
            btnReset.setTextColor(ResourcesCompat.getColor(requireContext().resources, R.color.grey_4, null))
        }else{
            btnReset.beEnable()
            btnReset.setTextColor(ResourcesCompat.getColor(requireContext().resources, R.color.green_1, null))
        }

    }

    private fun setTestFilterData(bottomSheetDialog: BottomSheetDialog) {
        testFilterData?.let {

            bottomSheetDialog.tvFromDate.text = it.fromDate ?: ""
            bottomSheetDialog.tvToDate.text = it.toDate ?: ""

            displayAll = (it.displayAll != null && it.displayAll!!)
            beforeMeal = (it.beforeMeal != null && it.beforeMeal!!)
            afterMeal =  (it.afterMeal != null && it.afterMeal!!)
            postMedicine = (it.postMed != null && it.postMed!!)
            postWorkout = (it.postWorkout != null && it.postWorkout!!)
            controlSolution = (it.controlSolution != null && it.controlSolution!!)

            bottomSheetDialog.ivDisplayAll.isSelected = (it.displayAll != null && it.displayAll!!)
            bottomSheetDialog.ivBeforeMeal.isSelected = (it.beforeMeal != null && it.beforeMeal!!)
            bottomSheetDialog.ivAfterMeal.isSelected = (it.afterMeal != null && it.afterMeal!!)
            bottomSheetDialog.ivPostMedicine.isSelected = (it.postMed != null && it.postMed!!)
            bottomSheetDialog.ivPostWorkout.isSelected = (it.postWorkout != null && it.postWorkout!!)
            bottomSheetDialog.ivControlSolution.isSelected = (it.controlSolution != null && it.controlSolution!!)
        }
    }

    private fun manageFilterDotVisibility() {
        when (viewPager.currentItem) {
            0 -> {
                if (testFilterData != null && testFilterData!!.isFilterApplied) {
                    view_filter.beVisible()
                } else {
                    view_filter.beGone()
                }
            }
            1 -> {
                if (mealFilterData != null && mealFilterData!!.isFilterApplied) {
                    view_filter.beVisible()
                } else {
                    view_filter.beGone()
                }
            }
            else -> {
                if (medFilterData != null && medFilterData!!.isFilterApplied) {
                    view_filter.beVisible()
                } else {
                    view_filter.beGone()
                }
            }

        }
    }
    private fun resetTestLogFilter() {
        displayAll = false
        beforeMeal = false
        afterMeal = false
        postMedicine = false
        postWorkout = false
        controlSolution = false
    }


    private fun getSelectedFilterTiming(testFilterData: TestLogFilter): String {
        val sBuilder = StringBuilder()

        if (displayAll) {
            testFilterData.displayAll = true
            sBuilder.append(AppConstants.DISPLAY_ALL)
        }

        if (beforeMeal) {
            testFilterData.beforeMeal = true
            sBuilder.append(AppConstants.BEFORE_MEAL)
            sBuilder.append(",")
        }

        if (afterMeal) {
            testFilterData.afterMeal = true
            sBuilder.append(AppConstants.AFTER_MEAL)
            sBuilder.append(",")
        }
        if (postMedicine) {
            testFilterData.postMed = true

            sBuilder.append(AppConstants.POST_MEDICINE)
            sBuilder.append(",")
        }
        if (postWorkout) {
            testFilterData.postWorkout = true
            sBuilder.append(AppConstants.POST_WORKOUT)
            sBuilder.append(",")
        }
        if (controlSolution) {
            testFilterData.controlSolution = true
            sBuilder.append(AppConstants.CONTROLE_SOLUTION)
        }
        return sBuilder.toString()
    }
    private fun getPreSelectedFilterTiming(testFilterData: TestLogFilter): String {
        val sBuilder = StringBuilder()

        if (testFilterData.displayAll!=null && testFilterData.displayAll!!) {
            sBuilder.append(AppConstants.DISPLAY_ALL)
        }

        if (testFilterData.beforeMeal!=null && testFilterData.beforeMeal!!) {
            sBuilder.append(AppConstants.BEFORE_MEAL)
            sBuilder.append(",")
        }

        if (testFilterData.afterMeal!=null && testFilterData.afterMeal!!) {
            sBuilder.append(AppConstants.AFTER_MEAL)
            sBuilder.append(",")
        }
        if (testFilterData.postMed!=null && testFilterData.postMed!!) {
            sBuilder.append(AppConstants.POST_MEDICINE)
            sBuilder.append(",")
        }
        if (testFilterData.postWorkout!=null && testFilterData.postWorkout!!) {
            sBuilder.append(AppConstants.POST_WORKOUT)
            sBuilder.append(",")
        }
        if (testFilterData.controlSolution!=null && testFilterData.controlSolution!!) {
            sBuilder.append(AppConstants.CONTROLE_SOLUTION)
        }
        return sBuilder.toString()
    }


    // Filter - Meal
    private fun showMealBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.apply {
            setContentView(R.layout.bottomsheet_meal_logs_filter)
            setupFullHeight(bottomSheetDialog)
            show()

            resetMealLogFilter()
            setMealFilterData(this)
            manageResetButton_Meal(tvMealFromDate,tvMealToDate, btnReset)

            ivMealDisplayAll.setOnClickListener {
                ivMealDisplayAll.isSelected = !ivMealDisplayAll.isSelected
                displayAll = ivMealDisplayAll.isSelected
                ivToday.isSelected = false
                ivThisWeek.isSelected = false
                ivThisMonth.isSelected = false
                tvMealFromDate.text = ""
                tvMealToDate.text = ""

                today = false
                thisWeek = false
                thisMonth = false
                manageResetButton_Meal(tvMealFromDate,tvMealToDate, btnReset)

            }
            ivToday.setOnClickListener {
                ivMealDisplayAll.isSelected = false
                displayAll = false

                ivThisWeek.isSelected = false
                ivThisMonth.isSelected = false

                thisWeek = false
                thisMonth = false
                tvMealFromDate.text = ""
                tvMealToDate.text = ""

                ivToday.isSelected = !ivToday.isSelected
                today = ivToday.isSelected

                manageResetButton_Meal(tvMealFromDate,tvMealToDate, btnReset)

            }
            ivThisWeek.setOnClickListener {

                ivMealDisplayAll.isSelected = false
                displayAll = false

                ivToday.isSelected = false
                ivThisMonth.isSelected = false

                today = false
                thisMonth = false

                tvMealFromDate.text = ""
                tvMealToDate.text = ""

                ivThisWeek.isSelected = !ivThisWeek.isSelected
                thisWeek = ivThisWeek.isSelected
                manageResetButton_Meal(tvMealFromDate,tvMealToDate, btnReset)

            }
            ivThisMonth.setOnClickListener {

                ivMealDisplayAll.isSelected = false
                displayAll = false

                ivToday.isSelected = false
                ivThisWeek.isSelected = false

                today = false
                thisWeek = false

                tvMealFromDate.text = ""
                tvMealToDate.text = ""

                ivThisMonth.isSelected = !ivThisMonth.isSelected
                thisMonth = ivThisMonth.isSelected
                manageResetButton_Meal(tvMealFromDate,tvMealToDate, btnReset)

            }
            tvMealFromDate.setOnClickListener {
                selectDate(tvMealFromDate, true,
                        dateCallback =  { dateInMillis: Long, isStartDate: Boolean ->
                            if (isStartDate) {
                                startDateForMeal = dateInMillis
                            }
                            ivMealDisplayAll.isSelected = false
                            ivToday.isSelected = false
                            ivThisWeek.isSelected = false
                            ivThisMonth.isSelected = false
                            tvMealToDate.text = ""
                            resetMealLogFilter()
                            manageResetButton_Meal(tvMealFromDate,tvMealToDate, btnReset)
                        })
            }
            tvMealToDate.setOnClickListener {
                selectDate(tvMealToDate, false,
                        minDateLimit = startDateForMeal,
                        dateCallback =  { _: Long, _: Boolean ->
                            ivMealDisplayAll.isSelected = false
                            ivToday.isSelected = false
                            ivThisWeek.isSelected = false
                            ivThisMonth.isSelected = false
                            resetMealLogFilter()
                            manageResetButton_Meal(tvMealFromDate,tvMealToDate, btnReset)
                        }
                )
            }
            btnMealApplyFilter.setOnClickListener {

                val mealLogFragment = mPagerAdapter?.getItem(1) as MealLogsFragment
                val toDate = tvMealToDate.text.toString().trim()
                val fromDate = tvMealFromDate.text.toString().trim()

                if(fromDate.isEmpty() && toDate.isNotEmpty()){
                    showToast(MessageConstants.Errors.please_enter_from_date)
                    return@setOnClickListener
                }

                if(toDate.isEmpty() && fromDate.isNotEmpty()){
                    showToast(MessageConstants.Errors.please_enter_to_date)
                    return@setOnClickListener
                }

                var typeToSend: String?=null
                mealFilterData= MealLogFilter().apply {
                    this.toDate = toDate
                    this.fromDate = fromDate
                    typeToSend =  getDuration(this)

                    checkFilterApplied()
                }
                manageFilterDotVisibility()

                mealLogFragment.getData(
                        pageNo = 1,
                        fromDate = fromDate,
                        toDate = toDate,
                        type = typeToSend
                )
                bottomSheetDialog.dismiss()
            }
            btnReset.setOnClickListener {
                mealFilterData= null
                tvMealFromDate.text = ""
                tvMealToDate.text = ""
                displayAll = false
                ivToday.isSelected = false
                ivThisWeek.isSelected = false
                ivThisMonth.isSelected = false
                resetMealLogFilter()
                manageFilterDotVisibility()
                manageResetButton_Meal(tvMealFromDate,tvMealToDate, btnReset)
            }
        }
    }


    // Filter - Meal -RESET Button
    private fun manageResetButton_Meal(tvFromDate: AppCompatTextView,
                                       tvToDate: AppCompatTextView,
                                       btnReset: AppCompatButton) {
        val fromDate = tvFromDate.text.toString()
        val toDate = tvToDate.text.toString()

        if(fromDate.isEmpty() && toDate.isEmpty() && !displayAll
                && !today && !thisWeek && !thisMonth
        ){
            btnReset.beDisable()
            btnReset.setTextColor(ResourcesCompat.getColor(requireContext().resources, R.color.grey_4, null))
        }else{
            btnReset.beEnable()
            btnReset.setTextColor(ResourcesCompat.getColor(requireContext().resources, R.color.green_1, null))
        }

    }


    private fun setMealFilterData(bottomSheetDialog: BottomSheetDialog) {
        mealFilterData?.let {
            bottomSheetDialog.tvMealFromDate.text = it.fromDate ?: ""
            bottomSheetDialog.tvMealToDate.text = it.toDate ?: ""

            displayAll = (it.displayAll != null && it.displayAll!!)
            today =  (it.today != null && it.today!!)
            thisWeek = (it.thisWeek != null && it.thisWeek!!)
            thisMonth = (it.thisMonth != null && it.thisMonth!!)

            bottomSheetDialog.ivMealDisplayAll.isSelected = (it.displayAll != null && it.displayAll!!)
            bottomSheetDialog.ivToday.isSelected = (it.today != null && it.today!!)
            bottomSheetDialog.ivThisWeek.isSelected = (it.thisWeek != null && it.thisWeek!!)
            bottomSheetDialog.ivThisMonth.isSelected = (it.thisMonth != null && it.thisMonth!!)
        }
    }

    private fun getDuration(mealFilterData: MealLogFilter): String {
        return when {
            today -> {
                mealFilterData.today = true
                TODAY
            }
            thisWeek -> {
                mealFilterData.thisWeek = true
                THIS_WEEK
            }
            thisMonth -> {
                mealFilterData.thisMonth = true
                THIS_MONTH
            }
            displayAll -> {
                mealFilterData.displayAll = true
                ""
            }else ->{
                ""
            }
        }
    }
    private fun getPreSelectedDuration(mealFilterData: MealLogFilter): String {
        return when {
            (mealFilterData.today!=null && mealFilterData.today!!) -> {
                TODAY
            }
            (mealFilterData.thisWeek!=null && mealFilterData.thisWeek!!) -> {
                THIS_WEEK
            }
            (mealFilterData.thisMonth!=null && mealFilterData.thisMonth!!) -> {
                THIS_MONTH
            }
            else -> {
                ""
            }
        }
    }

    private fun resetMealLogFilter() {
        displayAll = false
        today = false
        thisWeek = false
        thisMonth = false
    }


    // Filter - Medication
    private fun showMedicationBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.apply {
            setContentView(R.layout.bottomsheet_medication_filter)
            setupFullHeight(bottomSheetDialog)
            show()
            resetMedicationFilter()

            setMedicationFilterData(this)
            manageResetButton_Med(tvMedFromDate, tvMedToDate, btnReset )

            ivAll.setOnClickListener {
                ivAll.isSelected = !ivAll.isSelected
                all = ivAll.isSelected
                ivPills.isSelected = false
                ivMG.isSelected = false
                ivML.isSelected = false

                pills = false
                mg = false
                ml = false
                manageResetButton_Med(tvMedFromDate, tvMedToDate, btnReset )

            }
            ivPills.setOnClickListener {
                ivPills.isSelected = !ivPills.isSelected
                pills = ivPills.isSelected

                ivAll.isSelected = false
                all = false
                ivMG.isSelected =false
                mg = false
                ivML.isSelected = false
                ml = false
                manageResetButton_Med(tvMedFromDate, tvMedToDate, btnReset )

            }
            ivMG.setOnClickListener {
                ivMG.isSelected = !ivMG.isSelected
                mg = ivMG.isSelected

                ivAll.isSelected = false
                all = false
                ivPills.isSelected =false
                pills = false
                ivML.isSelected = false
                ml = false
                manageResetButton_Med(tvMedFromDate, tvMedToDate, btnReset )

            }
            ivML.setOnClickListener {
                ivML.isSelected = !ivML.isSelected
                ml = ivML.isSelected

                ivAll.isSelected = false
                all = false
                ivPills.isSelected =false
                pills = false
                ivMG.isSelected = false
                mg = false
                manageResetButton_Med(tvMedFromDate, tvMedToDate, btnReset )

            }

            tvMedFromDate.setOnClickListener {
                tvMedToDate.text = ""
                selectDate(tvMedFromDate, true,
                        dateCallback = { dateInMillis: Long, isStartDate: Boolean ->
                            if (isStartDate) {
                                startDateForMed = dateInMillis
                            }
                            manageResetButton_Med(tvMedFromDate, tvMedToDate, btnReset )
                        })
            }
            tvMedToDate.setOnClickListener {
                selectDate(tvMedToDate, false, startDateForMed,
                        dateCallback = { dateInMillis: Long, isStartDate: Boolean ->
                            manageResetButton_Med(tvMedFromDate, tvMedToDate, btnReset )
                        })
            }
            btnMedApplyFilter.setOnClickListener {
                val medFragment = mPagerAdapter?.getItem(2) as MedicationLogsFragment
                val toDate = tvMedToDate.text.toString().trim()
                val fromDate = tvMedFromDate.text.toString().trim()

                if(fromDate.isEmpty() && toDate.isNotEmpty()){
                    showToast(MessageConstants.Errors.please_enter_from_date)
                    return@setOnClickListener
                }

                if(toDate.isEmpty() && fromDate.isNotEmpty()){
                    showToast(MessageConstants.Errors.please_enter_to_date)
                    return@setOnClickListener
                }

                var type :String?=null

                medFilterData= MedicationLogFilter().apply {
                    this.toDate = toDate
                    this.fromDate = fromDate
                    type =  getSelectedType(this)
                    checkFilterApplied()
                }
                manageFilterDotVisibility() // Med filter applied

                medFragment.getData(
                        pageNo = 1,
                        fromDate = fromDate,
                        toDate = toDate,
                        type = type
                )
                bottomSheetDialog.dismiss()
            }
            btnReset.setOnClickListener {
                medFilterData = null
                tvMedFromDate.text = ""
                tvMedToDate.text = ""
                ivPills.isSelected = false
                ivAll.isSelected = false
                ivMG.isSelected = false
                ivML.isSelected = false
                resetMedicationFilter()
                manageFilterDotVisibility()
                manageResetButton_Med(tvMedFromDate, tvMedToDate, btnReset )

            }
        }
    }


    // Filter - Med -RESET Button
    private fun manageResetButton_Med(tvFromDate: AppCompatTextView,
                                      tvToDate: AppCompatTextView,
                                      btnReset: AppCompatButton) {
        val fromDate = tvFromDate.text.toString()
        val toDate = tvToDate.text.toString()

        if(fromDate.isEmpty() && toDate.isEmpty() && !all
                && !pills && !mg && !ml
        ){
            btnReset.beDisable()
            btnReset.setTextColor(ResourcesCompat.getColor(requireContext().resources, R.color.grey_4, null))
        }else{
            btnReset.beEnable()
            btnReset.setTextColor(ResourcesCompat.getColor(requireContext().resources, R.color.green_1, null))
        }

    }
    private fun setMedicationFilterData(bottomSheetDialog: BottomSheetDialog) {
        medFilterData?.let {
            bottomSheetDialog.tvMedFromDate.text = it.fromDate ?: ""
            bottomSheetDialog.tvMedToDate.text = it.toDate ?: ""

            all = (it.all != null && it.all!!)
            pills = (it.pills != null && it.pills!!)
            mg = (it.mg != null && it.mg!!)
            ml = (it.ml != null && it.ml!!)

            bottomSheetDialog.ivAll.isSelected = (it.all != null && it.all!!)
            bottomSheetDialog.ivPills.isSelected = (it.pills != null && it.pills!!)
            bottomSheetDialog.ivMG.isSelected = (it.mg != null && it.mg!!)
            bottomSheetDialog.ivML.isSelected = (it.ml != null && it.ml!!)
        }
    }

    private fun getSelectedType(medFilterData: MedicationLogFilter): String {
        val sBuilder = StringBuilder()
        if (pills) {
            medFilterData.pills=true
            sBuilder.append(AppConstants.PILLES)
        }
        if (mg) {
            medFilterData.mg=true
            sBuilder.append(AppConstants.MG)
        }
        if (ml) {
            medFilterData.ml=true
            sBuilder.append(AppConstants.ML)
        }
        if (all) {
            medFilterData.all=true
        }
        return sBuilder.toString()
    }

    private fun getPreSelectedType(medFilterData: MedicationLogFilter): String {
        val sBuilder = StringBuilder()

        if (medFilterData.pills!=null && medFilterData.pills!!) {
            sBuilder.append(AppConstants.PILLES)
        }

        if (medFilterData.mg!=null && medFilterData.mg!!) {
            sBuilder.append(AppConstants.MG)
        }

        if (medFilterData.ml!=null && medFilterData.ml!!) {
            sBuilder.append(AppConstants.ML)
        }

        if (medFilterData.all!=null && medFilterData.all!!) {
            medFilterData.all=true
        }
        return sBuilder.toString()
    }

    private fun resetMedicationFilter() {
        all = false
        pills = false
        mg = false
        ml = false
    }

    private fun selectDate(dateTextView: AppCompatTextView,
                           isStartDate: Boolean,
                           minDateLimit: Long = 0L,
                           dateCallback: ((Long, Boolean) -> Any)? = null) {
        val c = Calendar.getInstance()
        mYear = c.get(Calendar.YEAR)
        mMonth = c.get(Calendar.MONTH)
        mDay = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
                DatePickerDialog(requireContext(), { _, year, monthOfYear, dayOfMonth ->
                    val strDate: String = year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth.toString()
                    dateTextView.text = getDifferentInfoFromDateInString(strDate, DATE_FORMAT_3, DATE_FORMAT_7)

                    dateCallback?.let {
                        val selectedDateInMillis = Calendar.getInstance()
                        selectedDateInMillis.set(year, monthOfYear, dayOfMonth)
                        it(selectedDateInMillis.timeInMillis, isStartDate)
                    }
                }, mYear, mMonth, mDay)
        val c1 = Calendar.getInstance()
        c1.add(Calendar.MONTH, -2)

        if(!isStartDate && minDateLimit!=0L){
            datePickerDialog.datePicker.minDate = minDateLimit
        }
        datePickerDialog.show()
    }

    override fun onEventClick(o: Any?, o1: Any?) {
        val type = o as String

        if (type == TestLogsFragment::class.java.simpleName && viewPager.currentItem == 0) {

            val testLogFragment = mPagerAdapter?.getItem(0) as TestLogsFragment

            if (testFilterData != null) {
                val filterTiming = getPreSelectedFilterTiming(testFilterData!!)

                testLogFragment.getData(
                        pageNo = 1,
                        testingTime = filterTiming,
                        fromDate = testFilterData!!.fromDate ?: "",
                        toDate = testFilterData!!.toDate ?: "",
                )
                manageFilterDotVisibility()

            } else {
                testLogFragment.getData(1)
            }
        } else if (type == MealLogsFragment::class.java.simpleName && viewPager.currentItem == 1) {

            val mealLogFragment = mPagerAdapter?.getItem(1) as MealLogsFragment

            if (mealFilterData != null) {
                val typeToSend = getPreSelectedDuration(mealFilterData!!)
                mealLogFragment.getData(
                        pageNo = 1,
                        fromDate = mealFilterData!!.fromDate ?: "",
                        toDate = mealFilterData!!.toDate ?: "",
                        type = typeToSend
                )
                manageFilterDotVisibility()
            } else {
                mealLogFragment.getData(1)
            }
        } else if (type == MedicationLogsFragment::class.java.simpleName && viewPager.currentItem == 2) {
            val medFragment = mPagerAdapter?.getItem(2) as MedicationLogsFragment

            if (medFilterData != null) {
                val type = getPreSelectedType(medFilterData!!)

                medFragment.getData(
                        pageNo = 1,
                        fromDate = medFilterData!!.fromDate ?: "",
                        toDate = medFilterData!!.toDate ?: "",
                        type = type
                )
                manageFilterDotVisibility() // Med filter applied

            } else {
                medFragment.getData(1)
            }
        }
    }


}