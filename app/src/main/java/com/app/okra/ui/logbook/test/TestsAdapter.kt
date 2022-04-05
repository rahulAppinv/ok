package com.app.okra.ui.logbook.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.okra.R
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.extension.getGlucoseToSet
import com.app.okra.models.Data
import com.app.okra.utils.*
import com.app.okra.utils.AppConstants.Companion.ALL_TEXT
import kotlinx.android.synthetic.main.row_test.view.*

class TestsAdapter (var listener: Listeners.ItemClickListener,
                    private val dataList :  ArrayList<Data>,
) : RecyclerView.Adapter<TestsAdapter.ItemViewHolder>() {

    var bloodGlucoseUnit: String?=null
    init {
         bloodGlucoseUnit = PreferenceManager.getString(AppConstants.Pref_Key.BLOOD_GLUCOSE_UNIT)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_test, parent, false
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
        fun onBind(data: Data?, position: Int) {

            data?.let{ it ->
                it.bloodGlucose?.let{
                    /*itemView.tvGlucoseValue.text=    if(!bloodGlucoseUnit.isNullOrEmpty()){
                                if(bloodGlucoseUnit == AppConstants.MM_OL){
                                    "${convertMGDLtoMMOL(it.toFloat())} mmol"
                                }else{
                                    "$it mg/dL"
                                }
                    }else{
                        "$it mg/dL"
                    }*/
                itemView.tvGlucoseValue.getGlucoseToSet(it)

                }
                it.testingTime?.let{
                    itemView.tvDetail.text = if(getMealTime(it)!= ALL_TEXT){
                        getMealTime(it)
                    }else{
                        ""
                    }
                }

                it.date?.let{
                    if(it.isNotEmpty()) {
                        itemView.tvTime.text = getDateFromISOInString(it, formatYouWant = "hh:mm a")
                    }
                }

            }
        }
    }
}
