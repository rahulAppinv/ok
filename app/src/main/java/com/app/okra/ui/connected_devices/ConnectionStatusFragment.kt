package com.app.okra.ui.connected_devices

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.app.okra.R
import com.app.okra.base.BaseFragment
import com.app.okra.base.BaseViewModel
import com.app.okra.bluetooth.BleManager
import com.app.okra.bluetooth.bluetooth.BleConnector.Companion.UUID_CLIENT_WRITE_CHARACTERISTIC_DESCRIPTOR
import com.app.okra.bluetooth.bluetooth.BleConnector.Companion.UUID_CLIENT_WRITE_SERVICE_DESCRIPTOR
import com.app.okra.bluetooth.bluetooth.BleConnector.Companion.mByte_ForCount
import com.app.okra.bluetooth.bluetooth.BleConnector.Companion.mByte_ForData
import com.app.okra.bluetooth.callback.BleWriteCallback
import com.app.okra.bluetooth.data.BleDevice
import com.app.okra.bluetooth.exception.BleException
import com.app.okra.data.repo.ConnectedDevicesRepoImpl
import com.app.okra.extension.viewModelFactory
import com.app.okra.models.BLETestData
import com.app.okra.models.DeviceDataCount
import com.app.okra.utils.*
import kotlinx.android.synthetic.main.fragment_connection_status.*


class ConnectionStatusFragment : BaseFragment(){

    private var totalCountFromDevice: DeviceDataCount?=null
    private var bleDevice: BleDevice?=null
    private var service: BluetoothGattService?=null
    private var characteristic : BluetoothGattCharacteristic?=null
    private var forTestData = false
    private var initValue = 1
    private var limitValue = -1
    private val fetchedTestList = ArrayList<BLETestData>()


    private val bleManager by lazy {
        // Sets up the bluetooth controller.
        BleManager.instance
    }

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory {
            ConnectedDevicesViewModel(ConnectedDevicesRepoImpl(apiServiceAuth))
        }).get(ConnectedDevicesViewModel::class.java)
    }

    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    override fun onResume() {
        super.onResume()
        (activity as BluetoothActivity).setTitle(getString(R.string.title_connect_device))
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_connection_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
        setObserver()
        getServicesOfDevice()
    }

    private fun getServicesOfDevice() {
        bleDevice =  (activity as BluetoothActivity).connectedBleDevice

        if(bleDevice!=null){
            val gatt = bleManager.getBluetoothGatt(bleDevice)

            gatt?.let {
                for (service in it.services) {
                    //  println(">::::: Services: ${service.uuid}")

                    if (service.uuid.toString() == UUID_CLIENT_WRITE_SERVICE_DESCRIPTOR) {
                        this.service = service
                        (activity as BluetoothActivity).gattService = service
                        break
                    }
                }

                if (service != null) {
                    getCharacteristicsOfDevice()
                } else {
                    showToastErrorOccurred()
                }
            }
        }else{
            showToastErrorOccurred()
        }
    }

    private fun getCharacteristicsOfDevice() {
        for (singleCharacteristic in service!!.characteristics) {
            if(singleCharacteristic.uuid.toString() == UUID_CLIENT_WRITE_CHARACTERISTIC_DESCRIPTOR){
                characteristic = singleCharacteristic
                (activity as BluetoothActivity).gattCharacteristic = singleCharacteristic
                break
            }
        }

        if(characteristic!=null){
            if (characteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
                addCallBackForCharacteristicChange()

                bleManager.write(
                    bleDevice,
                    characteristic!!.service.uuid.toString(),
                    characteristic!!.uuid.toString(),
                    mByte_ForCount,
                    object : BleWriteCallback() {
                        override fun onWriteSuccess(
                            current: Int,
                            total: Int,
                            justWrite: ByteArray?
                        ) {
                         //   println("::::::: Output: ${justWrite}")
                        }

                        override fun onWriteFailure(exception: BleException?) {
                         //   println("::::::: Exception: ${exception?.description}")
                        }
                    })
            }else{
                showToastErrorOccurred()
            }
        }else{
            showToastErrorOccurred()
        }
    }

    private fun addCallBackForCharacteristicChange() {

        val bleBluetooth = bleManager.multipleBluetoothController?.getBleBluetooth(bleDevice)

        bleBluetooth?.addOwnCallback(object : Listeners.BLEDataListener {
            override fun onDataReceived(data: ByteArray?) {

                if (!forTestData) {
                    forTestData = true

                    // Extracting recieved Byte array in to readable format i.e BLETestData class
                    val bleTestData = viewModel.extractDataFromByteArray(data, bleDevice)

                    bleTestData?.let {
                        // activity!!.runOnUiThread { showToast("Total Count: ${it.totalDataCount!!}") }

                        //this DeviceDataCount is for updating local data list after successful data
                        // fetch from device
                        totalCountFromDevice = DeviceDataCount(
                            it.deviceId!!,
                            it.totalDataCount!!.toInt()
                        )

                        // println("::::::: Data Received Method call:")

                        // Fetching Local Devices (with count) list
                        val localDataCountList = viewModel.getDeviceDataList()

                        // Here, initially no local data will exist, so when we fetch data from BLE Device
                        // we will get certain count, so automatically it will become the max count. i.e limitValue
                        if (localDataCountList.isNullOrEmpty() && it.totalDataCount!!.toInt() > 0) {
                            limitValue = it.totalDataCount!!.toInt()
                        }
                        else {
                            // Getting data count of currently used device from preferences,
                            // if device is previously connected and data synced.
                            val localDeviceDataCount = viewModel.getDeviceDataCount(bleDevice!!)
                           // println("::::::: Total data Count: ${it.totalDataCount} local data count: $localDeviceDataCount")

                            // In below code, we are finding the latest data count to which we need to fetch from device
                            // (by avoiding the data which is already fetched).
                            // In other way, we can say that fetching count for latest data after last sync.
                            // and 'limitValue' is the count for that.
                            if (it.totalDataCount!!.toInt() > 0) {
                                if (localDeviceDataCount < it.totalDataCount!!.toInt()) {
                                    limitValue = it.totalDataCount!!.toInt() - localDeviceDataCount
                                } else {
                                    showAlreadySyncAlertDialog()
                                }
                            } else {
                                showAlreadySyncAlertDialog()
                            }
                        }

                        println("::::::: limitValue: $limitValue")

                        if (limitValue > -1) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                activity!!.runOnUiThread {
                                    ivImage.setImageResource(R.mipmap.device_sync)
                                    tvInfoText.text = getString(R.string.retrieving_data)
                                }
                                fetchTestDataFromDevice()
                            }, 1000)
                        } else {
                            showAlreadySyncAlertDialog()
                        }
                    }
                } else {
                    /* print("::::: Data Own (For $initValue): ")

                     for (i in data!!.indices) {
                         print(" " + data[i])
                     }
                     println()*/

                    val bleTestData = viewModel.extractDataFromByteArray(data, bleDevice)

                    bleTestData?.let {
                        fetchedTestList.add(it)
                    }

                    initValue++
                    if (initValue <= limitValue) {
                        fetchTestDataFromDevice()
                    } else {
                        viewModel.prepareTestRequest(fetchedTestList)
                        viewModel.addTestApi()
                    }
                }
            }
        })
    }

    private fun showAlreadySyncAlertDialog() {
        executeOnMainThread {
            showAlertDialog(
                requireContext(),
                listener =object :Listeners.DialogListener{
                    override fun onOkClick(dialog: DialogInterface?) {
                        (requireActivity() as BluetoothActivity).navigateToStartingFragment()
                    }

                    override fun onCancelClick(dialog: DialogInterface?) {}

                },
                MessageConstants.Messages.alert_msg_sync,
                false
            )

        }

    }

    private fun fetchTestDataFromDevice() {
        if(initValue<=255){
            mByte_ForData[5] =initValue.toString().toByte()
        }else{
            val value4thByte = (initValue/100).toByte()
            mByte_ForData[4] = value4thByte
            mByte_ForData[5] = initValue.toString().toByte()
        }

        bleManager.write(
            bleDevice,
            characteristic!!.service.uuid.toString(),
            characteristic!!.uuid.toString(),
            mByte_ForData,
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {}

                override fun onWriteFailure(exception: BleException?) {
                    println("::::::: Exception: ${exception?.description}")
                }
            })

    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, observeError = false)
        viewModel._testAddLiveData.observe(viewLifecycleOwner){ it ->
            totalCountFromDevice?.let {
                viewModel.updateDeviceDataList(it)
            }
            it.message?.let { it1 -> showToast(it1) }
            EventLiveData.eventLiveData.value =
                Event(EventLiveData.EventData(ConnectionStatusFragment::class.java.simpleName))
            requireActivity().finish()
        }

        viewModel._errorObserver.observe(viewLifecycleOwner) {
            val data = it.getContent()!!

            if (data.message == sessionMsg) {
                showToast( data.message)
                navigateToLogin(requireActivity())
                requireActivity().finish()
            }else if(data.message == MessageConstants.Errors.network_issue){
                showToast(MessageConstants.Errors.network_issue_try_again)
                if(bleDevice!=null) {
                    BleManager.instance.disconnect(bleDevice)
                }
                navController.popBackStack()
            }
        }

    }

    private fun setViews() {
        ivImage.setImageResource(R.mipmap.welcome_to_ca_mo)
        tvInfoText.text = getString(R.string.device_connected_successfully)
        (activity as BluetoothActivity).setDeleteButtonVisibility(false,true)
        (activity as BluetoothActivity).setHeaderButtonVisibility(false)
    }

}