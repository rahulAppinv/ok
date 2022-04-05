package com.app.okra.extension

import android.app.Activity
import android.content.Intent
import com.app.okra.base.BaseActivity


fun  Activity.navigationOnly(baseActivity: BaseActivity){
    this.startActivity(Intent(this, baseActivity::class.java))
}

fun  Activity.navigate(intent: Intent){
    this.startActivity(intent)
}


fun  Activity.navigateForResult(activity: Activity,intent: Intent, requestCode: Int){
    activity.startActivityForResult(intent,
        requestCode
    )
}

fun  Activity.navigateForResult(activity: Activity,baseActivity: BaseActivity, requestCode: Int){
    activity.startActivityForResult(Intent(this, baseActivity::class.java),
        requestCode
    )
}

