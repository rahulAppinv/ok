package com.app.okra.ui.profile

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseFragmentWithoutNav
import com.app.okra.base.BaseViewModel
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.data.repo.ProfileRepoImpl
import com.app.okra.extension.loadUserImageFromUrl
import com.app.okra.extension.navigate
import com.app.okra.extension.navigationOnly
import com.app.okra.extension.viewModelFactory
import com.app.okra.models.ItemModel
import com.app.okra.ui.boarding.resetPassword.ResetOrChangePasswordActivity
import com.app.okra.ui.connected_devices.BluetoothActivity
import com.app.okra.ui.profile.profile_details.ProfileInfoActivity
import com.app.okra.ui.profile.profile_details.ProfileViewModel
import com.app.okra.ui.my_account.setting.SettingsActivity
import com.app.okra.ui.my_account.support_request.SupportRequestActivity
import com.app.okra.ui.my_reminder.MyReminderActivity
import com.app.okra.ui.tutorial.AppTutorialActivity
import com.app.okra.utils.*
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_profile.*
import java.io.File

class ProfileFragment : BaseFragmentWithoutNav(), Listeners.ItemClickListener,
    View.OnClickListener,
    Listeners.CustomMediaDialogListener,
    PermissionUtils.IGetPermissionListener,
    ImageUtils.IChooseImageInterface{

    companion object{
        var isProfilePicUpdated = false
    }

    private lateinit var amazonImageUrl: String
    private lateinit var profileAdapter: ItemsAdapter
    private var name: String? = null
    private var profilePic: String? = null
    private val mPermissionUtils: PermissionUtils = PermissionUtils(this)
    private val mChooseImageUtils: ImageUtils = ImageUtils()
    private var typeOfAction: Int = 0

    private val viewModel by lazy {
        ViewModelProvider(this,
            viewModelFactory {
                ProfileViewModel(ProfileRepoImpl(apiServiceAuth))
            }
        ).get(ProfileViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        if(isProfilePicUpdated){
            profilePic = PreferenceManager.getString(AppConstants.Pref_Key.PROFILE_PIC)
            iv_profile_pic.loadUserImageFromUrl(requireContext(), profilePic)
        }
    }


    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manageUi()
        setAdapter()
        setLiveDataObserver()
        setListener()
    }

    private fun getData() {
        name = PreferenceManager.getString(AppConstants.Pref_Key.NAME)
        profilePic = PreferenceManager.getString(AppConstants.Pref_Key.PROFILE_PIC)
    }

    private fun setListener() {
        tvViewProfile.setOnClickListener(this)
        iv_upload.setOnClickListener(this)
        mChooseImageUtils.setCallbacks(requireActivity(), this, this)
        viewModel.setAmazonCallback(requireActivity())
    }

    private fun setLiveDataObserver() {
        setBaseObservers(viewModel, this)


        viewModel._amazonStatusLiveData.observe(viewLifecycleOwner) {
            if(it.serverUrl.isNotEmpty()){
                amazonImageUrl =  it.serverUrl
                viewModel.setProfileRequest(profilePic = it.serverUrl)
                viewModel.updateProfileInfo()
            }else{
                showToast(MessageConstants.Errors.an_error_occurred)
            }
        }

        viewModel._updateProfileLiveData.observe(viewLifecycleOwner) {
            showToast(it.message!!)
            BaseActivity.saveDataInPreference(profilePicture = amazonImageUrl)
            iv_profile_pic.loadUserImageFromUrl(requireActivity(), amazonImageUrl)
        }
    }

    private fun manageUi() {
        tvName.text = name
        iv_profile_pic.loadUserImageFromUrl(requireActivity(), profilePic)
        if(!profilePic.isNullOrEmpty()) {
            iv_profile_pic.layoutParams.width = 400
            iv_profile_pic.layoutParams.height = 400
        }
    }

    private fun setAdapter() {
        profileAdapter = ItemsAdapter(
            this,
            requireActivity(),
            ProfileFragment::class.java.simpleName
        )
        rvOptions.layoutManager = LinearLayoutManager(requireContext())
        rvOptions.adapter = profileAdapter
        //  rvOptions.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

    }

    override fun onSelect(o: Any?, o1: Any?) {
        val pos = o as Int
        val type = o1 as ItemModel

        when (pos + 1) {
            1 -> {
                requireActivity().navigationOnly(ProfileInfoActivity())
            }
            2 -> {
                val intent = Intent(requireActivity(), StaticContentActivity::class.java)
                intent.putExtra(AppConstants.Intent_Constant.TYPE, StaticContentActivity.OKRA_WEB_URL)

                requireActivity().navigate(intent)
            }
            3 -> {
                val intent = Intent(requireActivity(), BluetoothActivity::class.java)
                intent.putExtra(AppConstants.Intent_Constant.FROM_SCREEN,
                    ProfileFragment::class.java.simpleName
                )
                requireActivity().navigate(intent)
            }
            4 -> {
                requireActivity().navigationOnly(SettingsActivity())
            }
            5 -> {
                requireActivity().navigationOnly(AppTutorialActivity())
            }
            6 -> {
                requireActivity().navigate(
                    Intent(requireContext(), ResetOrChangePasswordActivity::class.java)
                        .putExtra(AppConstants.SCREEN_TYPE, ProfileFragment::class.java.simpleName)
                )
            }
            7 -> {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/")))
                } catch (e: ActivityNotFoundException) {
                    println("Exception:"+e.printStackTrace())
                }
            }
            8 -> {
                requireActivity().navigationOnly(MyReminderActivity())
            }
            9 -> {
                requireActivity().navigationOnly(SupportRequestActivity())
            }
        }
    }

    override fun onUnSelect(o: Any?, o1: Any?) {}

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.tvViewProfile -> {
                requireActivity().navigationOnly(ProfileInfoActivity())
            }
            R.id.iv_upload -> {
                showOptionDialog(requireContext(), this, false)
            }
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
        typeOfAction =AppConstants.PermissionCodes.PERMISSION_CAMERA
        if(mPermissionUtils.checkAndGetStorageAndCameraPermissions(requireContext())){
            onPermissionsGiven(AppConstants.PermissionCodes.PERMISSION_CAMERA)
        }
    }

    private fun checkStoragePermissions() {
        typeOfAction =AppConstants.PermissionCodes.PERMISSION_STORAGE
        if(mPermissionUtils.checkAndGetStoragePermissions(requireContext())) {
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
        if(resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result: CropImage.ActivityResult? = CropImage.getActivityResult(data)

                    if (result != null && result.uri != null) {

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
            .start(requireActivity(), this)

    }
}