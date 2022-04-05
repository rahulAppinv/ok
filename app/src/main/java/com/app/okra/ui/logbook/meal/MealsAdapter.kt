package com.app.okra.ui.logbook.meal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.okra.R
import com.app.okra.models.MealData
import com.app.okra.utils.Listeners
import com.app.okra.utils.getDateFromISOInString
import com.app.okra.utils.getMealTime
import kotlinx.android.synthetic.main.row_meal.view.*

class MealsAdapter (var listener: Listeners.ItemClickListener,
                    private val dataList :  ArrayList<MealData>,
) : RecyclerView.Adapter<MealsAdapter.ItemViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_meal, parent, false
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
        fun onBind(data: MealData?, position: Int) {

            data?.let{ it ->
                it.carbs?.let{
                    val valToSet = "${it.value} ${it.unit} Carbs"
                    itemView.tvDetail.text = valToSet
                }
                it.date?.let{
                    if(it.isNotEmpty()) {
                        itemView.tvGlucoseValue.text = getDateFromISOInString(it, formatYouWant = "hh:mm a")
                    }
                }
                it.foodType?.let{
                    if(it.isNotEmpty()) {
                        itemView.tvTitle.text = it
                    }
                }

            }
        }
    }
}
