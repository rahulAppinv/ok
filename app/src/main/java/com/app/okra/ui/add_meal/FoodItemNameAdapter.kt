package com.app.okra.ui.add_meal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.okra.R
import com.app.okra.models.Results
import com.app.okra.utils.Listeners
import kotlinx.android.synthetic.main.row_item_name.view.*

class FoodItemNameAdapter (private val data : ArrayList<Results>,
                           val listener :Listeners.ItemClickListener?=null
) : RecyclerView.Adapter<FoodItemNameAdapter.ItemViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_item_name, parent, false
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

            itemView.tv_name.text = data[position].group
            itemView.tv_name.background =     if(data[position].isSelected){
                itemView.tv_name.setTextColor(ResourcesCompat.getColor(itemView.context.resources, R.color.white,null))

                ResourcesCompat.getDrawable(itemView.context.resources,
                    R.mipmap.selected, null)
            }else{
                itemView.tv_name.setTextColor(ResourcesCompat.getColor(itemView.context.resources, R.color.black,null))

                ResourcesCompat.getDrawable(itemView.context.resources,
                    R.mipmap.unselected_item, null)
            }

            itemView.setOnClickListener{
                listener?.onSelect(position,null)
            }

        }
    }
}
