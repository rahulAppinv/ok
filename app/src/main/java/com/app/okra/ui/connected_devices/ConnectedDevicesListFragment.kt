package com.app.okra.ui.connected_devices

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.app.okra.R
import com.app.okra.base.BaseFragment
import com.app.okra.base.BaseViewModel
import com.app.okra.bluetooth.BleManager
import com.app.okra.bluetooth.data.BleDevice
import com.app.okra.bluetooth.data.BleScanState
import com.app.okra.bluetooth.scan.BleScanRuleConfig
import com.app.okra.data.repo.ConnectedDevicesRepoImpl
import com.app.okra.extension.beGone
import com.app.okra.extension.beVisible
import com.app.okra.extension.viewModelFactory
import com.app.okra.models.BLEDeviceListData
import com.app.okra.utils.*
import com.app.okra.utils.bleValidater.BLEValidaterListener
import com.app.okra.utils.bleValidater.BleValidate
import kotlinx.android.synthetic.main.fragment_connected_devices_list.*
import kotlinx.android.synthetic.main.layout_button.*
import java.util.*
import kotlin.collections.ArrayList


class ConnectedDevicesListFragment : BaseFragment(),
    Listeners.ItemClickListener,
    BLEValidaterListener,
    View.OnClickListener {

    private lateinit var devicesAdapter: ConnectedDevicesAdapter
    private val devicesList = ArrayList<BleDevice>()

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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_connected_devices_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        setViews()
        setObserver()
        setViewListener()
        bleValidate.checkPermissions()
        viewModel.getPreviousDevices()
    }

    private fun setViewListener() {
        btnCommon.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        (activity as BluetoothActivity).setTitle(getString(R.string.title_connected_devices))
    }
    override fun onDestroy() {
        super.onDestroy()
        hideProgressBar()

    }
    private fun setAdapter() {
        devicesAdapter = ConnectedDevicesAdapter(this, devicesList)
        rv_devices.adapter = devicesAdapter
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

    private fun setViews() {
        btnCommon.text = getString(R.string.btn_connect)
        (activity as BluetoothActivity).setDeleteButtonVisibility(makeVisible = false, beInvisible = true)
        (activity as BluetoothActivity).setHeaderButtonVisibility(false)
    }


    override fun onBluetoothDisable(msg: String) {
       /* showCustomAlertDialog(
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
        )*/
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

    override fun onPermissionsGiven(data: Int) {}

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


    private fun manageViewVisibility() {
        if(isResumed){
            if(devicesList.isNotEmpty()) {
                llNoData.beGone()
                llList.beVisible()
            }else{
                llNoData.beVisible()
                llList.beGone()
            }
        }
    }

    override fun onSelect(o: Any?, o1: Any?) {
        val pos = o as Int
        navController.navigate(R.id.action_connectedDevicesListFragment_to_blueToothDeviceListFragment)

    }

    override fun onUnSelect(o: Any?, o1: Any?) { }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.btnCommon -> {
                navController.navigate(R.id.action_connectedDevicesListFragment_to_connectedDevicesFragment)
            }
        }
    }
}