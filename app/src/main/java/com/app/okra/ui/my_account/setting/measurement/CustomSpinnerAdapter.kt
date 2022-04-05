package com.app.okra.ui.my_account.setting.measurement

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.app.okra.R
import com.app.okra.utils.Listeners

class CustomSpinnerAdapter : ArrayAdapter<String?> {
    private var layoutInflater: LayoutInflater
    private var context: Activity
    private var listener: Listeners.InAdapterItemClickListener?=null
    private var data: Array<String>? = null
    private var list: List<String>? = null
    fun setType(type: String) {
        this.type = type
    }

    private var type = ""

    constructor(context: Activity, objects: Array<String>?) : super(context, R.layout.item_custom_spinner, objects!!) {
        this.context = context
        data = objects
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    constructor(context: Activity, list: List<String>?) : super(context, R.layout.item_custom_spinner, list!!) {
        this.context = context
        this.list = list
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    constructor(context: Activity,listener: Listeners.InAdapterItemClickListener, list: List<String>?) : super(context, R.layout.item_custom_spinner, list!!) {
        this.context = context
        this.listener = listener
        this.list = list
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        var view=convertView
        if (view == null) {
            view= layoutInflater.inflate(R.layout.item_custom_spinner, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }

        /*viewHolder.view.setOnClickListener{
            listener?.onItemClick(null, position)
        }*/
        var value = ""
        value = if (data != null) {
            data!![position]
        } else {
            list!![position]
        }
        viewHolder.tv_text.text = value
        return view!!
    }

    inner class ViewHolder(val view: View) {
        var tv_text: TextView = view.findViewById(R.id.tvTitle)

    }

    override fun getCount(): Int {
        return super.getCount()
    }
}