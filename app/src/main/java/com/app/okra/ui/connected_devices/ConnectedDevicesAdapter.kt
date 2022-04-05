package com.app.okra.ui.connected_devices

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.okra.R
import com.app.okra.bluetooth.data.BleDevice
import com.app.okra.utils.Listeners
import kotlinx.android.synthetic.main.row_connected_devices.view.*

class ConnectedDevicesAdapter(
    listener: Listeners.ItemClickListener,
    val devicesList: ArrayList<BleDevice>
) : RecyclerView.Adapter<ConnectedDevicesAdapter.ItemViewHolder>() {

    var mlistener = listener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_connected_devices, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return devicesList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
          holder.onBind(devicesList[position], position)

        holder.itemView.setOnClickListener {
            mlistener.onSelect(position, devicesList[position])
        }
    }

    fun removeDevice(bleDevice: BleDevice?) {
        devicesList.remove(bleDevice)
        notifyDataSetChanged()
    }

    fun addDevice(bleDevice: BleDevice?) {
        removeDevice(bleDevice)
        devicesList.add(bleDevice!!)
        notifyDataSetChanged()
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun onBind(data: BleDevice?, position: Int) {
            itemView.tvDeviceName.text = data?.name
            itemView.tvDeviceAddress.text = data?.mac

        }
    }
}
