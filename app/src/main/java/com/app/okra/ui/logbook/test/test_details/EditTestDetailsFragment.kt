package com.app.okra.ui.logbook.test.test_details

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.ViewModelProvider
import com.app.okra.R
import com.app.okra.base.BaseFragment
import com.app.okra.base.BaseViewModel
import com.app.okra.data.repo.TestLogsRepoImpl
import com.app.okra.extension.*
import com.app.okra.models.Data
import com.app.okra.ui.logbook.test.TestLogsViewModel
import com.app.okra.ui.my_account.setting.measurement.CustomSpinnerAdapter
import com.app.okra.utils.*
import com.app.okra.utils.AppConstants.DateFormat.DATE_FORMAT_5
import kotlinx.android.synthetic.main.fragment_edit_test_details.*
import kotlinx.android.synthetic.main.layout_header.*
import java.util.*

class EditTestDetailsFragment : BaseFragment() , View.OnClickListener{

    private  var strDate: String?=null
    private lateinit var customSpinnerAdapter: CustomSpinnerAdapter
    private var data: Data? = null
    private var mYear: Int = 0
    private var mMonth: Int = 0
    private var mDay: Int = 0
    private var mHour: Int = 0
    private var mMin: Int = 0

    private val timingList by lazy {
        arrayListOf<String>()
    }

    private val viewModel by lazy {
        ViewModelProvider(this,
            viewModelFactory {
                TestLogsViewModel(TestLogsRepoImpl(apiServiceAuth))
            }
        ).get(TestLogsViewModel::class.java)
    }

    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_test_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListener()
        getData()
        setView()
        setAdapter()
        setObserver()
    }

    private fun setAdapter() {
        timingList.add(AppConstants.SELECT_TESTING_TIME)
        timingList.add(AppConstants.BEFORE_MEAL_TEXT)
        timingList.add(AppConstants.AFTER_MEAL_TEXT)
        timingList.add(AppConstants.POST_MEDICINE_TEXT)
        timingList.add(AppConstants.POST_WORKOUT_TEXT)
        timingList.add(AppConstants.CONTROLE_SOLUTION_TEXT)

        customSpinnerAdapter = CustomSpinnerAdapter(requireActivity(), timingList)
        spinner.adapter = customSpinnerAdapter

        var index = 0

        data?.testingTime?.let {
            if(it.isNotEmpty()){
                for((i, data) in timingList.withIndex()){
                    if(getMealTime(it) == data){
                        index = i
                        break;
                    }
                }
            }
        }
        spinner.setSelection(index)
        tvSetTestingTime.text = timingList[index]
    }

    private fun setView() {
        ivRight.beInvisible()
        ivDelete.beGone()
        btnSave.beVisible()
    }

    private fun setObserver() {
        setBaseObservers(viewModel, this)
        viewModel._updateTestLiveData.observe(viewLifecycleOwner) {
            val bundle = Bundle()
            bundle.putParcelable(AppConstants.DATA, data)
            bundle.putString(AppConstants.Intent_Constant.FROM_SCREEN,
                    EditTestDetailsFragment::class.java.simpleName )
            navController.navigate(
                R.id.action_editTestDetails_to_successfulUpdatedFragment,
                bundle
            )
        }
    }

    private fun setListener() {
        ivBack.setOnClickListener(this)

        tvSetTestingTime.setOnClickListener(this)
        btnSave.setOnClickListener (this)

        tvDate.setOnClickListener {
            selectDate(tvDate)
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                tvSetTestingTime.text = timingList[p2]
                if(p2 ==0 && etNotes.text.isNullOrEmpty()){
                    btnSave.beGone()
                }else{
                    btnSave.beVisible()

                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        etNotes.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0.isNullOrEmpty() && tvSetTestingTime.text.toString().trim() == AppConstants.SELECT_TESTING_TIME){
                    btnSave.beGone()
                }else{
                    btnSave.beVisible()
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })
    }

    private fun getData() {
        arguments?.let {
            data = it.getParcelable(AppConstants.DATA)

            tvDate.text =
                data?.date?.let { it1 ->
                    getDateFromISOInString(it1, AppConstants.DateFormat.DATE_FORMAT_1)
                }
            tvBloodGlucoseValue.getGlucoseToSet(data?.bloodGlucose)
            tvDeviceIdValue.text = data?.deviceId ?: ""
            tvDeviceNameValue.text = data?.deviceName ?: ""
            etNotes.setText(data?.additionalNotes ?: "")
        }
    }


    private fun selectDate(tvDate: AppCompatTextView) {
        val c = Calendar.getInstance()
        mYear = c.get(Calendar.YEAR)
        mMonth = c.get(Calendar.MONTH)
        mDay = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(requireContext(), { view, year, monthOfYear, dayOfMonth ->
                strDate = year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth.toString()
            showTimePicker(tvDate, strDate!!)
            }, mYear, mMonth, mDay)
        val c1 = Calendar.getInstance()
        c1.add(Calendar.MONTH, -2)
        datePickerDialog.show()

    }

    private fun showTimePicker(
        tvDate: AppCompatTextView,
        selectedDate: String
    ) {
        val c = Calendar.getInstance()
        mHour = c.get(Calendar.HOUR_OF_DAY)
        mMin = c.get(Calendar.MINUTE)

        val tpd = TimePickerDialog(
            requireContext(), { timePicker, hour, minute ->
                strDate = "$selectedDate $hour:$minute"
                tvDate.text =  getDifferentInfoFromDateInString(strDate!!, DATE_FORMAT_5,
                    AppConstants.DateFormat.DATE_FORMAT_1
                )
            },
            mHour,
            mMin,
            true
        )
        tpd.show()

    }


    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.ivBack -> activity?.finish()
            R.id.tvSetTestingTime -> spinner.performClick()
            R.id.btnSave -> {
                var bloodGlucose = 0
                var bloodPresure = 0
                var insulin = 0
                var testId = ""
                var testingTime = ""
                data?.apply {
                    bloodGlucose = if (!this.bloodGlucose.isNullOrEmpty()) {
                        this.bloodGlucose!!.toInt()
                    } else 0

                    bloodPresure = if (!this.datbloodPressuree.isNullOrEmpty()) {
                        this.datbloodPressuree!!.toInt()
                    } else 0

                    insulin = if (!this.insulin.isNullOrEmpty()) {
                        this.insulin!!.toInt()
                    } else 0

                    testId = if (!_id.isNullOrEmpty()) {
                        _id!!
                    } else ""

                    testId = if (!_id.isNullOrEmpty()) {
                        _id!!
                    } else ""

                    val newTestTime = tvSetTestingTime.text.toString().trim()
                    testingTime =
                        if (newTestTime.isEmpty() || newTestTime == AppConstants.SELECT_TESTING_TIME) ({
                            if(!this.testingTime.isNullOrEmpty()){
                                this.testingTime
                            }else ""
                        }).toString()
                        else {
                            getMealTime(newTestTime, false)
                        }

                    _id = testId
                    this.bloodGlucose = bloodGlucose.toString()
                    this.datbloodPressuree = bloodPresure.toString()
                    this.insulin = insulin.toString()
                    this.additionalNotes = etNotes.text.toString().trim()
                    this.testingTime =testingTime

                    if(!strDate.isNullOrEmpty()){
                        this.date =getISOFromDate(strDate!!, DATE_FORMAT_5)
                    }
                }

                if(etNotes.text.isNullOrEmpty()){
                    showToast(MessageConstants.Messages.addtional_notes_not_allowed)
                }else {

                    viewModel.prepareUpdateRequest(
                            testId = testId,
                            bloodGlucose = bloodGlucose,
                            bloodPressure = bloodPresure,
                            insulin = insulin,
                            additionalNotes = etNotes.text.toString().trim(),
                            mealsAfter = null,
                            mealsBefore = null,
                            testingTime = testingTime,
                            date = strDate
                    )
                    viewModel.updateTest()

                }

            }
        }
    }


}