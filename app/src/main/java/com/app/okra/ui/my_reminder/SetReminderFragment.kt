package com.app.okra.ui.my_reminder

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.lifecycle.ViewModelProvider
import com.app.okra.R
import com.app.okra.base.BaseFragment
import com.app.okra.base.BaseViewModel
import com.app.okra.data.repo.ReminderRepoImpl
import com.app.okra.extension.viewModelFactory
import com.app.okra.ui.my_account.setting.measurement.CustomSpinnerAdapter
import com.app.okra.utils.*
import com.app.okra.utils.AppConstants.DateFormat.DATE_FORMAT_3
import com.app.okra.utils.AppConstants.DateFormat.DATE_FORMAT_8
import kotlinx.android.synthetic.main.fragment_set_reminder.*
import kotlinx.android.synthetic.main.layout_button.*
import java.text.SimpleDateFormat
import java.util.*

class SetReminderFragment : BaseFragment() {
    private var mYear: Int = 0
    private var mMonth: Int = 0
    private var mDay: Int = 0
    private lateinit var customSpinnerAdapter1: CustomSpinnerAdapter
    private lateinit var customSpinnerAdapter2: CustomSpinnerAdapter
    private var time: Int = 1
    private var strDate: String = ""
    private var strDateCalendarInstance: Calendar ?=null
    private var endDate: String = ""
    private var timeValue: String = ""
    private var reminderType: Int = 0
    private var min: Int = 0
    private var hour: Int = 0
    private val defaultTime by lazy {
        "12:00 pm"
    }

    private val repeatList by lazy {
        arrayListOf<String>()
    }
    private val endRepeatList by lazy {
        arrayListOf<String>()
    }

    private val viewModel by lazy {
        ViewModelProvider(this,
            viewModelFactory {
                ReminderViewModel(ReminderRepoImpl(apiServiceAuth))
            }
        ).get(ReminderViewModel::class.java)
    }

    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_set_reminder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData()
        setAdapter()
        setListener()
        setObserver()
    }

    private fun getData() {
        arguments?.let { it ->
            val data: String? = it.getString(AppConstants.DATA)
            reminderType = when {
                data.equals(AppConstants.DIABETES) -> {
                    tvTitle.text = getString(R.string.perform_diabetes_test)
                    2
                }
                data.equals(AppConstants.FOOD) -> {
                    tvTitle.text = getString(R.string.food_log)
                    1
                }
                else -> {
                    tvTitle.text = getString(R.string.title_take_medicine)
                    3
                }
            }
        }
    }

    private fun setListener() {
        clDate.setOnClickListener {
            time = 1
            selectDate()
        }

        clTime.setOnClickListener {
            selectTime()
        }

        ivDateSelector.setOnClickListener {
            if (ivDateSelector.isSelected) {
                ivDateSelector.isSelected = false
                tvDate.gravity = Gravity.CENTER
                tvDateValue.visibility = View.GONE
                if (!ivTimeSelector.isSelected)
                    layout_button.visibility = View.GONE
            } else {
                selectDate()
            }
        }

        ivTimeSelector.setOnClickListener {
            if (ivTimeSelector.isSelected) {
                ivTimeSelector.isSelected = false
                tvTime.gravity = Gravity.CENTER
                tvTimeValue.visibility = View.GONE
                if (!ivDateSelector.isSelected)
                    layout_button.visibility = View.GONE
            } else {
                selectTime()
            }
        }

        tvSetRepeat.setOnClickListener {
            spinnerRepeat.performClick()
        }

        tvSetEndRepeat.setOnClickListener {
            time = 2
            spinnerEndRepeat.performClick()
        }

        spinnerRepeat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                tvSetRepeat.text = repeatList[p2]
                if (p2 == 0) {
                    tvEndRepeat.visibility = View.GONE
                    clSpinnerEndRepeat.visibility = View.GONE
                } else {
                    tvEndRepeat.visibility = View.VISIBLE
                    clSpinnerEndRepeat.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        spinnerEndRepeat.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                tvSetEndRepeat.text = endRepeatList[p2]
                if (p2 == 1) {
                    time = 2
                    selectDate()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        btnCommon.setOnClickListener {
            val c = Calendar.getInstance()
            if (ivTimeSelector.isSelected && ivDateSelector.isSelected) {
                val date1: Date = c.time
                val startDate =
                    getDateFromPattern(AppConstants.DateFormat.DATE_FORMAT_2, "$strDate $timeValue")
                if (startDate?.before(date1) == true) {
                    showToast(getString(R.string.selected_time_should_not_be_lesser_than_current_time))
                } else
                    hitApi()
            } else {
                if (ivDateSelector.isSelected) {
                    val date1: Date = c.time
                    val startDate = getDateFromPattern(
                        AppConstants.DateFormat.DATE_FORMAT_2,
                        "$strDate $defaultTime"
                    )
                    if (startDate?.before(date1) == true) {
                        showToast("You can not select today's date as the default time of $defaultTime is already passed.")

                        ivDateSelector.isSelected = false
                        tvDate.gravity = Gravity.CENTER
                        tvDateValue.visibility = View.GONE
                        if (!ivTimeSelector.isSelected)
                            layout_button.visibility = View.GONE
                    } else
                        hitApi()
                } else if (ivTimeSelector.isSelected) {
                    val date1: Date = c.time
                    val df =
                        SimpleDateFormat(DATE_FORMAT_3, Locale.getDefault())
                    strDate = df.format(date1)
                    val startDate = getDateFromPattern(
                        AppConstants.DateFormat.DATE_FORMAT_2,
                        "$strDate $timeValue"
                    )
                    if (startDate?.before(date1) == true) {
                        showToast(MessageConstants.Errors.selected_time_should_not_be)

                        ivTimeSelector.isSelected = false
                        tvTime.gravity = Gravity.CENTER
                        tvTimeValue.visibility = View.GONE
                        if (!ivDateSelector.isSelected)
                            layout_button.visibility = View.GONE
                    } else
                        hitApi()
                }
            }
        }
    }

    private fun setAdapter() {
        btnCommon.text = getString(R.string.btn_save)
        repeatList.add(AppConstants.NEVER_TEXT)
        repeatList.add(AppConstants.DAILY)
        repeatList.add(AppConstants.WEEKLY)
        repeatList.add(AppConstants.MONTHLY)

        customSpinnerAdapter1 = CustomSpinnerAdapter(requireActivity(), repeatList)
        spinnerRepeat.adapter = customSpinnerAdapter1
        tvSetRepeat.text = repeatList[0]

        endRepeatList.add(AppConstants.NEVER_TEXT)
        endRepeatList.add(AppConstants.END_REPEAT_DATE)

        customSpinnerAdapter2 = CustomSpinnerAdapter(requireActivity(), endRepeatList)
        spinnerEndRepeat.adapter = customSpinnerAdapter2
        tvSetEndRepeat.text = endRepeatList[0]
    }

    private fun hitApi() {
        setReminder(hour, min)
        val startDate: String
        val timeDate: String
        val endDateValue: String
        val obj = HashMap<String, Any>()
        if (ivDateSelector.isSelected) {
            startDate = convertLocalTimeZoneToUTC(DATE_FORMAT_3, strDate)
        } else {
            val c = Calendar.getInstance().time
            val df = SimpleDateFormat(DATE_FORMAT_3, Locale.getDefault())
            strDate = df.format(c)
            startDate = convertLocalTimeZoneToUTC(DATE_FORMAT_3, strDate)
        }
        obj[AppConstants.RequestParam.reminderType] = reminderType
        obj[AppConstants.RequestParam.startDate] = startDate

        if (ivTimeSelector.isSelected) {
            timeDate =
                convertLocalTimeZoneToUTC(
                    AppConstants.DateFormat.DATE_FORMAT_2,
                    "$strDate $timeValue"
                )
        } else
            timeDate =
                convertLocalTimeZoneToUTC(
                    AppConstants.DateFormat.DATE_FORMAT_2,
                    "$strDate 12:00 pm"
                )
        obj[AppConstants.RequestParam.time] = timeDate


        obj[AppConstants.RequestParam.repeatType] =
            viewModel.getRepeatType(tvSetRepeat.text.toString(), false)

        val endRepeatType: String = if (tvSetEndRepeat.text.toString() == AppConstants.NEVER_TEXT)
            AppConstants.NEVER
        else
            AppConstants.EVERY_DAY

        obj[AppConstants.RequestParam.endRepeatType] = endRepeatType

        if (endRepeatType == AppConstants.EVERY_DAY) {
            endDateValue = convertLocalTimeZoneToUTC(DATE_FORMAT_3, endDate)
            obj[AppConstants.RequestParam.endDate] = endDateValue
        }

        viewModel.setReminder(obj)
    }


    private fun selectDate() {
        val c = Calendar.getInstance()
        mYear = c.get(Calendar.YEAR)
        mMonth = c.get(Calendar.MONTH)
        mDay = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(requireContext(), { _, year, monthOfYear, dayOfMonth ->
                val date = year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth.toString()
                mYear = year
                mMonth = monthOfYear
                mDay = dayOfMonth

                if (time == 1) { // for Date
                    layout_button.visibility = View.VISIBLE
                    ivDateSelector.isSelected = true
                    tvDate.gravity = Gravity.BOTTOM
                    tvDateValue.visibility = View.VISIBLE
                    strDateCalendarInstance = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, mDay)
                        set(Calendar.MONTH, mMonth)
                        set(Calendar.YEAR, mYear)
                    }
                    strDate = date
                    tvDateValue.text = getDifferentInfoFromDateInString(date, DATE_FORMAT_3, DATE_FORMAT_8)
                    if (ivTimeSelector.isSelected) {
                        val date1: Date = c.time
                        val startDate = getDateFromPattern(
                            AppConstants.DateFormat.DATE_FORMAT_2,
                            "$strDate $timeValue"
                        )
                        if (startDate?.before(date1) == true) {
                            ivTimeSelector.isSelected = false
                            tvTime.gravity = Gravity.CENTER
                            tvTimeValue.visibility = View.GONE
                        }
                    }
                } else if (time == 2) { // for END Date
                    endDate = date
                    tvSetEndRepeat.text = date
                }
            }, mYear, mMonth, mDay)
        val c1 = Calendar.getInstance()
        c1.add(Calendar.MONTH, -2)

        if(strDateCalendarInstance!=null){
            strDateCalendarInstance!!.add(Calendar.DATE, 1)
            datePickerDialog.datePicker.minDate = strDateCalendarInstance!!.timeInMillis
        }else{
            datePickerDialog.datePicker.minDate = System.currentTimeMillis()

        }
        datePickerDialog.show()
    }

    private fun selectTime() {
      val calendar = Calendar.getInstance()
       TimePickerDialog(
            requireContext(),
            timePickerDialogListener,
            12,
            10,
            false
        ).apply {
            updateTime(calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE))
            show()
        }
    }

    private val timePickerDialogListener: TimePickerDialog.OnTimeSetListener =
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                val formattedTime: String = when {
                    hourOfDay == 0 -> {
                        if (minute < 10) {
                            "${hourOfDay + 12}:0${minute} AM"
                        } else {
                            "${hourOfDay + 12}:${minute} AM"
                        }
                    }
                    hourOfDay > 12 -> {
                        if (minute < 10) {
                            "${hourOfDay - 12}:0${minute} PM"
                        } else {
                            "${hourOfDay - 12}:${minute} PM"
                        }
                    }
                    hourOfDay == 12 -> {
                        if (minute < 10) {
                            "${hourOfDay}:0${minute} PM"
                        } else {
                            "${hourOfDay}:${minute} PM"
                        }
                    }
                    else -> {
                        if (minute < 10) {
                            "${hourOfDay}:${minute} AM"
                        } else {
                            "${hourOfDay}:${minute} AM"
                        }
                    }
                }
                timeValue = formattedTime
                hour = hourOfDay
                min = minute
                layout_button.visibility = View.VISIBLE
                ivTimeSelector.isSelected = true
                tvTime.gravity = Gravity.BOTTOM
                tvTimeValue.visibility = View.VISIBLE
                tvTimeValue.text = formattedTime
            }

    private fun setObserver() {
        setBaseObservers(viewModel, this)
        viewModel._setReminderLiveData.observe(viewLifecycleOwner) { it ->
            showToast(getString(R.string.saved_successfully))

            EventLiveData.eventLiveData.value =
                    Event(EventLiveData.EventData(SetReminderFragment::class.java.simpleName))
            navController.popBackStack()
        }
    }

    private fun setReminder(
        hour: Int,
        min: Int,
    ) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar[Calendar.DAY_OF_MONTH] = mDay
        calendar[Calendar.MONTH] = mMonth
        calendar[Calendar.YEAR] = mYear
        calendar[Calendar.HOUR_OF_DAY] = hour
        calendar[Calendar.MINUTE] = min
        calendar[Calendar.SECOND] = 0
        val alarmMgr = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val notificationTitle = when (reminderType) {
            1 -> {
                "Food Log"
            }
            2 -> {
                "Perform Diabetes Test"
            }
            else -> {
                "Take Medicine"
            }
        }

        val notficationDesc = getNotificationDescription(calendar)
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(AppConstants.Intent_Constant.NOTIFICATION_TITLE, notificationTitle)
        intent.putExtra(AppConstants.Intent_Constant.NOTIFICATION_DESC, notficationDesc)
        val alarmIntent = PendingIntent.getBroadcast(
            context,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        println(":::: Notification time: ${getDateFromTimeStamp( calendar.timeInMillis,
            AppConstants.DateFormat.DATE_FORMAT_5)}")

        when( tvSetRepeat.text.toString() ) {
            AppConstants.DAILY -> {

                 alarmMgr.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 1,
                    alarmIntent
                )
            }
            AppConstants.MONTHLY ->{
                alarmMgr.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_HOUR * (1 / 24),
                    alarmIntent
                )
                alarmMgr.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 30,
                alarmIntent
            )
            }
            AppConstants.WEEKLY -> alarmMgr.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                alarmIntent
            )
            else -> {
                alarmMgr.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_HOUR * (1 / 24),
                    alarmIntent
                )
            }
        }


        calendar.add(Calendar.DAY_OF_MONTH, mDay)
    }

    private fun getNotificationDescription(calendar: Calendar): String {

        val timeToShow = getDateFromTimeStamp(calendar.timeInMillis, "hh:mm a")
        val repeatType = viewModel.getRepeatType(tvSetRepeat.text.toString(), true)

        return when (repeatType) {
            AppConstants.DAILY -> "Daily at $timeToShow"
            AppConstants.WEEKLY -> {
                val day = getDateFromTimeStamp(calendar.timeInMillis, "EEEE")
                "Every $day at $timeToShow"
            }
            AppConstants.MONTHLY -> {
                "Every Month at $timeToShow"
            }
            else -> ""
        }

    }
}