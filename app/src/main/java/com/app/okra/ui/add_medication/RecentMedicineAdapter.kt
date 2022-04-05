package com.app.okra.ui.add_medication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.okra.R
import com.app.okra.utils.Listeners
import kotlinx.android.synthetic.main.row_name.view.*

class RecentMedicineAdapter(
    var listener: Listeners.ItemClickListener,
    private val dataList: ArrayList<String>,
) : RecyclerView.Adapter<RecentMedicineAdapter.ItemViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_name, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        if(dataList.size>3)
            return 3
        else
            return dataList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.onBind(dataList[position], position)

        holder.itemView.setOnClickListener {
            listener.onUnSelect(position, dataList[position])
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(data: String?, position: Int) {

            data?.let { it ->
                val string = it.replace("[","").replace("]","").trim()
                    itemView.tvName.text = "\"" + string + "\""
            }
        }
    }
}
