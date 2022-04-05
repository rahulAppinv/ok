package com.app.okra.ui.logbook.meal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.okra.R
import com.app.okra.models.MealData
import com.app.okra.utils.AppConstants
import com.app.okra.utils.Listeners
import com.app.okra.utils.getDifferentInfoFromDateInString
import kotlinx.android.synthetic.main.row_test_or_meal_logs.view.*

class MealLogsAdapter (var listener: Listeners.ItemClickListener,
                       private val hashMapKeyList : List<String>,
                       private val hashMap : HashMap<String, ArrayList<MealData>>


) : RecyclerView.Adapter<MealLogsAdapter.ItemViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_test_or_meal_logs, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return hashMapKeyList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.onBind( position)


    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun onBind( position: Int) {
            val dateKey = hashMapKeyList[position]
            val entriesOfDate = hashMap[dateKey] as ArrayList<MealData>

            itemView.tvDate.text = getDifferentInfoFromDateInString(
                dateKey,
                initFormat = "dd/MM/yyyy",
                    formatYouWant = AppConstants.DateFormat.DATE_FORMAT_9)

            val adapter = MealsAdapter(listener, entriesOfDate)
            itemView.rvTestLogs.adapter = adapter
        }
    }
}
