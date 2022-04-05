package com.app.okra.ui.add_meal

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.*
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.extension.loadUserImageFromUrl
import com.app.okra.models.FoodRecognintionResponse
import com.app.okra.models.Items
import com.app.okra.models.Results
import com.app.okra.models.ServingSize
import com.app.okra.ui.add_meal.contract.AddMealContracts
import com.app.okra.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_image_view.*
import kotlinx.android.synthetic.main.bottomsheet_choose_item.*
import kotlinx.android.synthetic.main.bottomsheet_final_selected_meal.*


class ImageViewActivity : BaseActivity(), Listeners.DialogListener {

    private lateinit var enteredNoOfServing: String
    var data: FoodRecognintionResponse? = null

    private lateinit var foodNameAdapter: FoodItemNameAdapter
    private lateinit var foodTypeAdapter: FoodItemAdapter
    private lateinit var servingAdapter : FoodServingAdapter
    private var selectedItem : Items?=null
    private lateinit var bottomSheetDialog :BottomSheetDialog
    private lateinit var bottomSheetConfirmationDialog :BottomSheetDialog

    private val foodNameList by lazy {  ArrayList<Results>() }
    private val foodTypeList by lazy {  ArrayList<Items>() }
    private val foodServingList by lazy {  ArrayList<ServingSize>() }


    override fun getViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)
        getData()
    }

    private fun getData() {
        intent.let {
            val mealInput =  it.getParcelableExtra<MealInput>(AddMealContracts.data)
            iv_image.loadUserImageFromUrl(this, mealInput!!.image)

            if (mealInput.invalid) {
                showCustomAlertDialog(
                    this,
                    this,
                    getString(R.string.this_does_not_look_like_photo),
                    true,
                    positiveButtonText = getString(R.string.retake),
                    negativeButtonText = getString(R.string.add_manually),
                    title = getString(R.string.invalid_photo)
                )
            }
            else {
                println("::::: ImageView")

                data = mealInput.data
                showBottomSheetDialog()
            }
        }
    }

    override fun onOkClick(dialog: DialogInterface?) {
        dialog?.dismiss()
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onCancelClick(dialog: DialogInterface?) {
        dialog?.dismiss()
        setResult(RESULT_FIRST_USER, Intent().putExtra("isManual",true))
        finish()
    }

    private fun showBottomSheetDialog() {
         bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.apply {
            setContentView(R.layout.bottomsheet_choose_item)
            val height = Resources.getSystem().displayMetrics.heightPixels

            val heightToSet = (height/2)
            setupFullHeight(bottomSheetDialog)


            val layoutManager = LinearLayoutManager(
                bottomSheetDialog.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )

            if (data?.results != null) {
                foodNameList.clear()
                foodNameList.addAll(data?.results!!)
                foodNameList[0].isSelected=true
                foodNameAdapter = FoodItemNameAdapter(foodNameList,
                    object : Listeners.ItemClickListener {
                        override fun onSelect(o: Any?, o1: Any?) {
                            val pos = o as Int
                            updateSelectedFoodName(pos)
                            foodTypeList.clear()
                            foodTypeList.addAll(foodNameList[pos].items!!)
                            foodTypeAdapter.notifyDataSetChanged()
                        }

                        override fun onUnSelect(o: Any?, o1: Any?) {}
                    })

                rv_item_names.layoutManager = layoutManager
                rv_item_names.adapter = foodNameAdapter
                rv_item_names.scrollToPosition(0)

                foodTypeList.clear()
                foodTypeList.addAll(data?.results!![0].items!!)

                foodTypeAdapter = FoodItemAdapter(
                    foodTypeList,
                    object : Listeners.ItemClickListener {
                        override fun onSelect(o: Any?, o1: Any?) {
                            val pos = o as Int
                            selectedItem = foodTypeList[pos]
                            foodTypeList[pos].servingSizes?.let {
                                foodServingList.clear()
                                foodServingList.addAll(it)
                                if (foodServingList.size > 0) {
                                    showFoodServingDialog()
                                } else {
                                    showToast(MessageConstants.Messages.no_serving_available)
                                }
                            }
                        }

                        override fun onUnSelect(o: Any?, o1: Any?) {}
                    })
                rv_item_types.adapter = foodTypeAdapter
                rv_item_types.scrollToPosition(0)

                tvAddManual.setOnClickListener {
                    setResult(RESULT_FIRST_USER, Intent().putExtra("isManual",true))
                    finish()
                }

                show()

            }
        }
    }

    private fun updateSelectedFoodName(pos: Int) {
        if(foodNameList.size>0){
            for((i, data) in foodNameList.withIndex()){
                if(i !=pos){
                    data.isSelected = false
                }
            }
            foodNameList[pos].isSelected = true
        }
        foodNameAdapter.notifyDataSetChanged()
    }

    private fun updateSelectedServing(pos: Int) {
        if(foodServingList.size>0){
            for((i, data) in foodServingList.withIndex()){
                if(i !=pos){
                    data.isServingSelected = false
                }
            }
            foodServingList[pos].isServingSelected = true
        }
        servingAdapter.notifyDataSetChanged()
    }

    private fun setupFullHeight(bottomSheet: BottomSheetDialog, heightToSet: Int = -1) {

        val height = Resources.getSystem().displayMetrics.heightPixels
        val parentLayout =
            bottomSheet.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        parentLayout?.let { it ->
            val behaviour = BottomSheetBehavior.from(it)
            val layoutParams = it.layoutParams
            if(heightToSet!=-1) {
                layoutParams.height = heightToSet
            }else{
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            it.layoutParams = layoutParams
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    fun showFoodServingDialog() {
        dialog = Dialog(this, R.style.MyCustomTheme)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_food_serving, null)
        dialog?.apply {
            setContentView(view)
            setCanceledOnTouchOutside(false)

            val lp = dialog!!.window!!.attributes
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.gravity = Gravity.CENTER
            lp.dimAmount = 0.5f
            window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            lp.windowAnimations = R.style.DialogAnimation
            window?.attributes = lp

            val btnPositive: Button = findViewById(R.id.btnPositive)
            val btnNegative: Button = findViewById(R.id.btnNegative)
            val etNoOfServingCount: TextView = findViewById(R.id.etNoOfServingCount)
            val rv_serving: RecyclerView = findViewById(R.id.rv_serving)

            selectedItem?.selectedServingSize = foodServingList[0]
            foodServingList[0].isServingSelected = true
            val staggeredGridLayoutManager = StaggeredGridLayoutManager(
                2,
                LinearLayoutManager.VERTICAL
            )
            servingAdapter = FoodServingAdapter(
                foodServingList,
                object : Listeners.ItemClickListener {
                    override fun onSelect(o: Any?, o1: Any?) {
                        val pos = o as Int
                        selectedItem?.selectedServingSize = foodServingList[pos]

                        updateSelectedServing(pos)
                    }

                    override fun onUnSelect(o: Any?, o1: Any?) {}

                })
            rv_serving.layoutManager= staggeredGridLayoutManager
            rv_serving.adapter = servingAdapter

            btnPositive.setOnClickListener {
                if(selectedItem!=null && selectedItem!!.selectedServingSize!=null) {
                    enteredNoOfServing = etNoOfServingCount.text.toString()
                    selectedItem!!.noOfServing = enteredNoOfServing
                    showBottomSheetConfirmationDialog()
                }else{
                    showToast(MessageConstants.Errors.invalid_data)
                }
                dialog?.dismiss()
            }

            btnNegative.setOnClickListener {
                dialog?.dismiss()
            }

            show()
        }
    }

    private fun showBottomSheetConfirmationDialog() {
         bottomSheetConfirmationDialog = BottomSheetDialog(this)
        bottomSheetConfirmationDialog.apply {
            setContentView(R.layout.bottomsheet_final_selected_meal)
            setupFullHeight(bottomSheetConfirmationDialog)
            show()

            selectedItem?.let{ it ->
                tvSelectedName.text = selectedItem!!.name
                it.selectedServingSize?.let{
                    tvSelectedServing.text = it.unit
                }

                tv_looks_good.setOnClickListener {
                    setResult(RESULT_OK, Intent()
                        .putExtra(AddMealContracts.data, selectedItem)
                        .putExtra(AppConstants.NO_OF_SERVING, enteredNoOfServing)
                    )
                    finish()
                }
            }
        }
    }

}