package com.app.okra.ui.connected_devices

import android.bluetooth.BluetoothGatt
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.app.okra.OkraApplication
import com.app.okra.R
import com.app.okra.base.BaseFragment
import com.app.okra.base.BaseViewModel
import com.app.okra.bluetooth.BleManager
import com.app.okra.bluetooth.callback.BleGattCallback
import com.app.okra.bluetooth.callback.BleScanCallback
import com.app.okra.bluetooth.data.BleDevice
import com.app.okra.bluetooth.data.BleScanState
import com.app.okra.bluetooth.exception.BleException
import com.app.okra.bluetooth.scan.BleScanRuleConfig
import com.app.okra.bluetooth.utils.BleLog
import com.app.okra.bluetooth.utils.HexUtil
import com.app.okra.data.repo.ConnectedDevicesRepoImpl
import com.app.okra.extension.beGone
import com.app.okra.extension.beVisible
import com.app.okra.extension.viewModelFactory
import com.app.okra.utils.*
import com.app.okra.utils.bleValidater.BLEValidaterListener
import com.app.okra.utils.bleValidater.BleValidate
import kotlinx.android.synthetic.main.fragment_bluetooth_devices_list.*
import java.util.*
import kotlin.collections.ArrayList


class BluetoothDevicesListFragment : BaseFragment(),
    Listeners.ItemClickListener,
    PermissionUtils.IGetPermissionListener,
    BLEValidaterListener,
    View.OnClickListener {

    private lateinit var deviceData: BleDevice
    private lateinit var devicesAdapter: ConnectedDevicesAdapter
    private val devicesList = ArrayList<BleDevice>()
    private val mPermissionUtils by lazy {
        PermissionUtils(this)
    }

    private val bleManager by lazy {
        // Sets up the bluetooth controller.
        BleManager.instance
    }

    private val bleValidate by lazy {
        BleValidate(requireActivity(), this)
    }

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory {
            ConnectedDevicesViewModel(ConnectedDevicesRepoImpl(apiServiceAuth))
        }).get(ConnectedDevicesViewModel::class.java)
    }

    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    companion object {
        @JvmStatic
        fun newInstance() = BluetoothDevicesListFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bluetooth_devices_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        setViews()
        setObserver()
        BleManager.instance.init(OkraApplication.getApplicationInstance())
        BleManager.instance.enableLog(true)
            .setReConnectCount(1, AppConstants.BLE_SCAN_TIMEOUT)
            .setConnectOverTime(AppConstants.BLE_SCAN_TIMEOUT).operateTimeout

        bleValidate.checkPermissions()
    }

    private fun setAdapter() {
        devicesAdapter = ConnectedDevicesAdapter(this, devicesList)
        rv_devices.adapter = devicesAdapter
    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, observeError = false)
        viewModel._dataCountLiveData.observe(viewLifecycleOwner){ it ->
            it.data?.let{
                it.deviceId = deviceData.mac!!

                // updated test count into local list
                viewModel.updateDeviceDataList(it)

                // fetching total test count from BLE Device
                navController.navigate(R.id.action_blueToothDeviceListFragment_to_connectionStatusFragment)
            }
        }


        viewModel._errorObserver.observe(viewLifecycleOwner) {
            val data = it.getContent()!!

            if (data.message == sessionMsg) {
                showToast( data.message)
                navigateToLogin(requireActivity())
                requireActivity().finish()
            }else if(data.message == MessageConstants.Errors.network_issue){
                showToast(MessageConstants.Errors.network_issue_try_again)

                /*if(this::deviceData.isInitialized) {
                    BleManager.instance.disconnect(deviceData)
                }
                navController.popBackStack()*/
                requireActivity().finish()
            }
        }

    }

    private fun setViews() {
        llDiscovering.beVisible()
        llList.beGone()
        (activity as BluetoothActivity).setDeleteButtonVisibility(false)
        (activity as BluetoothActivity).setHeaderButtonVisibility(true)
        (activity as BluetoothActivity).setHeaderButtonText(getString(R.string.scan))
    }

    private fun addDeviceInList(device: BleDevice) {
        devicesList.add(device)
    }

    override fun onBluetoothDisable(msg: String) {
        showCustomAlertDialog(
            requireContext(),
            object : Listeners.DialogListener {
                override fun onOkClick(dialog: DialogInterface?) {
                    navController.popBackStack()
                    dialog?.dismiss()
                }

                override fun onCancelClick(dialog: DialogInterface?) {
                    dialog?.dismiss()
                }
            },
            MessageConstants.Messages.bluetooth_turn_on_permission,
            false,
            positiveButtonText = getString(R.string.btn_ok),
            negativeButtonText = getString(R.string.btn_cancel),
            title = getString(R.string.bluetooth),
        )
    }

    override fun onLocationDisable(msg: String) {
        showCustomAlertDialog(
            requireContext(),
            object : Listeners.DialogListener {
                override fun onOkClick(dialog: DialogInterface?) {
                    navController.popBackStack()
                    dialog?.dismiss()
                }

                override fun onCancelClick(dialog: DialogInterface?) {
                    dialog?.dismiss()
                }
            },
            MessageConstants.Messages.please_turn_on_your_location,
            false,
            positiveButtonText = getString(R.string.btn_ok),
            negativeButtonText = getString(R.string.btn_cancel),
            title = getString(R.string.location),
        )
    }

    override fun onPermissionsGiven(data: Int) {
        setScanRule()
        startScan()
    }

    private fun setScanRule() {
        val scanRuleConfig: BleScanRuleConfig = BleScanRuleConfig.Builder()
            .setAutoConnect(false)
            .setScanTimeOut(AppConstants.BLE_SCAN_TIMEOUT)
            .build()
        BleManager.instance.initScanRule(scanRuleConfig)
    }

    override fun onPermissionsDeny(data: Int) {
        showCustomAlertDialog(
            requireContext(),
            object : Listeners.DialogListener {
                override fun onOkClick(dialog: DialogInterface?) {
                    navController.popBackStack()
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


    override fun onDestroyView() {
        super.onDestroyView()
        bleManager.removeConnectGattCallback((activity as BluetoothActivity).connectedBleDevice)
    }

    private fun startScan() {
        BleManager.instance.scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
                if (success) {
                    (activity as BluetoothActivity).setHeaderButtonVisibility(false)
                    (activity as BluetoothActivity).setDeleteButtonVisibility(false,true)

                    devicesList.clear()
                    devicesAdapter!!.notifyDataSetChanged()
                    manageViewVisibility(true)
                }
            }

            override fun onLeScan(bleDevice: BleDevice?) {
                super.onLeScan(bleDevice)
            }

            override fun onScanning(bleDevice: BleDevice?) {
                println(":::: Device Found: ${bleDevice?.device?.name}, " +
                        "Address: ${bleDevice?.device?.address}, "+
                        " Type: ${bleDevice?.device?.type!!}, "+
                        " Rssi: ${bleDevice.rssi} "+
                        " ScanRecord: ${HexUtil.formatHexString(bleDevice.scanRecord)}, "
                )

                addDeviceInList(bleDevice!!)
                devicesAdapter.notifyDataSetChanged()
                manageViewVisibility(true)
            }

            override fun onScanFinished(scanResultList: List<BleDevice?>?) {
                if(isResumed) {
                    (activity as BluetoothActivity).setHeaderButtonVisibility(true)
                    (activity as BluetoothActivity).setDeleteButtonVisibility(false)

                    manageViewVisibility(false)
                    showToast("Scan finished")
                }
                /* img_loading.clearAnimation()
                img_loading.setVisibility(View.INVISIBLE)
                btn_scan.setText(getString(R.string.start_scan))*/
            }
        })
    }

    private fun manageViewVisibility(isDiscovering: Boolean) {
        if(isResumed){
            if(isDiscovering){
                if(devicesList.isNotEmpty()) {
                    llDiscovering.beGone()
                    llList.beVisible()
                }else{
                    llDiscovering.beVisible()
                    llList.beGone()
                }
            }else{
                llDiscovering.beGone()
                llList.beVisible()

                if(devicesList.isNotEmpty()){
                    rv_devices.beVisible()
                    tvNoDevice.beGone()
                }else{
                    rv_devices.beGone()
                    tvNoDevice.beVisible()
                }
            }
        }
    }

    override fun onSelect(o: Any?, o1: Any?) {
        val pos = o as Int
        deviceData = o1 as BleDevice

        var needCancelButton  = false
        val needOkButtonText :String

        val dialogText = if(deviceData.name!=null && deviceData.name!!.toLowerCase(Locale.ROOT)
                .contains("okra")){
            needCancelButton = true
            needOkButtonText = getString(R.string.btn_connect)
            MessageConstants.Messages.do_you_want_to
        }else{
            needOkButtonText = getString(R.string.btn_ok)
            MessageConstants.Messages.you_can_only_pair
        }

        showAlertDialog(requireContext(),object : Listeners.DialogListener{
            override fun onOkClick(dialog: DialogInterface?) {
                if(deviceData.name!=null && deviceData.name!!.toLowerCase(Locale.ROOT).contains("okra")){
                    if (!BleManager.instance.isConnected(deviceData)) {
                        BleManager.instance.cancelScan()
                        connect(deviceData)
                    }
                }
                dialog?.dismiss()
            }

            override fun onCancelClick(dialog: DialogInterface?) {
                dialog?.dismiss()
            }

        }, dialogText,
            needCancelButton,
            needOkButtonText,
            getString(R.string.btn_cancel)
        )

    }

    private fun connect(deviceData: BleDevice) {
        BleManager.instance.connect(deviceData,gattCallBack )
    }

    private var gattCallBack :BleGattCallback?= object : BleGattCallback() {
        override fun onStartConnect() {
            (activity as BluetoothActivity).setHeaderButtonVisibility(false)
            (activity as BluetoothActivity).setDeleteButtonVisibility(false,true)
            showProgressBar()
        }

        override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
            (activity as BluetoothActivity).setHeaderButtonVisibility(true)
            (activity as BluetoothActivity).setDeleteButtonVisibility(false)

            hideProgressBar()
            showToast(getString(R.string.connect_fail))
        }

        override fun onConnectSuccess(bleDevice: BleDevice?, gatt: BluetoothGatt?, status: Int) {
            hideProgressBar()

            if(bleDevice!=null) {
                (activity as BluetoothActivity).connectedBleDevice = bleDevice

                if ((activity as BluetoothActivity).checkIfDeviceCountExist(bleDevice)) {
                    // Show device connected view and get total count from BLE device
                    navController.navigate(R.id.action_blueToothDeviceListFragment_to_connectionStatusFragment)
                }else{
                    // Hit api, get count from it and then get total count from BLE device
                    viewModel.setDeviceDataRequest(bleDevice.name, bleDevice.mac)
                    viewModel.getTestDataCountFromApi()
                }
            }else{
                showToast(MessageConstants.Errors.an_error_occurred)
            }
            showToast("Connection Success")

        }

        override fun onDisConnected(
            isActiveDisConnected: Boolean,
            bleDevice: BleDevice?,
            gatt: BluetoothGatt?,
            status: Int
        ) {
            /*if((activity as BluetoothActivity).getCurrentLoadedFragment() is ConnectionStatusFragment){
                navController.popBackStack()
            }*/

            if(!isDetached) {
                hideProgressBar()

                devicesAdapter.removeDevice(bleDevice)

                if (isActiveDisConnected) {
                    showToast(getString(R.string.active_disconnected))
                } else {
                    showToast(getString(R.string.disconnected),requireActivity())
                }
            }
        }
    }

    override fun onUnSelect(o: Any?, o1: Any?) { }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        startScan()
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.btnSave -> {
                checkAndScan()
            }
        }
    }

    fun checkAndScan() {
        if (checkIfAlreadyScanning()) {
            bleValidate.checkPermissions()
        } else {
            showToast(MessageConstants.Messages.already_scanning)
        }

    }

    private fun checkIfAlreadyScanning(): Boolean {
        return BleManager.instance.scanSate ==  BleScanState.STATE_IDLE
    }


}