package com.app.okra.ui.connected_devices

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.app.okra.R
import com.app.okra.base.BaseFragment
import com.app.okra.base.BaseViewModel
import com.app.okra.bluetooth.BleManager
import com.app.okra.bluetooth.data.BleDevice
import com.app.okra.data.repo.ConnectedDevicesRepoImpl
import com.app.okra.extension.*
import com.app.okra.models.BLEDeviceListData
import com.app.okra.ui.my_account.support_request.SupportRequestActivity
import com.app.okra.utils.*
import com.app.okra.utils.bleValidater.BLEValidaterListener
import com.app.okra.utils.bleValidater.BleValidate
import com.app.okra.utils.bleValidater.GPSContract
import kotlinx.android.synthetic.main.activity_otp_verify.*
import kotlinx.android.synthetic.main.fragment_connected_devices.*
import kotlinx.android.synthetic.main.layout_header.*


class ConnectedDevicesFragment : BaseFragment(),
    View.OnClickListener,
    Listeners.ItemClickListener, BLEValidaterListener {

    private lateinit var devicesAdapter: ConnectedDevicesAdapter
    private val devicesList = ArrayList<BleDevice>()

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory {
            ConnectedDevicesViewModel(ConnectedDevicesRepoImpl(apiServiceAuth))
        }).get(ConnectedDevicesViewModel::class.java)
    }


    private val bleManager by lazy {
        // Sets up the bluetooth controller.
        BleManager.instance
    }

    private val bleValidate by lazy {
        BleValidate(requireContext(), this)
    }

    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_connected_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
        setAdapter()
        setObserver()
        setListener()
        getPreviousDevices()
    }

    private fun setViews() {
         val span = SpannableString(tvInst1.text)
        span.setSpan(StyleSpan(Typeface.BOLD), 0, 8, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        tvInst1.text = span

        val span2 = SpannableString(tvInst2.text)
        span2.setSpan(StyleSpan(Typeface.BOLD), 0, 53, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        tvInst2.text = span2
    }

    private fun getPreviousDevices() {
        viewModel.getPreviousDevices()
    }

    private fun setAdapter() {
        devicesAdapter = ConnectedDevicesAdapter(this, devicesList)
        rv_connected_devices.adapter = devicesAdapter
        rv_connected_devices.isNestedScrollingEnabled = true
    }


    override fun onDestroy() {
        super.onDestroy()
        hideProgressBar()

    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, observeToast = false)

        viewModel._connectedDevicesLiveData.observe(viewLifecycleOwner){
            it.data?.let{
                devicesList.clear()
                prepareDataForAdapter(it)
                devicesAdapter.notifyDataSetChanged()
            }
            manageViewVisibility()
        }
    }

    private fun prepareDataForAdapter(arrayList: ArrayList<BLEDeviceListData>) {
        if(!arrayList.isNullOrEmpty()){
            for(deviceData in arrayList){
                val bleDevice  = BleDevice()
                bleDevice.localName = deviceData.deviceName
                bleDevice.localDeviceId = deviceData.deviceUUID
                devicesList.add(bleDevice)
            }
        }
    }

    private fun manageViewVisibility() {
        if(devicesList.isNullOrEmpty()){
            tvConnectedDevices.beGone()
            rv_connected_devices.beGone()
        }else{
            tvConnectedDevices.beVisible()
            rv_connected_devices.beVisible()
        }
    }


    private fun setListener() {
        btnConnect.setOnClickListener(this)
        tvNeedHelp.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.btnConnect -> {
                bleValidate.checkPermissions()
            }
            R.id.tvNeedHelp -> {
                requireActivity().navigate(Intent(
                    requireContext(),
                    SupportRequestActivity::class.java
                ).apply {
                    putExtra(
                        AppConstants.SCREEN_TYPE,
                        BluetoothActivity::class.java.simpleName
                    )
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as BluetoothActivity).setHeaderButtonVisibility(false)
    }

    override fun onSelect(o: Any?, o1: Any?) {
        val pos = o as Int
        val bleDevice = o1 as BleDevice
        navController.navigate(R.id.action_connectedDevicesFragment_to_bluetoothDeviceListFragment)

    }

    override fun onUnSelect(o: Any?, o1: Any?) {}


    override fun onPermissionsGiven(data: Int) {
        //startScan()
        navController.navigate(R.id.action_connectedDevicesFragment_to_bluetoothDeviceListFragment)

    }

    override fun onPermissionsDeny(data: Int) {
        showCustomAlertDialog(
            requireContext(),
            object : Listeners.DialogListener {
                override fun onOkClick(dialog: DialogInterface?) {
                    dialog?.dismiss()
                }

                override fun onCancelClick(dialog: DialogInterface?) {
                    dialog?.dismiss()
                }
            },
            MessageConstants.Messages.location_permission_deny_text,
            false,
            positiveButtonText = getString(R.string.btn_ok),
            title = getString(R.string.alert),
        )
    }

    override fun onBluetoothDisable(msg: String) {

        showCustomAlertDialog(
            requireContext(),
            object : Listeners.DialogListener {
                override fun onOkClick(dialog: DialogInterface?) {
                    bleManager.enableBluetooth()
                    dialog ?. dismiss ()
                }

                override fun onCancelClick(dialog: DialogInterface?) {
                    dialog?.dismiss()
                }
            },
            MessageConstants.Messages.bluetooth_turn_on_permission,
            true,
            positiveButtonText = getString(R.string.allow),
            negativeButtonText = getString(R.string.btn_cancel),
            title = getString(R.string.bluetooth),
        )
    }

    override fun onLocationDisable(msg: String) {
        showCustomAlertDialog(
            requireContext(),
            object : Listeners.DialogListener {
                override fun onOkClick(dialog: DialogInterface?) {
                    activityForResult.launch(null)
                    dialog ?. dismiss ()
                }

                override fun onCancelClick(dialog: DialogInterface?) {
                    dialog?.dismiss()
                }
            },
            MessageConstants.Messages.please_turn_on_your_location,
            true,
            positiveButtonText = getString(R.string.btn_ok),
            negativeButtonText = getString(R.string.btn_cancel),
            title = getString(R.string.location),
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        navController.navigate(R.id.action_connectedDevicesFragment_to_bluetoothDeviceListFragment)
    }

    private val activityForResult = registerForActivityResult(GPSContract()){ result ->
        if(result!=null && result){
            navController.navigate(R.id.action_connectedDevicesFragment_to_bluetoothDeviceListFragment)
        }
    }
 }