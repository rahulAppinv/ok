package com.app.okra.ui.add_meal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.okra.R
import com.app.okra.models.Items
import com.app.okra.models.Results
import com.app.okra.utils.Listeners
import kotlinx.android.synthetic.main.row_item_name.view.*
import kotlinx.android.synthetic.main.row_meal.view.*

class FoodItemAdapter (private val data : ArrayList<Items>, val listener : Listeners.ItemClickListener?=null
) : RecyclerView.Adapter<FoodItemAdapter.ItemViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_item_type, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.onBind( position)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun onBind( position: Int) {

            itemView.tvTitle.text = data[position].name
            val details = data[position].servingSizes?.size.toString() + " Serving"
            itemView.tvDetail.text = details
            itemView.setOnClickListener{
                listener?.onSelect(position,null)
            }
        }
    }
}
