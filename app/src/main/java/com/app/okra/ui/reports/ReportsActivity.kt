package com.app.okra.ui.reports

import android.app.DatePickerDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.ViewModelProvider
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.data.repo.ReportRepoImpl
import com.app.okra.extension.viewModelFactory
import com.app.okra.ui.my_account.setting.measurement.CustomSpinnerAdapter
import com.app.okra.utils.DateFormatter
import com.app.okra.utils.MessageConstants
import com.app.okra.utils.getDatePicker
import com.app.okra.utils.getMinDateForReports
import kotlinx.android.synthetic.main.activity_reports.*
import kotlinx.android.synthetic.main.layout_header.*
import java.lang.Exception
import java.util.*


class ReportsActivity : BaseActivity(),
    AdapterView.OnItemSelectedListener,
    View.OnClickListener {
    private val reportsViewModel by lazy {
        ViewModelProvider(this,
            viewModelFactory {
                ReportsViewModel(ReportRepoImpl(apiServiceAuth))
            }
        ).get(ReportsViewModel::class.java)
    }

    private lateinit var fileTypeDropDownAdapter: CustomSpinnerAdapter
    private lateinit var startDatePickerInstance: DatePickerDialog
    private lateinit var endDatePickerInstance: DatePickerDialog
    private var isFirstTime = true
    private var queueNo :Long = 0
    private var _downloadmanager: DownloadManager?=null

    override fun getViewModel(): BaseViewModel {
        return reportsViewModel
    }

    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            if(intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if(queueNo == id){
                    intent.extras?.let { it ->
                        showToast(MessageConstants.Messages.file_downloaded_successfully)

                        val downloadedFileId = it.getLong(DownloadManager.EXTRA_DOWNLOAD_ID)
                        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        val uri: Uri = downloadManager.getUriForDownloadedFile(downloadedFileId)

                        //opening it
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                        try {
                            startActivity(intent)
                        }catch (e: Exception){
                                e.message?.let{
                                    if(it.contains("No Activity found")){
                                        showToast(MessageConstants.Messages.no_app_found)
                                    }
                                }
                         //   println("::: Exception Exception: ${ e.printStackTrace()}")
                         //   println("::: Exception Message: ${e.message}")
                        }


                       /* val fileUri =  _downloadmanager?.getUriForDownloadedFile(queueNo)
                        val fileURL =  fileUri?.encodedPath*/
                        println(":::: Downloaded File Path: ${uri.path}")
                    }
                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onComplete)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)
        setUpUI()
        setObservers()
        setView()
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }



    private fun setView() {
        // startDateTV.text = getCurrentDateInString()
        // endDateTV.text = getCurrentDateInString()
        // downloadBTN.beEnable()
    }

    override fun onPostResume() {
        super.onPostResume()
        ivBack.setOnClickListener(this)
        downloadBTN.setOnClickListener(this)
        tvSpinner.setOnClickListener(this)
        startDateTV.setOnClickListener(this)
        endDateTV.setOnClickListener(this)
    }

    private fun setUpUI() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        tvTitle.text = getString(R.string.title_reports)
        fileTypeDropDownAdapter = CustomSpinnerAdapter(this, reportsViewModel.fileTypeArray)
        // Specify the layout to use when the list of choices appears
        fileTypeDropDownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        spinner.adapter = fileTypeDropDownAdapter
        spinner.onItemSelectedListener = this
    }

    private fun setObservers() {
        setBaseObservers(reportsViewModel, this, this, observeToast = false)
        reportsViewModel._reportLiveData.observe(this){ it ->
            if(it.statusCode == "200" && !it.data.isNullOrEmpty()){
                val urlString = it.data
                val uri: Uri = Uri.parse(it.data)

                _downloadmanager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                } else {
                    TODO("VERSION.SDK_INT < M")
                }

                _downloadmanager?.let {
                    val fileName: String = urlString.substring(
                        urlString.lastIndexOf('/') + 1,
                        urlString.length
                    )
                    val newFileName = System.currentTimeMillis().toString() + fileName;

                    val request: DownloadManager.Request = DownloadManager.Request(uri)
                    request.setTitle("OKRA")
                    request.setDescription("Downloading File...")
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, newFileName)
                    queueNo = it.enqueue(request)
                }

            }
            else if(it.statusCode == "400"){
                apiToGetURL()
            }
        }
        reportsViewModel.isEndDateSetMLD.observe(this, {
            reportsViewModel.areAllFieldsSet.value =
                reportsViewModel.isEndDateSetMLD.value!!
                        && reportsViewModel.isStartDateSetMLD.value!!
                        && reportsViewModel.isFileTypeSetMLD.value!!
        })

        reportsViewModel.isStartDateSetMLD.observe(this, {
            reportsViewModel.areAllFieldsSet.value =
                reportsViewModel.isEndDateSetMLD.value!!
                        && reportsViewModel.isStartDateSetMLD.value!!
                        && reportsViewModel.isFileTypeSetMLD.value!!
        })

        reportsViewModel.isFileTypeSetMLD.observe(this, {
            reportsViewModel.areAllFieldsSet.value = reportsViewModel.isEndDateSetMLD.value!!
                    && reportsViewModel.isStartDateSetMLD.value!! && reportsViewModel.isFileTypeSetMLD.value!!
        })

        reportsViewModel.areAllFieldsSet.observe(this, { allFieldsSet ->
            if (!isFirstTime) {
                downloadBTN.isEnabled = allFieldsSet
                downloadBTN.alpha = if (allFieldsSet) 1.0f
                else 0.4f
            } else {
                isFirstTime = false
            }
        })

        reportsViewModel._toastObserver.observe(this){ it1->
            it1.getContent().let {
                if (!it!!.message.startsWith("ENOENT:")) {
                    showToast(it.message)
                }
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        reportsViewModel.isFileTypeSetMLD.value = true
        reportsViewModel.selectedFileType = reportsViewModel.fileTypeArray[position]
        tvSpinner.text = reportsViewModel.selectedFileType
        tvSpinner.setTextColor(Color.BLACK)
        fileTypeHintTV.visibility = View.VISIBLE

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    private val dateSelected = { dateSelected: Long, isEndDate: Boolean ->
        if (!isEndDate) {
            reportsViewModel.startDateTimestamp.value = dateSelected
            reportsViewModel.isStartDateSetMLD.value = true
            startDateTV.text = DateFormatter.covertMiliIntoDate(dateSelected.toString(), null)
            endDateTV.text=""
        }
        else {
            reportsViewModel.endDateTimestamp.value = dateSelected
            reportsViewModel.isEndDateSetMLD.value = true
            endDateTV.text = DateFormatter.covertMiliIntoDate(dateSelected.toString(), null)
        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.ivBack -> finish()
            R.id.downloadBTN -> {
                apiToGetURL()

            }
            R.id.tvSpinner -> {
                spinner.performClick()
            }
            R.id.startDateTV -> {
                startDatePickerInstance = if (reportsViewModel.startDateTimestamp.value!! > 0) {
                    getDatePicker(
                        this,
                        dateSelected,
                        reportsViewModel.startDateTimestamp.value!!,
                        false
                    )
                } else {
                    getDatePicker(this, dateSelected, false)
                }

                startDatePickerInstance.datePicker.minDate = getMinDateForReports()

               /// startDatePickerInstance.datePicker.maxDate = Calendar.getInstance().timeInMillis
                startDatePickerInstance.show()
            }
            R.id.endDateTV -> {
                endDatePickerInstance = if (reportsViewModel.startDateTimestamp.value!! > 0) {
                    getDatePicker(
                        this,
                        dateSelected,
                        reportsViewModel.startDateTimestamp.value!!,
                        true
                    )
                } else {
                    getDatePicker(this, dateSelected, true)
                }
              //  endDatePickerInstance.datePicker.maxDate = Calendar.getInstance().timeInMillis
                endDatePickerInstance.datePicker.minDate =
                    reportsViewModel.startDateTimestamp.value!!
                endDatePickerInstance.show()
            }
        }
    }

    private fun apiToGetURL() {
        val sDate = startDateTV.text.toString()
        val eDate = endDateTV.text.toString()

        reportsViewModel.setReportRequest(sDate, eDate, tvSpinner.text.toString())
        reportsViewModel.getReportUrl()

    }
}