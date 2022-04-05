package com.app.okra.ui.add_medication

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.app.okra.R
import com.app.okra.base.BaseFragment
import com.app.okra.base.BaseViewModel
import com.app.okra.data.repo.MedicationRepoImpl
import com.app.okra.extension.*
import com.app.okra.models.MedicationData
import com.app.okra.ui.logbook.medication.MedicationViewModel
import com.app.okra.utils.*
import com.app.okra.utils.AppConstants.RequestOrResultCodes.REQUEST_PICK_IMAGE_FROM_GALLERY
import com.app.okra.utils.EventLiveData.editMedicationLiveData
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_edit_medication.*
import kotlinx.android.synthetic.main.layout_header.*
import java.io.File

class EditMedicationFragment : BaseFragment(),
        View.OnClickListener,
        Listeners.CustomMediaDialogListener,
        PermissionUtils.IGetPermissionListener,
        ImageUtils.IChooseImageInterface, Listeners.ItemClickListener {

    private val THRESHOLD_IMAGE_LIMIT=10
    private var medicationType: String? = null
    private var medicationData: MedicationData? = null
    private lateinit var mAdapter: ImageAdapter
    private lateinit var localImageUri: Uri
    private val mPermissionUtils: PermissionUtils = PermissionUtils(this)
    private val mChooseImageUtils: ImageUtils = ImageUtils()
    private var typeOfAction: Int = 0
    private val imageList = ArrayList<String>()


    override fun getViewModel(): BaseViewModel? {
        return null
    }

    private val viewModel by lazy {
        ViewModelProvider(this,
                viewModelFactory {
                    MedicationViewModel(MedicationRepoImpl(apiServiceAuth))
                }
        ).get(MedicationViewModel::class.java)
    }

    private val activityGalleryResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result != null) {
                    mChooseImageUtils.setImageResult(
                            REQUEST_PICK_IMAGE_FROM_GALLERY,
                            Activity.RESULT_OK,
                            result.data
                    )
                }
            }


    private val activityCameraResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it != null) {
                    mChooseImageUtils.getCameraImageResult(it.data)
                }
            }


    private var tvUnitValue: TextView? = null
    private lateinit var tvQuantityValue: TextView

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_medication, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
        setAdapter()
        getData()
        setView(view)
        setListener()
    }

    private fun setView(view: View) {
        tvUnitValue = view.findViewById(R.id.tvUnitData)
        tvQuantityValue = view.findViewById(R.id.tvQuantityData)
        btnSave.beVisible()
        etName.isEnabled = false

        medicationData?.let {
            if (!it.medicineName.isNullOrEmpty()) {
                etName.setText(it.medicineName)
            }

            if (!it.unit.isNullOrEmpty()) {
                val unit: String = when {
                    it.unit.equals(AppConstants.MG) -> {
                        tvUnitData.isEnabled = true
                        getString(R.string.mg)
                    }
                    it.unit.equals(AppConstants.PILLES) -> {
                        tvUnitData.isEnabled = true
                        getString(R.string.pills)
                    }
                    else -> {
                        tvUnitData.isEnabled = false
                        getString(R.string.ml)

                    }
                }
                tvUnitData.text = unit
            }

            if (it.quantity != null) {
                tvQuantityValue.text = it.quantity.toString()
            }

            if (!it.image.isNullOrEmpty()) {
                imageList.clear()
                imageList.addAll(it.image!!)
                imageList.reverse()
                mAdapter.notifyDataSetChanged()
            }

            if (!it.tags.isNullOrEmpty()) {
                etTags.setText(it.tags.toString())
            }

            if (!it.feelings.isNullOrEmpty()) {
                etHowToFeel.setText(it.feelings.toString())

            }


            /*  if(!it.createdAt.isNullOrEmpty()) {
                  tvDateValue.text = getDifferentInfoFromDate_String(
                      it.createdAt!!,
                      initFormat = "dd/MM/yyyy",
                      formatYouWant = "MMM dd yyyy")
              }else{
                  val cal = Calendar.getInstance()
                  tvDateValue.text =
                      getDateFromTimeStamp(
                          cal.timeInMillis,
                          formatYouWant = AppConstants.DateFormat.DATE_FORMAT_4
                      )
              }
              */
        }
    }

    private fun getData() {
        arguments?.let { it ->
            if (it.containsKey(AppConstants.DATA)) {
                medicationData = it.getParcelable(AppConstants.DATA)
            }
            if (it.containsKey(AppConstants.MEDICATION_TYPE)) {
                medicationType = it.getString(AppConstants.MEDICATION_TYPE)
            }
        }
    }

    private fun setAdapter() {
        mAdapter = ImageAdapter(requireContext(), imageList, this,
                EditMedicationFragment::class.java.simpleName
        )
        rvMealImages.adapter = mAdapter
    }

    private fun setListener() {
        mChooseImageUtils.setCallbacks(requireActivity(), this, this)
        viewModel.setAmazonCallback(requireActivity())

        ivBack.setOnClickListener(this)

        cvAdd.setOnClickListener(this)

        btnSave.setOnClickListener(this)
        tvUnitData.setOnClickListener(this)
        tvQuantityData.setOnClickListener(this)
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
        if (mPermissionUtils.checkAndGetStorageAndCameraPermissions(requireContext())) {
            onPermissionsGiven(AppConstants.PermissionCodes.PERMISSION_CAMERA)
        }
    }

    private fun checkStoragePermissions() {
        typeOfAction = AppConstants.PermissionCodes.PERMISSION_STORAGE
        if (mPermissionUtils.checkAndGetStoragePermissions(requireContext())) {
            onPermissionsGiven(AppConstants.PermissionCodes.PERMISSION_STORAGE)
        }
    }

    override fun onPermissionsGiven(data: Int) {
        when (data) {
            AppConstants.PermissionCodes.PERMISSION_STORAGE -> {
                val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                            setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                        }
                activityGalleryResult.launch(intent)
            }
            AppConstants.PermissionCodes.PERMISSION_CAMERA -> {
                val cameraIntent = mChooseImageUtils.openCamera(false)
                activityCameraResult.launch(cameraIntent)
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
        if (resultCode == AppCompatActivity.RESULT_OK) {
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
                .setRequestedSize(50, 50)
                .start(requireActivity())

    }

    private fun setObserver() {
        setBaseObservers(viewModel, this)

        viewModel._amazonStatusLiveData.observe(viewLifecycleOwner) {
            if (it.serverUrl.isNotEmpty()) {
                imageList.add(it.serverUrl)
                imageList.reverse()
                mAdapter.notifyDataSetChanged()

                manageImageOptionVisibility()

                rvMealImages.scrollToPosition(0)
            } else {
                showToast(MessageConstants.Errors.an_error_occurred)
            }
        }
        viewModel._updateMedicationLiveData.observe(viewLifecycleOwner) {
            editMedicationLiveData.value= Event(medicationData!!)
            val intent = Intent()
            intent.putExtra(AppConstants.Intent_Constant.RELOAD_SCREEN, "true")
            requireActivity().setResult(AppCompatActivity.RESULT_OK, intent)

            showToast(MessageConstants.Messages.medication_updated_successfully)
            navController.popBackStack()
        }
    }

    private fun manageImageOptionVisibility() {
        if(imageList.size==THRESHOLD_IMAGE_LIMIT){
            cvAdd.beGone()
        }else{
            cvAdd.beVisible()
        }
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.tvQuantityData -> {
                showUnitDialog(tvUnitData.text.toString(), tvQuantityValue.text.toString())
            }
            R.id.tvUnitData -> {
                showUnitDialog(tvUnitData.text.toString(), tvQuantityValue.text.toString())
            }
            R.id.cvAdd -> {
                if (imageList.size < THRESHOLD_IMAGE_LIMIT) {
                    showOptionDialog(requireContext(), this, false)
                } else {
                    showToast(msg = MessageConstants.Messages.you_cant_add_more)
                }

            }
            R.id.btnSave -> {
                if (medicationData != null) {
                    val unitToSend = when {
                        tvUnitData.text.toString() == getString(R.string.mg) -> AppConstants.MG
                        tvUnitData.text.toString() == getString(R.string.pills) -> AppConstants.PILLES
                        else -> AppConstants.ML
                    }

                    medicationData!!.unit = unitToSend
                    medicationData!!.quantity = Integer.parseInt(tvQuantityValue.text.toString())
                    medicationData!!.tags = etTags.text.toString()
                    medicationData!!.feelings = etHowToFeel.text.toString()
                    medicationData!!.image = imageList


                    viewModel.prepareUpdateRequest(medicationData!!)
                    viewModel.updateMedication()
                }

            }
            R.id.ivBack -> {
                navController.popBackStack()
            }
        }
    }

    override fun onSelect(o: Any?, o1: Any?) {
        val pos = o as Int
        imageList.remove(imageList[pos])
        mAdapter.notifyDataSetChanged()

        manageImageOptionVisibility()
    }

    override fun onUnSelect(o: Any?, o1: Any?) {}


    private fun showUnitDialog(unit: String, quantity: String) {
        dialog = activity?.let { Dialog(it, R.style.MyCustomTheme) }
        val view: View = LayoutInflater.from(activity).inflate(R.layout.dialog_medicine_unit, null)
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

            val btnAdd: Button = findViewById(R.id.btnAdd)
            val tvTitle: TextView = findViewById(R.id.tvTitle)
            val etUnit: EditText = findViewById(R.id.etUnit)
            val tvMG: TextView = findViewById(R.id.tvMG)
            val tvPill: TextView = findViewById(R.id.tvPill)
            val tvML: TextView = findViewById(R.id.tvML)
            var isPill = false
            var isML = false
            var isMG = false


            etUnit.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if(!p0.isNullOrEmpty()){
                        btnAdd.beEnable()
                    }else{
                        btnAdd.beDisable()
                    }
                }

                override fun afterTextChanged(p0: Editable?) { }
            })

            medicationData?.let {
                tvTitle.text = it.medicineName

                when (unit) {
                    getString(R.string.mg) -> {
                        tvMG.background =
                                ResourcesCompat.getDrawable(resources, R.drawable.bg_button_green, null)
                        tvMG.setTextColor(ContextCompat.getColor(context, R.color.white))
                        tvPill.background = null
                        tvPill.setTextColor(ContextCompat.getColor(context, R.color.grey_3))
                        tvML.background = null
                        tvML.setTextColor(ContextCompat.getColor(context, R.color.grey_3))
                        isMG = true
                        isPill = false
                        isML = false
                        etUnit.setMaxLength(4)

                    }
                    getString(R.string.pills) -> {
                        tvPill.background =
                                ResourcesCompat.getDrawable(resources, R.drawable.bg_button_green, null)
                        tvPill.setTextColor(ContextCompat.getColor(context, R.color.white))
                        tvMG.background = null
                        tvMG.setTextColor(ContextCompat.getColor(context, R.color.grey_3))
                        tvML.background = null
                        tvML.setTextColor(ContextCompat.getColor(context, R.color.grey_3))
                        isPill = true
                        isMG = false
                        isML = false
                        etUnit.setMaxLength(2)
                    }
                    else -> {
                        tvML.background =
                                ResourcesCompat.getDrawable(resources, R.drawable.bg_button_green, null)
                        tvML.setTextColor(ContextCompat.getColor(context, R.color.white))
                        tvMG.background = null
                        tvMG.setTextColor(ContextCompat.getColor(context, R.color.grey_3))
                        tvPill.background = null
                        tvPill.setTextColor(ContextCompat.getColor(context, R.color.grey_3))
                        isMG = false
                        isML = true
                        isPill = false
                        etUnit.setMaxLength(3)
                    }
                }
                etUnit.setText(quantity)
            }

            tvMG.setOnClickListener {
                if(!isML) {
                    isPill = false
                    etUnit.setMaxLength(4)
                    tvMG.background =
                            ResourcesCompat.getDrawable(resources, R.drawable.bg_button_green, null)
                    tvMG.setTextColor(ContextCompat.getColor(context, R.color.white))
                    tvPill.background = null
                    tvPill.setTextColor(ContextCompat.getColor(context, R.color.grey_3))
                    tvML.background = null
                    tvML.setTextColor(ContextCompat.getColor(context, R.color.grey_3))
                    etUnit.setText("")
                }
                else{
                    showAlert()
                }
            }

            tvPill.setOnClickListener {
                if (!isML) {
                    isPill = true
                    etUnit.setMaxLength(2)
                    tvPill.background =
                            ResourcesCompat.getDrawable(resources, R.drawable.bg_button_green, null)
                    tvPill.setTextColor(ContextCompat.getColor(context, R.color.white))
                    tvMG.background = null
                    tvMG.setTextColor(ContextCompat.getColor(context, R.color.grey_3))
                    tvML.background = null
                    tvML.setTextColor(ContextCompat.getColor(context, R.color.grey_3))
                    etUnit.setText("")
                }else{
                    showAlert()
                }
            }

            tvML.setOnClickListener {
                if (!isMG && !isPill) {
                    isPill = false
                    etUnit.setMaxLength(3)
                    tvML.background =
                            ResourcesCompat.getDrawable(resources, R.drawable.bg_button_green, null)
                    tvML.setTextColor(ContextCompat.getColor(context, R.color.white))
                    tvMG.background = null
                    tvMG.setTextColor(ContextCompat.getColor(context, R.color.grey_3))
                    tvPill.background = null
                    tvPill.setTextColor(ContextCompat.getColor(context, R.color.grey_3))
                    etUnit.setText("")
                }else{
                    showAlert()

                }
            }

            btnAdd.setOnClickListener {
                if (etUnit.text.isNullOrEmpty()) {
                    showToast(getString(R.string.please_enter_medicine_unit))
                } else {
                    if (isPill) {
                        when {
                            etUnit.text.toString().toInt() < 1 -> {
                                showToast("Enter atleast 1 pill")
                            }
                            etUnit.text.toString().toInt() > 10 -> {
                                showToast("You can not exceed 10 pills")
                            }
                            else -> {
                                tvUnitValue?.text = getString(R.string.pills)
                                tvQuantityValue.setText(etUnit.text.toString())
                                dialog?.dismiss()
                            }
                        }
                    }
                    else if(isML){
                        if (etUnit.text.toString().isEmpty()) {
                            showToast("Enter no of ml.")
                        } else {
                            tvUnitValue?.text = getString(R.string.ml)
                            tvQuantityValue.setText(etUnit.text.toString())
                            dialog?.dismiss()
                        }
                    }
                    else {
                        if (etUnit.text.toString().isEmpty()) {
                            showToast("Enter no of mg.")
                        } else {
                            tvUnitValue?.text = getString(R.string.mg)
                            tvQuantityValue.setText(etUnit.text.toString())
                            dialog?.dismiss()
                        }
                    }
                }
            }
            show()
        }
    }

    private fun showAlert() {
        showAlertDialog(
                requireContext(),
                listener = object :Listeners.DialogListener{
                    override fun onOkClick(dialog: DialogInterface?) {
                        dialog?.dismiss()
                    }

                    override fun onCancelClick(dialog: DialogInterface?) {
                        dialog?.dismiss()
                    }
                },
                MessageConstants.Messages.message_unit_interchange,
                false
        )
    }
}