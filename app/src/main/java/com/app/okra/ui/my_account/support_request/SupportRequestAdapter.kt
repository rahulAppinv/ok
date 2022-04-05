package com.app.okra.ui.my_account.support_request

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.okra.R
import com.app.okra.models.SupportResponse
import com.app.okra.utils.Listeners
import com.app.okra.utils.getDateFromISOInDate
import com.app.okra.utils.getDateFromISOInString
import kotlinx.android.synthetic.main.row_support_request.view.*

class SupportRequestAdapter (var listener: Listeners.ItemClickListener,
                             private val dataList : List<SupportResponse>,
) : RecyclerView.Adapter<SupportRequestAdapter.ItemViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_support_request, parent, false
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

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun onBind(data: SupportResponse?, position: Int) {

            data?.let{ it ->
                it.title?.let{
                    itemView.tvRequestTitle.text = it
                }
                it.description?.let{
                    itemView.tvDetail.text = it
                }

                it.updatedAt?.let{
                    if(it.isNotEmpty()) {
                        itemView.tvTime.text = getDateFromISOInString(it, formatYouWant = "hh:mm a")
                    }
                }

            }
        }
    }
}
