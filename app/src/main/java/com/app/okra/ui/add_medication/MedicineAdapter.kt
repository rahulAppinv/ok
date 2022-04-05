package com.app.okra.ui.add_medication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.okra.R
import com.app.okra.models.MedicineName
import com.app.okra.utils.Listeners
import kotlinx.android.synthetic.main.row_medicine.view.*

class MedicineAdapter(
    var listener: Listeners.ItemClickListener,
    private val dataList: ArrayList<MedicineName>,
) : RecyclerView.Adapter<MedicineAdapter.ItemViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_medicine, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.onBind(dataList[position], position)

        holder.itemView.setOnClickListener {
            listener.onSelect(position, dataList[position])
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(data: MedicineName?, position: Int) {

            data?.let { it ->
                    itemView.tvTitle.text = it.medicineName
            }
        }
    }
}
