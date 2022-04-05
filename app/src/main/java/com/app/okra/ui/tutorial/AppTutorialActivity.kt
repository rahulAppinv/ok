package com.app.okra.ui.tutorial

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.extension.navigate
import com.app.okra.utils.AppConstants
import kotlinx.android.synthetic.main.activity_app_tutorial.*
import kotlinx.android.synthetic.main.layout_header.*


class AppTutorialActivity : BaseActivity() {

    override fun getViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_tutorial)
        setImage()
    }

    private fun setImage() {
        val okraLogo = BitmapFactory.decodeResource(resources, R.mipmap.okra_home_logo)

        val ssb = SpannableStringBuilder("   ")
        ssb.setSpan(ImageSpan(okraLogo), 1, 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        tvTestMessage2.text = getString(R.string.app_tutorial_test_message1)
        tvTestMessage2.append(ssb)
        tvTestMessage2.append(getString(R.string.app_tutorial_test_message2))

        val ssbMedicine = SpannableStringBuilder("   ")
        ssbMedicine.setSpan(ImageSpan(okraLogo), 1, 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        tvMedicationMessage2.text = getString(R.string.app_tutorial_med_message1)
        tvMedicationMessage2.append(ssb)
        tvMedicationMessage2.append(getString(R.string.app_tutorial_med_message2))

        ivPlay.setOnClickListener {
            navigate(
                Intent(this, VideoPlayerActivity::class.java)
                    .putExtra(AppConstants.DATA, "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4")
            )
        }

        ivBack.setOnClickListener {
            finish()
        }
    }
}