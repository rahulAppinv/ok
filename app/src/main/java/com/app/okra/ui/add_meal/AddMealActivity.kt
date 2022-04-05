package com.app.okra.ui.add_meal

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.data.repo.AddMealRepoImpl
import com.app.okra.extension.beVisible
import com.app.okra.extension.loadUserImageFromUrl
import com.app.okra.extension.viewModelFactory
import com.app.okra.models.CommonData
import com.app.okra.models.FoodItemsRequest
import com.app.okra.models.Items
import com.app.okra.ui.add_meal.contract.AddMealContracts
import com.app.okra.utils.*
import com.app.okra.utils.AppConstants.DateFormat.DATE_FORMAT_1
import com.app.okra.utils.AppConstants.DateFormat.DATE_FORMAT_6
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_add_meal.*
import kotlinx.android.synthetic.main.activity_add_meal.cv_image
import kotlinx.android.synthetic.main.activity_add_meal.tvDate
import kotlinx.android.synthetic.main.layout_header.*
import kotlinx.android.synthetic.main.layout_header.tvTitle
import java.io.File
import java.util.*

class AddMealActivity : BaseActivity(), Listeners.CustomMediaDialogListener,
    PermissionUtils.IGetPermissionListener,
    ImageUtils.IChooseImageInterface {

    private var amazonImageUrl: String = ""
    private lateinit var localImageUri: Uri
    private val mPermissionUtils: PermissionUtils = PermissionUtils(this)
    private val mChooseImageUtils: ImageUtils = ImageUtils()
    private var typeOfAction: Int = 0
    private var mYear: Int = 0
    private var mMonth: Int = 0
    private var mDay: Int = 0
    private var mHour: Int = 0
    private var mMin: Int = 0
    private  lateinit var selectedFoodItem : Items
    private var isManual = false
    private var strDate : String = ""

    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    private val viewModel by lazy {
        ViewModelProvider(this,
            viewModelFactory {
                AddMealViewModel(AddMealRepoImpl(apiServiceCalorieMama,apiServiceAuth))
            }
        ).get(AddMealViewModel::class.java)
    }

    private val activityForResult = registerForActivityResult(AddMealContracts()){ result ->
        if(result!=null){
            selectedFoodItem= result
            tvFoodTypeValue.text = result.name

            result.noOfServing?.let {
                etNoOfServing.setText(it)
            }
            result.nutrition?.let {
                if(it.calories!=null) {
                    tvCalories.setText("${it.calories}")
                }
                if(it.totalCarbs!=null) {
                    tvCarbs.setText("${it.totalCarbs}")
                }

                if(it.totalFat!=null) {
                    tvFat.setText("${it.totalFat}")
                }

                if(it.protein!=null) {
                    tvProtein.setText("${it.protein}")
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_meal)
        setUpToolbar()
        setView()
        setObserver()
        setListener()
    }

    private fun setView() {
        tvDate.text = getCurrentDateInString(DATE_FORMAT_1)
        strDate = getCurrentDateInString(DATE_FORMAT_6)
    }

    private fun setUpToolbar() {
        tvTitle.text = getString(R.string.new_meal)
        btnSave.beVisible()
        btnSave.text = getString(R.string.btn_add_meal)
    }

    private fun setListener() {
        mChooseImageUtils.setCallbacks(this, this)
        viewModel.setAmazonCallback(this)

        ivBack.setOnClickListener {
            checkDataExistence()
        }

        cv_image.setOnClickListener {
            showOptionDialog(this, this, false)
        }
        tvUploadImage.setOnClickListener {
            showOptionDialog(this, this, false)
        }

        btnSave.setOnClickListener {
            when {
                TextUtils.isEmpty(amazonImageUrl) -> {
                    showToast(MessageConstants.Errors.please_select_image)
                }
                TextUtils.isEmpty(etFoodTypeValue.text.toString()) && isManual -> {
                    showToast(MessageConstants.Errors.please_select_food_type)
                }
                TextUtils.isEmpty(tvDate.text.toString()) -> {
                    showToast(MessageConstants.Errors.please_select_date)
                }
                TextUtils.isEmpty(tvCalories.text.toString()) -> {
                    showToast(MessageConstants.Errors.please_select_calories)
                }
                TextUtils.isEmpty(tvCarbs.text.toString()) -> {
                    showToast(MessageConstants.Errors.please_select_carbs)
                }
                TextUtils.isEmpty(tvFat.text.toString()) -> {
                    showToast(MessageConstants.Errors.please_select_fat)
                }
                TextUtils.isEmpty(tvProtein.text.toString()) -> {
                    showToast(MessageConstants.Errors.please_select_protein)
                }
                else -> {
                    val foodList: ArrayList<FoodItemsRequest> = ArrayList()

                    val foodType = if(isManual)
                        etFoodTypeValue.text.toString()
                    else
                        tvFoodTypeValue.text.toString()

                    val dateToConvert =  getISOFromDate(strDate, DATE_FORMAT_6)
                    viewModel.prepareAddRequest(
                        date = dateToConvert,
                        image = amazonImageUrl,
                        foodItems = foodList,
                        foodType = foodType,
                        calories = CommonData(tvCalories.text.toString(), "cal"),
                        carbs = CommonData(tvCarbs.text.toString(), "gm"),
                        fat = CommonData(tvFat.text.toString(), "gm"),
                        protein = CommonData(tvProtein.text.toString(), "gm"),
                        noOfServings = etNoOfServing.text.toString()
                    )
                    viewModel.addMeal()
                }
            }
        }

        tvDate.setOnClickListener {
            selectDate(tvDate)
        }

        tvFoodTypeValue.setOnClickListener {
            if(isManual){
                tvFoodTypeValue.visibility = View.INVISIBLE
                etFoodTypeValue.visibility = View.VISIBLE
                etFoodTypeValue.requestFocus()
            }
        }
    }

    private fun checkDataExistence() {
        if(this::localImageUri.isInitialized){
            showCustomAlertDialog(
                this,
                object : Listeners.DialogListener{
                    override fun onOkClick(dialog: DialogInterface?) {
                        finish()
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
            finish()
        }

    }

    override fun onImageClick(dialog: DialogInterface?) {
        checkCameraPermissions()
        dialog?.dismiss()
    }

    override fun onUploadFromGallery(dialog: DialogInterface?) {
        checkStoragePermissions()
        dialog?.dismiss()
    }

    override fun onCancelOrUploadFromEmail(dialog: DialogInterface?) {
        dialog?.dismiss()
    }

    private fun checkCameraPermissions() {
        typeOfAction = AppConstants.PermissionCodes.PERMISSION_CAMERA
        if (mPermissionUtils.checkAndGetStorageAndCameraPermissions(this)) {
            onPermissionsGiven(AppConstants.PermissionCodes.PERMISSION_CAMERA)
        }
    }

    private fun checkStoragePermissions() {
        typeOfAction = AppConstants.PermissionCodes.PERMISSION_STORAGE
        if (mPermissionUtils.checkAndGetStoragePermissions(this)) {
            onPermissionsGiven(AppConstants.PermissionCodes.PERMISSION_STORAGE)
        }

    }

    override fun onPermissionsGiven(data: Int) {
        when (data) {
            AppConstants.PermissionCodes.PERMISSION_STORAGE -> {
                mChooseImageUtils.openGallery()
            }
            AppConstants.PermissionCodes.PERMISSION_CAMERA -> {
                mChooseImageUtils.openCamera()
            }
        }
    }

    override fun onPermissionsDeny(requestCode: Int) {
        when (requestCode) {
            AppConstants.PermissionCodes.PERMISSION_STORAGE -> {
                showToast(MessageConstants.Messages.msg_storage_permission)
            }
            AppConstants.PermissionCodes.PERMISSION_CAMERA -> {
                showToast(MessageConstants.Messages.msg_camera_permission)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result: CropImage.ActivityResult? = CropImage.getActivityResult(data)

                    if (result != null && result.uri != null) {
                        val imageUri = result.uri!!
                        val fileSize = getFileSize(imageUri!!)

                        println(":::: File Size: $fileSize")
                        if (fileSize > -1 && fileSize <= AppConstants.ALLOWED_FILE_SIZE) {
                            localImageUri = imageUri
                            viewModel.uploadFile(imageUri)
                            //  viewModel.foodRecognition(imageUri.path)
                        } else {
                            showToast("Selected file exceeds the maximum limit of ${AppConstants.ALLOWED_FILE_SIZE} MB.")
                        }
                    }
                }
                else -> {
                    if(data?.hasExtra("isManual") == true){
                        isManual = true
                        tvFoodTypeValue.text = getString(R.string.tap_here_to_edit)
                    }else {
                        isManual = false
                        mChooseImageUtils.setImageResult(requestCode, resultCode, data)
                    }

                }
            }
        }else if (resultCode == RESULT_FIRST_USER) {
            if(data?.hasExtra("isManual") == true){
                isManual = true
                tvFoodTypeValue.text = getString(R.string.tap_here_to_edit)
            }else {
                isManual = false
                mChooseImageUtils.setImageResult(requestCode, resultCode, data)
            }
        }
    }


    override fun setImagePath(path: Uri) {
        val file = File(path.path!!)

        if (file.exists()) {
            openCropper(file.toUri())
        }
    }

    override fun setImageForGallery(path: Uri) {
        openCropper(path)
    }

    private fun openCropper(uri: Uri) {
        CropImage.activity(uri)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .setAspectRatio(1, 1)
            .setAutoZoomEnabled(false)
            .setAllowFlipping(false)
            .setBorderLineThickness(8f)
            .setGuidelines(CropImageView.Guidelines.OFF)
            .setAllowRotation(false)
            .setRequestedSize(544, 544)
            .start(this)

    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, this, observeError = false)

        viewModel._amazonStatusLiveData.observe(this) {
            if(it.serverUrl.isNotEmpty()){
                amazonImageUrl =  it.serverUrl

                if(this::localImageUri.isInitialized) {
                    viewModel.foodRecognition(localImageUri.path)
                }
            }else{
                showToast(MessageConstants.Errors.an_error_occurred)
            }
        }
        viewModel._foodRecognitionLiveData.observe(this) {
            iv_image.loadUserImageFromUrl(this, localImageUri.path)
            try {
                val mealInput  = if (!it.is_food) {

                    MealInput(invalid = true, image= localImageUri.path!!)
                } else if (it.results == null || it.results!!.size == 0) {

                    MealInput(invalid = true, image= localImageUri.path!!)
                } else {
                    MealInput(invalid = false, image= localImageUri.path!!, data = it)
                }
                activityForResult.launch(mealInput)

                println("::: Exception NO")

                /*startActivity(Intent(this, ImageViewActivity::class.java)
                    .putExtra(AddMealContracts.data,mealInput))*/

            }catch (e: Exception){
                println("::: Exception: ${e.message}")
                e.printStackTrace()
            }
        }

        viewModel._addMealLiveData.observe(this) {
            EventLiveData.eventLiveData.value =
                Event(EventLiveData.EventData(AddMealActivity::class.java.simpleName))
            finish()
        }

        viewModel._errorObserver.observe(this) {
            val data = it.getContent()!!
            showToast(data.message!!)
        }
    }

    private fun selectDate(tvDate: AppCompatTextView) {
        val c = Calendar.getInstance()
        mYear = c.get(Calendar.YEAR)
        mMonth = c.get(Calendar.MONTH)
        mDay = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(this, { view, year, monthOfYear, dayOfMonth ->
                strDate =
                    year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth.toString()
                showTimePicker(tvDate, strDate)
            }, mYear, mMonth, mDay)
        val c1 = Calendar.getInstance()
        c1.add(Calendar.MONTH, -2)
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
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
            this, { timePicker, hour, minute ->
                strDate = "$selectedDate $hour:$minute"
                tvDate.text =  getDifferentInfoFromDateInString(strDate,"yyyy-MM-dd hh:mm",DATE_FORMAT_1)
            },
            mHour,
            mMin,
            true
        )
        tpd.show()

    }

    override fun onBackPressed() {
        checkDataExistence()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(mPermissionUtils.hasPermissions(this,*permissions)){
            if(typeOfAction == AppConstants.PermissionCodes.PERMISSION_CAMERA){
                onPermissionsGiven(AppConstants.PermissionCodes.PERMISSION_CAMERA)
            }
            else if(typeOfAction == AppConstants.PermissionCodes.PERMISSION_STORAGE){
                onPermissionsGiven(AppConstants.PermissionCodes.PERMISSION_STORAGE)
            }
        }
    }
}