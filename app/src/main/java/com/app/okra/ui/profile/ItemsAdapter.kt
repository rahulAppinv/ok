package com.app.okra.ui.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.okra.R
import com.app.okra.models.ItemModel
import com.app.okra.ui.my_account.setting.SettingsActivity
import com.app.okra.utils.Listeners
import com.app.okra.utils.getProfileItems
import com.app.okra.utils.getSettingsItems
import kotlinx.android.synthetic.main.row_profile.view.*

class ItemsAdapter (listener: Listeners.ItemClickListener,
                    val context : Context?=null,
                    val type :String?=null
) : RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>() {

    var mlistener = listener
    val rightArrow by lazy {  ResourcesCompat.getDrawable(
            context!!.resources,
            R.mipmap.right_arrow,
            null
    )
    }
    private  val  itemList by lazy {
        if(!type.isNullOrEmpty()) {
            if(type == SettingsActivity::class.java.simpleName){
                getSettingsItems(context)
            }else {
                getProfileItems(context)
            }
        }else{
            getProfileItems(context)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_profile, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.onBind(itemList[position], position)

        holder.itemView.setOnClickListener {
            mlistener.onSelect(position, itemList[position])
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun onBind(data: ItemModel?, position: Int) {
            itemView.tvOptionName.text = data?.name
            itemView.tvOptionName.setCompoundDrawablesWithIntrinsicBounds(
                context?.resources?.let {
                    ResourcesCompat.getDrawable(
                        it,
                        data?.icon!!,
                        null
                    )
                },
                null, rightArrow, null
            )
        }
    }
}
