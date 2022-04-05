package com.app.okra.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.okra.R
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.utils.AppConstants
import com.app.okra.utils.convertMGDLtoMMOL
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily


fun ImageView.loadUserImageFromUrl(context: Context?=null,url: String?, placeholder :Int? =null ){
   val pHolder = placeholder ?: R.mipmap.user_placeholder
    if(!url.isNullOrEmpty()) {
       // println("::::::: URL: $url")
        Glide.with(context!!)
                .load(url)
                .placeholder(pHolder)
                .into(this)
    }else{
      //  println("::::::: NO URL")

        this.setImageResource(pHolder)
    }
}

// for setting Circular corner, set "setAllCornerSize" -TRUE, and set value for 'allCornerSize'
fun ShapeableImageView.loadRoundCornerImageFromUrl(
    url: String,
    placeholder: Int? = null,
    topRight: Float = 0.0f, topLeft: Float = 0.0f,
    bottomRight: Float = 0.0f, bottomLeft: Float = 0.0f,
    setAllCornerSize: Boolean = false, allCornerSize: Float = 0.0f
){
    if(setAllCornerSize){
        this.shapeAppearanceModel = this.shapeAppearanceModel
            .toBuilder()
            .setAllCornerSizes(allCornerSize)
            .build()
    }else {
        this.shapeAppearanceModel = this.shapeAppearanceModel
            .toBuilder()
            .setTopRightCorner(CornerFamily.ROUNDED, topRight)
            .setTopLeftCorner(CornerFamily.ROUNDED, topLeft)
            .setBottomLeftCorner(CornerFamily.ROUNDED, bottomLeft)
            .setBottomRightCorner(CornerFamily.ROUNDED, bottomRight)
            .build()
    }

    if(placeholder!=null) {
        Glide.with(this.context.applicationContext)
            .load(url)
            .placeholder(placeholder)
            .into(this)
    }else{
        Glide.with(this.context.applicationContext)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(this)
    }
}


fun RecyclerView.manageLoadMore(loadMore: Boolean, functionToExecute: () -> Unit){

    this.layoutManager?.let{
        val layoutManager: LinearLayoutManager = it as LinearLayoutManager
        this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                if (lastVisiblePosition == recyclerView.childCount) {
                    if (loadMore) {
                        // loadMore = false
                        functionToExecute.invoke()
                    }
                }
            }
        })
    }
}
fun ViewGroup.inflateView(@LayoutRes layoutRes: Int): View =
    LayoutInflater.from(context).inflate(layoutRes, this, false)


fun View.textStyle(context: Context, type: String){
    val font = Typeface.createFromAsset(context.resources.assets, type);

    when(this){
        is TextView -> {
            typeface = font
        }
        is EditText -> {
            typeface = font
        }
    }
}

fun View.beInvisible() {
    visibility = View.INVISIBLE
}

fun View.beVisible() {
    visibility = View.VISIBLE
}

fun View.beGone() {
    visibility = View.GONE
}

fun View.beDisable() {
    isEnabled = false
     alpha= 0.4f
}

fun View.beEnable() {
    isEnabled = true
     alpha= 1f
}

fun View.isVisible() = visibility == View.VISIBLE

fun View.isInvisible() = visibility == View.INVISIBLE

fun View.isGone() = visibility == View.GONE

fun View.setErrorView(context: Context){
    this.background = ResourcesCompat.getDrawable(context.resources, R.mipmap.bg_edittext_error, null)
}

fun View.setNormalView(context: Context){
    this.background = ResourcesCompat.getDrawable(context.resources, R.mipmap.bg_edittext, null)
}

 fun EditText.setMaxLength(maxLength: Int) {
    filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
}

 fun TextView.getGlucoseToSet(data: String?) {
    val bloodGlucoseUnit = PreferenceManager.getString(AppConstants.Pref_Key.BLOOD_GLUCOSE_UNIT)

     data?.let {
       this.text =   if (bloodGlucoseUnit != null && bloodGlucoseUnit == AppConstants.MM_OL) {
           "${convertMGDLtoMMOL(it.toFloat())} mmol"
       } else {
           "$it mg/dL"
       }
     }
}