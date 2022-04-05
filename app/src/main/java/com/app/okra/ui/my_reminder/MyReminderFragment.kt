package com.app.okra.ui.my_reminder

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.app.okra.R
import com.app.okra.base.BaseFragment
import com.app.okra.base.BaseViewModel
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.data.repo.ReminderRepoImpl
import com.app.okra.extension.viewModelFactory
import com.app.okra.ui.connected_devices.ConnectionStatusFragment
import com.app.okra.utils.AppConstants
import com.app.okra.utils.EventLiveData
import com.app.okra.utils.convertUtc2Local
import kotlinx.android.synthetic.main.fragment_my_reminder.*

class MyReminderFragment : BaseFragment() {

    private val viewModel by lazy {
        ViewModelProvider(this,
            viewModelFactory {
                ReminderViewModel(ReminderRepoImpl(apiServiceAuth))
            }
        ).get(ReminderViewModel::class.java)
    }

    private val userId by lazy {
        PreferenceManager.getString(AppConstants.Pref_Key.USER_ID)
    }

    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_reminder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
        setListener()
        viewModel.getProfileInfo(userId!!)
    }

    private fun setObserver() {
        viewModel._profileInfoLiveData.observe(viewLifecycleOwner) { it ->
            it?.data?.let {
                if(it.diabetesReminder?.startDate!=null) {
                    tvAddTest.gravity = Gravity.BOTTOM
                    tvDiabetesValue.visibility = View.VISIBLE
                    val startDate = convertUtc2Local(it.diabetesReminder?.startDate, "dd MMM yyyy")
                    val time = convertUtc2Local(it.diabetesReminder?.time,"hh:mm a")
                    tvDiabetesValue.text = startDate+ ", "+time+ ", "+ getRepeatType(it.diabetesReminder?.repeatType)
                }
                if(it.foodReminder?.startDate!=null) {
                    tvAddFood.gravity = Gravity.BOTTOM
                    tvFoodValue.visibility = View.VISIBLE
                    val startDate = convertUtc2Local(it.foodReminder?.startDate, "dd MMM yyyy")
                    val time = convertUtc2Local(it.foodReminder?.time, "hh:mm a")
                    tvFoodValue.text = startDate+ ", "+time+ ", "+ getRepeatType(it.foodReminder?.repeatType)
                }
                if(it.medicationReminder?.startDate!=null) {
                    tvAddMedicine.gravity = Gravity.BOTTOM
                    tvMedicineValue.visibility = View.VISIBLE
                    val startDate = convertUtc2Local(it.medicationReminder?.startDate, "dd MMM yyyy")
                    val time = convertUtc2Local(it.medicationReminder?.time, "hh:mm a")
                    tvMedicineValue.text = startDate+ ", "+time+ ", "+ getRepeatType(it.medicationReminder?.repeatType)
                }
            }
        }

        EventLiveData.eventLiveData.observe(viewLifecycleOwner){event ->
            event.peekContent().let {
                if (!it.type.isNullOrEmpty() && it.type == SetReminderFragment::class.java.simpleName) {
                    event.update()
                    viewModel.getProfileInfo(userId!!)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MyReminderActivity).setSubTitleVisibility(false)

    }

    private fun setListener() {
        clDiabetesTest.setOnClickListener {
            (activity as MyReminderActivity).setSubTitle(getString(R.string.perform_diabetes_test))
            val bundle = Bundle()
            bundle.putString(AppConstants.DATA, AppConstants.DIABETES)
            navController.navigate(R.id.action_myReminderFragment_to_setReminderFragment, bundle)
        }

        clAddFood.setOnClickListener {
            (activity as MyReminderActivity).setSubTitle(getString(R.string.food_log))
            val bundle = Bundle()
            bundle.putString(AppConstants.DATA, AppConstants.FOOD)
            navController.navigate(R.id.action_myReminderFragment_to_setReminderFragment, bundle)
        }

        clAddMedicine.setOnClickListener {
            (activity as MyReminderActivity).setSubTitle(getString(R.string.title_take_medicine))
            val bundle = Bundle()
            bundle.putString(AppConstants.DATA, AppConstants.MEDICINE)
            navController.navigate(R.id.action_myReminderFragment_to_setReminderFragment, bundle)
        }
    }

    private fun getRepeatType(type: String?): String {
         return when (type) {
                AppConstants.EVERY_DAY -> {
                    AppConstants.EVERY_DAY_TEXT
                }
                AppConstants.EVERY_MONTH -> {
                    AppConstants.MONTHLY
                }
                AppConstants.EVERY_WEEK -> {
                    AppConstants.WEEKLY
                }
                else -> AppConstants.NEVER_TEXT
            }
    }
}