package com.app.okra.ui.logbook.test.test_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import com.app.okra.R
import com.app.okra.base.BaseFragment
import com.app.okra.base.BaseViewModel
import com.app.okra.models.Data
import com.app.okra.models.MealData
import com.app.okra.ui.logbook.meal.meal_detail.EditMealDetailsFragment
import com.app.okra.utils.AppConstants
import com.app.okra.utils.AppConstants.RequestOrResultCodes.RESULT_CODE_MEAL_LOG_UPDATED
import com.app.okra.utils.AppConstants.RequestOrResultCodes.RESULT_CODE_TEST_LOG_UPDATED
import com.app.okra.utils.getDateFromISOInString
import com.app.okra.utils.getMealTime
import kotlinx.android.synthetic.main.fragment_successful_updated.*
import kotlinx.android.synthetic.main.layout_button.*

class SuccessfulUpdatedFragment : BaseFragment() {
    override fun getViewModel(): BaseViewModel? {
        return null
    }

    private var fromScreen: String?=null
    private var data: Data? = null
    private var dataMeal: MealData? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_successful_updated, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData()
        setView()

        setListener()
    }

    private fun setView() {
        btnCommon.text = getString(R.string.btn_ok)

        fromScreen?.let{
            if(fromScreen == EditMealDetailsFragment::class.java.simpleName){
                tvSuccessfullyUpdated.text = getString(R.string.successfully_updated_meal_details)
                tvMessage.text = getString(R.string.your_meal_details_has_been_updated_successfully)
            }
        }
    }

    private fun setListener() {
        btnCommon.setOnClickListener{

            if(fromScreen!=null) {
                if(fromScreen == EditMealDetailsFragment::class.java.simpleName){
                    requireActivity().apply {
                        setResult(RESULT_CODE_MEAL_LOG_UPDATED)
                    }

                    val bundle = Bundle()
                    bundle.putParcelable(AppConstants.DATA, dataMeal)
                    val navOptions = NavOptions.Builder().setPopUpTo(R.id.mealDetails, true).build()
                    navController.navigate(R.id.mealDetails, bundle, navOptions)
                }
                else if(fromScreen == EditTestDetailsFragment::class.java.simpleName){
                    requireActivity().apply {
                        setResult(RESULT_CODE_TEST_LOG_UPDATED)
                    }

                    val bundle = Bundle()
                    bundle.putParcelable(AppConstants.DATA, data)
                    val navOptions = NavOptions.Builder().setPopUpTo(R.id.testDetails, true).build()
                    navController.navigate(R.id.testDetails, bundle, navOptions)
                }
            }else{
                navController.popBackStack()
            }
        }
    }

    private fun getData() {
        arguments?.let { it ->
            if (it.containsKey(AppConstants.Intent_Constant.FROM_SCREEN)) {
                fromScreen = it.getString(AppConstants.Intent_Constant.FROM_SCREEN)
            }

            if (requireArguments().containsKey(AppConstants.DATA)) {
                fromScreen?.let { it1 ->
                    if(it1 == EditMealDetailsFragment::class.java.simpleName){
                        dataMeal = it.getParcelable(AppConstants.DATA)
                    }else{
                        data = it.getParcelable(AppConstants.DATA)

                    }
                }
            }


        }
    }

}