package com.app.okra.ui.logbook.meal.meal_detail

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.app.okra.R
import com.app.okra.base.BaseFragment
import com.app.okra.base.BaseViewModel
import com.app.okra.data.repo.MealLogsRepoImpl
import com.app.okra.extension.beVisible
import com.app.okra.extension.loadUserImageFromUrl
import com.app.okra.extension.viewModelFactory
import com.app.okra.models.CommonData
import com.app.okra.models.FoodItemsRequest
import com.app.okra.models.MealData
import com.app.okra.ui.logbook.meal.MealLogsViewModel
import com.app.okra.utils.*
import kotlinx.android.synthetic.main.fragment_edit_meal_details.*
import kotlinx.android.synthetic.main.layout_header.*

class EditMealDetailsFragment : BaseFragment() {
    private var data: MealData? = null
    private var isDataModified: Boolean=false

    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    private val viewModel by lazy {
        ViewModelProvider(this,
                viewModelFactory {
                    MealLogsViewModel(MealLogsRepoImpl(apiServiceAuth))
                }
        ).get(MealLogsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_meal_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        getData()
        setListener()
        setObserver()
    }

    private fun setUpToolbar() {
        btnSave.beVisible()
    }

    private fun setListener() {
        ivBack.setOnClickListener {
            checkDataExistence()
        }

        btnSave.setOnClickListener {
            var mealId = ""
            var date = ""
            var image = ""
            var foodItemsRequest :FoodItemsRequest
            val foodList : ArrayList<FoodItemsRequest> = ArrayList()


            data?.apply {
                mealId = if (!_id.isNullOrEmpty()) {
                    _id!!
                } else ""
                date = if (!this.date.isNullOrEmpty()) {
                    this.date!!
                } else ""
                image = if (!this.image.isNullOrEmpty()) {
                    this.image!!
                } else ""

                if(!foodItems.isNullOrEmpty()) {
                    foodItemsRequest = FoodItemsRequest(
                            foodItems?.get(0)?.item,
                            foodItems?.get(0)?.type,
                            foodItems?.get(0)?.servingSize
                    )
                    foodList.add(foodItemsRequest)
                }
            }

            val noOfServing = etNoOfServing.text.toString()
            var calorie = etCaloriesValue.text.toString()
            var carbs = etCarbsValue.text.toString()
            var fat = etFatValue.text.toString()
            var protein = etProteinValue.text.toString()
            var cDataCalorie :CommonData? = null
            var cDataCarb :CommonData? = null
            var cDataFat :CommonData? = null
            var cDataProtein :CommonData? = null

            calorie= if(calorie.isNotEmpty()){
                calorie
            }else "0"

            data!!.calories?.value = calorie
            cDataCalorie = CommonData(calorie,data?.calories?.unit)

            carbs =  if(carbs.isNotEmpty()){
               carbs
            }else "0"

            data!!.carbs?.value = carbs
            cDataCarb = CommonData(carbs,data?.carbs?.unit)

            fat = if(fat.isNotEmpty()){
                fat
            }else "0"

            data!!.fat?.value = fat
            cDataFat = CommonData(fat,data?.fat?.unit)

            protein= if(protein.isNotEmpty()){
               protein
            }else "0"

            data!!.protien?.value = protein
            cDataProtein =CommonData(protein,data?.protien?.unit)

            viewModel.prepareUpdateRequest(
                    mealsId  = mealId,
                    date = date,
                    image = image,
                    foodItems = foodList,
                    foodType = data?.foodType,
                    calories = cDataCalorie,
                    carbs = cDataCarb,
                    fat = cDataFat,
                    protein = cDataProtein,
                noOfServing=noOfServing
            )
            viewModel.updateMeal()
        }

        etCaloriesValue.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                isDataModified = true
            }
            override fun afterTextChanged(p0: Editable?) { }
        })

        etCarbsValue.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                isDataModified = true
            }
            override fun afterTextChanged(p0: Editable?) { }
        })
        etFatValue.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                isDataModified = true
            }
            override fun afterTextChanged(p0: Editable?) { }
        })
        etProteinValue.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                isDataModified = true
            }
            override fun afterTextChanged(p0: Editable?) { }
        })


    }

    private fun setObserver() {
        setBaseObservers(viewModel, this)
        viewModel._updateMealLiveData.observe(viewLifecycleOwner) {
            val bundle = Bundle()
            bundle.putParcelable(AppConstants.DATA, data)
            bundle.putString(AppConstants.Intent_Constant.FROM_SCREEN,
                    EditMealDetailsFragment::class.java.simpleName )
            navController.navigate(R.id.action_editMealDetails_to_successfulUpdatedFragment, bundle)
        }
    }

    private fun getData() {
        arguments?.let { it ->
            data = it.getParcelable(AppConstants.DATA)

            tvDateValue.text =
                    data?.date?.let { it1 ->
                        getDateFromISOInString(
                                it1,
                                formatYouWant = AppConstants.DateFormat.DATE_FORMAT_1
                        )
                    }

            data?.image?.let {
                ivFood.loadUserImageFromUrl(
                        requireContext(),
                        it,
                        R.mipmap.ic_person_placeholder_bg
                )
            }

            data?.foodType?.let {
                tvFoodTypeValue.text = it
            }

            val prevCalorieValue = String.format("%.2f",data?.calories?.value!!.toBigDecimal())
            val prevCarbValue = String.format("%.2f",data?.carbs?.value!!.toBigDecimal())
            val prevFatValue = String.format("%.2f",data?.fat?.value!!.toBigDecimal())
            val prevProteinValue = String.format("%.2f",data?.protien?.value!!.toBigDecimal())
            etCaloriesValue.setText(prevCalorieValue)
            etCarbsValue.setText(prevCarbValue)
            etFatValue.setText(prevFatValue)
            etProteinValue.setText(prevProteinValue)

          if(!data?.noOfServings.isNullOrEmpty()) {
              etNoOfServing.setText(data?.noOfServings)
          }
        }
    }

    fun checkDataExistence(){
        if(isDataModified){
            showCustomAlertDialog(requireContext(), object : Listeners.DialogListener{
                        override fun onOkClick(dialog: DialogInterface?) {
                            navController.popBackStack()
                            dialog?.dismiss()
                        }
                        override fun onCancelClick(dialog: DialogInterface?) {
                            dialog?.dismiss()
                        }
                    },
                    MessageConstants.Messages.unsaved_meal_data,
                    true,
                    positiveButtonText = getString(R.string.btn_ok),
                    negativeButtonText = getString(R.string.btn_cancel),
                    title = getString(R.string.unsaved_meal)
            )
        }else{
            navController.popBackStack()
        }
    }


}