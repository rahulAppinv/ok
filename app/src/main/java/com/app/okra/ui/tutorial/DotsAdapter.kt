package com.app.okra.ui.tutorial

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.okra.R
import com.app.okra.models.TutorialModel
import kotlinx.android.synthetic.main.row_dots.view.*

class DotsAdapter (  val context : Context?=null,
                   var tutorialList :List<TutorialModel>)
    : RecyclerView.Adapter<DotsAdapter.TutorialViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialViewHolder {
        return TutorialViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_dots, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return tutorialList.size
    }

    override fun onBindViewHolder(holder: TutorialViewHolder, position: Int) {
        holder.onBind(tutorialList[position])
    }

    inner class TutorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun onBind(data: TutorialModel) {
         if(data.isSelected){
             itemView.ivDot.setImageResource(R.mipmap.pagination_selected_btn)
         }else{
             itemView.ivDot.setImageResource(R.mipmap.pagination_unselected_btn)
         }
        }
    }
}
