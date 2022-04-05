package com.app.okra.ui.my_account.setting.contactus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.data.repo.SettingRepoImpl
import com.app.okra.extension.viewModelFactory
import com.app.okra.ui.my_account.setting.SettingsViewModel
import com.app.okra.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_contact_us.*
import kotlinx.android.synthetic.main.layout_header.*


class ContactUsActivity : BaseActivity(), View.OnClickListener,
    PermissionUtils.IGetPermissionListener {

    private val mPermissionUtils: PermissionUtils = PermissionUtils(this)

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory {
            SettingsViewModel(SettingRepoImpl(apiServiceAuth))
        }).get(SettingsViewModel::class.java)
    }
    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_us)
        setViews()
        setListener()
        setObserver()
        viewModel.getContactUsApi()
    }

    private fun setViews() {
        tvTitle.text = getString(R.string.title_contact_us)
    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, this)
        viewModel._contactUsLiveData.observe(this){ it ->
            it.data?.let{
                if(!it.contact.isNullOrEmpty()){
                    tvContact.text = it.contact
                }
                if(!it.email.isNullOrEmpty()){
                    tvEmail.text = it.email
                }
                if(!it.address.isNullOrEmpty()){
                    tvAddress.text = it.address
                }
            }
        }
    }

    private fun setListener() {
        ivBack.setOnClickListener(this)
        clAddress.setOnClickListener(this)
        clContact.setOnClickListener(this)
        clEmail.setOnClickListener(this)
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.clContact -> {
                val phone = tvContact.text.toString().trim()

                if(phone.isNotEmpty()) {
                    if(mPermissionUtils.checkAndGetCallPermissions(this)) {
                        val callIntent = Intent(Intent.ACTION_CALL)
                        callIntent.data = Uri.parse("tel:${phone}")
                        startActivity(callIntent)
                    }
                }
            }
            R.id.clEmail -> {
                val email = tvEmail.text.toString().trim()

                if(email.isNotEmpty()) {

                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "plain/text"
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                    startActivity(Intent.createChooser(intent, "Choose the app"))
                }
            }
            R.id.clAddress -> {
                /*val address = tvAddress.text.toString().trim()

                if(address.isNotEmpty()) {
                    val gmmIntentUri = Uri.parse("geo:0,0?q=$address")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                }*/
            }
        }
    }

    override fun onPermissionsGiven(data: Int) {
        val phone = tvContact.text.toString().trim()

        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:${phone}")
        startActivity(callIntent)
    }

    override fun onPermissionsDeny(data: Int) {

    }

}