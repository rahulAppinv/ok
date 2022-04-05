package com.app.okra.ui.add_meal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.okra.R
import com.app.okra.models.Results
import com.app.okra.models.ServingSize
import com.app.okra.utils.Listeners
import kotlinx.android.synthetic.main.row_serving.view.*

class FoodServingAdapter (private val data : ArrayList<ServingSize>,
                          val listener :Listeners.ItemClickListener?=null
) : RecyclerView.Adapter<FoodServingAdapter.ItemViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_serving, parent, false
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

            itemView.tv_serving_name.text = data[position].unit
            itemView.tv_serving_name.background = if(data[position].isServingSelected){
                itemView.tv_serving_name.setTextColor(ResourcesCompat.getColor(itemView.context.resources, R.color.white,null))

                ResourcesCompat.getDrawable(itemView.context.resources, R.drawable.bg_green_rounded, null)
            }else{
                itemView.tv_serving_name.setTextColor(ResourcesCompat.getColor(itemView.context.resources, R.color.black,null))

                ResourcesCompat.getDrawable(itemView.context.resources,
                    R.drawable.bg_grey_green_outline_rounded, null)
            }

            itemView.setOnClickListener{
                listener?.onSelect(position,null)
            }
        }
    }
}
