package com.app.okra.ui.logbook.test.test_details

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.app.okra.R
import com.app.okra.base.BaseFragment
import com.app.okra.base.BaseViewModel
import com.app.okra.data.repo.TestLogsRepoImpl
import com.app.okra.extension.getGlucoseToSet
import com.app.okra.extension.viewModelFactory
import com.app.okra.models.Data
import com.app.okra.ui.logbook.test.TestLogsViewModel
import com.app.okra.utils.*
import com.app.okra.utils.AppConstants.DateFormat.DATE_FORMAT_1
import kotlinx.android.synthetic.main.fragment_test_details.*
import kotlinx.android.synthetic.main.layout_header.*

class TestDetailsFragment : BaseFragment(), Listeners.DialogListener {

    private var data: Data? = null

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private val viewModel by lazy {
        ViewModelProvider(this,
            viewModelFactory { TestLogsViewModel(TestLogsRepoImpl(apiServiceAuth)) }
        ).get(TestLogsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_test_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        setListener()
        getData()
        setObserver()
    }

    private fun setObserver() {
        setBaseObservers(viewModel, this)
        viewModel._deleteTestLiveData.observe(viewLifecycleOwner) { it ->
            requireActivity().apply{
                setResult(AppConstants.RequestOrResultCodes.RESULT_CODE_TEST_LOG_DELETED)
                finish()
            }
        }
    }

    private fun setUpToolbar() {
        tvTitle.text = getString(R.string.title_test_details)
        ivRight.visibility = View.VISIBLE
        ivDelete.visibility = View.GONE
    }

    private fun setListener() {
        ivBack.setOnClickListener {
            activity?.finish()
        }

        ivRight.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable(AppConstants.DATA, arguments?.getParcelable(AppConstants.DATA))
            navController.navigate(R.id.action_testDetails_to_editTestDetails, bundle)
        }

        ivDelete.setOnClickListener {
            showCustomAlertDialog(
                context,
                this,
                getString(R.string.are_you_sure_you_want_to_delete_the_added_test),
                true,
                positiveButtonText=   getString(R.string.btn_cancel),
                negativeButtonText = getString(R.string.btn_delete),
                title = getString(R.string.delete_test)
            )
        }
    }

    private fun getData() {
        arguments?.let { it ->
            data = it.getParcelable(AppConstants.DATA)

            tvDateValue.text =
                data?.date?.let { it1 ->
                    getDateFromISOInString(
                        it1,
                        formatYouWant = DATE_FORMAT_1
                    )
                }

            data?.testingTime?.let {
                tvTestingTimeValue.text =if(getMealTime(it)!= AppConstants.ALL_TEXT){
                    getMealTime(it)
                }else{
                    ""
                }
            }


            tvBloodGlucoseValue.getGlucoseToSet(data?.bloodGlucose)
            tvDeviceIdValue.text = data?.deviceId ?: ""
            tvDeviceNameValue.text = data?.deviceName ?: ""
        }
    }

    override fun onOkClick(dialog: DialogInterface?) {
        dialog?.dismiss()
    }

    override fun onCancelClick(dialog: DialogInterface?) {
        viewModel.deleteTest(data?._id!!)
        dialog?.dismiss()
    }

}