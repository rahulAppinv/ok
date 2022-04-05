package com.app.okra.ui.profile.profile_details

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.data.repo.ProfileRepoImpl
import com.app.okra.extension.*
import com.app.okra.models.UserDetailResponse
import com.app.okra.ui.boarding.resetPassword.ResetOrChangePasswordViewModel
import com.app.okra.ui.profile.ProfileFragment
import com.app.okra.utils.*
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_login.*

import kotlinx.android.synthetic.main.activity_profile_info.*
import kotlinx.android.synthetic.main.activity_profile_info.etEmail
import kotlinx.android.synthetic.main.activity_profile_info.iv_profile
import kotlinx.android.synthetic.main.activity_profile_info.iv_upload
import kotlinx.android.synthetic.main.activity_profile_info.tvName
import kotlinx.android.synthetic.main.activity_reset_or_change_password.*
import kotlinx.android.synthetic.main.activity_reset_or_change_password.etPassword
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.layout_button.*
import kotlinx.android.synthetic.main.layout_header.*
import java.io.File

class ProfileInfoActivity : BaseActivity(),
        View.OnClickListener,
        TextWatcher,
PermissionUtils.IGetPermissionListener,
ImageUtils.IChooseImageInterface, Listeners.CustomMediaDialogListener {

    private  var amazonImageUrl: String?=null
    private var isEditMode: Boolean =false
    private var name: String?=null
    private var email: String?=null
    private var age: String?=null
    private var profilePic: String?=null
    private var mobile: String?=null
    private  var userData: UserDetailResponse?=null
    private val mPermissionUtils: PermissionUtils = PermissionUtils(this)
    private val mChooseImageUtils: ImageUtils = ImageUtils()
    private var typeOfAction: Int = 0

    private val userId by lazy {
        PreferenceManager.getString(AppConstants.Pref_Key.USER_ID)
    }

    private val viewModel by lazy {
        ViewModelProvider(this,
                viewModelFactory {
                    ProfileViewModel(ProfileRepoImpl(apiServiceAuth))
                }
        ).get(ProfileViewModel::class.java)
    }

    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_info)
        getPrefData()
        setViews()
        setListener()
        setObserver()
        viewModel.getProfileInfo(userId!!)
    }

    private fun setListener() {
        ivRight.setOnClickListener(this)
        ivBack.setOnClickListener(this)
        iv_upload.setOnClickListener(this)
        btnCommon.setOnClickListener(this)
        etName.addTextChangedListener(this)
        etPhone.addTextChangedListener(this)
        etAge.addTextChangedListener(this)
        mChooseImageUtils.setCallbacks(this, this)
        viewModel.setAmazonCallback(this)
    }

    private fun activateButton() {
        val status = !(etName.text.isNullOrEmpty()
                && etPhone.text.isNullOrEmpty()
                && etAge.text.isNullOrEmpty()
                )
        if(status) {
            btnCommon.beEnable()
        }else{
            btnCommon.beDisable()
        }
    }

    private fun getPrefData() {
        name = PreferenceManager.getString(AppConstants.Pref_Key.NAME)
        email = PreferenceManager.getString(AppConstants.Pref_Key.EMAIL_ID)
        age = PreferenceManager.getString(AppConstants.Pref_Key.AGE)
        mobile = PreferenceManager.getString(AppConstants.Pref_Key.MOBILE)
        profilePic = PreferenceManager.getString(AppConstants.Pref_Key.PROFILE_PIC)
    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, this, observeToast = false)
        viewModel._profileInfoLiveData.observe(this, { it ->
            it?.data?.let {
                saveDataInPreference(
                        name = it.name,
                        email = it.email,
                        userType = it.userType,
                        userId = it.userId,
                        age = it.age,
                        phone = it.mobile,
                )
            }
            getPrefData()
            setViews()
        })

        viewModel._updateProfileLiveData.observe(this, {
            if(!amazonImageUrl.isNullOrEmpty()){
                ProfileFragment.isProfilePicUpdated = true
            }
            showToast(it.message!!)
            saveDataInPreference(
                    age = etAge.text.toString(),
                    name = etName.text.toString(),
                    phone = etPhone.text.toString(),
                    profilePicture = amazonImageUrl
            )

            getPrefData()
            setViews()
        })

        viewModel._amazonStatusLiveData.observe(this) {
            if(it.serverUrl.isNotEmpty()){
                amazonImageUrl =  it.serverUrl
                iv_profile.loadUserImageFromUrl(this,amazonImageUrl)
            }else{
                showToast(MessageConstants.Errors.an_error_occurred)
            }
        }
        viewModel._toastObserver.observe(this) {
            val data = it.getContent()!!

            if (isEditMode) {
                tvErrorName.beGone()
                tvErrorPhone.beGone()
                tvErrorAge.beGone()
                etName.setNormalView(this)
                etPhone.setNormalView(this)
                etAge.setNormalView(this)
                iv_upload.beGone()

                if (!checkAndLogout(data.message)) {

                    when (data.type) {
                        ProfileViewModel.FIELD_NAME -> {
                            tvErrorName.text = data.message
                            tvErrorName.beVisible()
                            etName.setErrorView(this)
                        }
                        ProfileViewModel.FIELD_PHONE -> {
                            tvErrorPhone.text = data.message
                            tvErrorPhone.beVisible()
                            etPhone.setErrorView(this)
                        }
                        ProfileViewModel.FIELD_AGE -> {
                            tvErrorAge.text = data.message
                            tvErrorAge.beVisible()
                            etAge.setErrorView(this)
                        }
                        else -> {
                          initFields()
                        }
                    }
                }
            }
            else{
                showToast(data.message)
                checkAndLogout(data.message)
            }
        }

    }
        private fun setViews() {
            tvTitle.text = getString(R.string.title_personal_details)
            cl_profile_edit.beGone()
            cv_profile_view_only.beVisible()
            ivRight.beVisible()
            ivDelete.beGone()
            includeButton.beGone()
            btnCommon.text = getString(R.string.btn_save)
            btnCommon.beDisable()
            iv_upload.beGone()

            tvName.text = name?:""
            tvEmail.text = email
            tvAge.text = age?:""
            tvPhone.text = mobile?:""

            iv_profile.loadUserImageFromUrl(this,profilePic)

        }

        override fun onClick(view: View?) {
            when (view?.id) {
                R.id.ivBack -> {
                    if(isEditMode){
                        isEditMode=false
                        cl_profile_edit.beGone()
                        cv_profile_view_only.beVisible()
                        includeButton.beGone()
                        iv_upload.beGone()
                        ivRight.beVisible()
                        ivDelete.beGone()

                        iv_profile.loadUserImageFromUrl(this,profilePic)

                    }else {
                        finish()
                    }
                }
                R.id.ivRight -> {
                    isEditMode = true
                    cl_profile_edit.beVisible()
                    cv_profile_view_only.beGone()
                    includeButton.beVisible()
                    ivRight.beGone()
                    ivDelete.beInvisible()
                    iv_upload.beVisible()
                    setValueInFields()
                }
                R.id.iv_upload -> {
                    showOptionDialog(this, this, false)

                }
                R.id.btnCommon -> {
                    initFields()
                    viewModel.setProfileRequest(
                            name= etName.text.toString().trim(),
                            age= etAge.text.toString().trim(),
                            phoneNo= etPhone.text.toString().trim(),
                            profilePic = amazonImageUrl
                    )
                    viewModel.updateProfileInfo()
                }
            }
        }

        private fun setValueInFields() {
            etName.setText(name)
            etEmail.setText(email)
            etAge.setText(age?:"")
            etPhone.setText(mobile?:"")
            etEmail.beDisable()
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            activateButton()
        }

        override fun afterTextChanged(p0: Editable?) {}


        private fun initFields(){
            tvErrorName.beGone()
            tvErrorPhone.beGone()
            tvErrorAge.beGone()
            etName.setNormalView(this)
            etPhone.setNormalView(this)
            etAge.setNormalView(this)
        }

    private fun checkCameraPermissions() {
        typeOfAction =AppConstants.PermissionCodes.PERMISSION_CAMERA
        if(mPermissionUtils.checkAndGetStorageAndCameraPermissions(this)){
            onPermissionsGiven(AppConstants.PermissionCodes.PERMISSION_CAMERA)
        }
    }

    private fun checkStoragePermissions() {
        typeOfAction =AppConstants.PermissionCodes.PERMISSION_STORAGE
        if(mPermissionUtils.checkAndGetStoragePermissions(this)) {
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


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK) {
            when (requestCode) {
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result: CropImage.ActivityResult? = CropImage.getActivityResult(data)

                    if(result!=null && result.uri!=null) {

                        val imageUri = result.uri!!

                        val fileSize = getFileSize(imageUri!!)
                        println(":::: File Size: $fileSize")
                        if (fileSize > -1 && fileSize <= AppConstants.ALLOWED_FILE_SIZE) {
                            viewModel.uploadFile(imageUri)
                        } else {
                            showToast("Selected file exceeds the maximum limit of ${AppConstants.ALLOWED_FILE_SIZE} MB.")
                        }
                    }
                }

                else -> {
                    mChooseImageUtils.setImageResult(requestCode, resultCode, data)

                }
            }
        }
    }


    override fun setImagePath(uri: Uri) {
        val file = File(uri.path)

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
                .start(this)

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


}