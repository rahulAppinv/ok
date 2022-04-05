package com.app.okra.ui.my_account.setting

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.data.repo.SettingRepoImpl
import com.app.okra.extension.beInvisible
import com.app.okra.extension.navigate
import com.app.okra.extension.navigationOnly
import com.app.okra.extension.viewModelFactory
import com.app.okra.models.ItemModel
import com.app.okra.ui.profile.ItemsAdapter
import com.app.okra.ui.profile.StaticContentActivity
import com.app.okra.ui.my_account.setting.contactus.ContactUsActivity
import com.app.okra.ui.my_account.setting.measurement.MeasurementSettingActivity
import com.app.okra.ui.my_account.setting.notification.NotificationSettingActivity
import com.app.okra.utils.*
import com.app.okra.utils.AppConstants.Intent_Constant.Companion.TYPE
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.layout_button.view.*
import kotlinx.android.synthetic.main.layout_header.*
import kotlinx.coroutines.delay


class SettingsActivity : BaseActivity(), View.OnClickListener, Listeners.ItemClickListener {

    private lateinit var itemAdapter: ItemsAdapter
    private val mHandler by lazy { Handler(Looper.getMainLooper())}


    private val userEmail by lazy {
        PreferenceManager.getString(AppConstants.Pref_Key.EMAIL_ID)
    }
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
        setContentView(R.layout.activity_settings)
        setAdapter()
        setViews()
        setObserver()
        setListener()
    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, this)
        viewModel._logoutLiveData.observe(this) {

                navigateToLogin(this)
                finish()

        }

        viewModel._deleteAccountData.observe(this) {
            if(it.statusCode =="200"){
                showToast(MessageConstants.Messages.msg_account_deleted_successfully)
                mHandler.postDelayed({
                    navigateToLogin(this)
                    finish()
                }, 500)
            }
        }
    }

    private fun setAdapter() {
        itemAdapter = ItemsAdapter(
                this,
                this,
                SettingsActivity::class.java.simpleName
        )
        rvOptions.layoutManager = LinearLayoutManager(this)
        rvOptions.adapter = itemAdapter

    }

    private fun setListener() {
        ivBack.setOnClickListener(this)
        includeLogoutButton.btnCommon.setOnClickListener(this)
        includeDeleteButton.btnCommon.setOnClickListener {
            showAlertDialog(this@SettingsActivity, object: Listeners.DialogListener {
                override fun onOkClick(dialog: DialogInterface?) {
                    if(!userEmail.isNullOrEmpty()){
                        viewModel.executeDeleteApi(userEmail!!)
                    }else{
                        showToast(MessageConstants.Errors.an_error_occurred)
                    }
                }

                override fun onCancelClick(dialog: DialogInterface?) {
                    dialog?.dismiss();
                }
            }, MessageConstants.Messages.msg_are_you_sure_deleteAccount,
                true,
            getString(R.string.btn_delete),
            getString(R.string.btn_cancel),
            )
        }
    }


    private fun setViews() {
        tvTitle.text = getString(R.string.title_my_setting)
        includeLogoutButton.btnCommon.text = getString(R.string.btn_log_out)
        includeLogoutButton.btnCommon.setTextColor(ContextCompat.getColor(this, R.color.red))

        includeDeleteButton.btnCommon.text = getString(R.string.btn_delete_account)
        includeDeleteButton.btnCommon.setTextColor(ContextCompat.getColor(this, R.color.red))
        ivDelete.beInvisible()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnCommon -> {
                showCustomAlertDialog(this,object :Listeners.DialogListener{
                    override fun onOkClick(dialog: DialogInterface?) {
                        viewModel.onLogout()
                        dialog?.dismiss()
                    }

                    override fun onCancelClick(dialog: DialogInterface?) {
                        dialog?.dismiss()
                    }

                }, MessageConstants.Messages.logout_message, true,
                getString(R.string.logout),
                getString(R.string.btn_cancel),
                getString(R.string.logout),
                )
            }
            R.id.ivBack -> {
               finish()
            }
        }
    }

    override fun onSelect(o: Any?, o1: Any?) {
        val pos = o as Int
        val type = o1 as ItemModel

        when (pos + 1) {
            1 -> {
              navigate(Intent(this,StaticContentActivity::class.java).apply {
                  putExtra(TYPE,StaticContentActivity.ABOUT_US)
              })
            }
             2 -> {
                 navigate(Intent(this,StaticContentActivity::class.java).apply {
                     putExtra(TYPE,StaticContentActivity.PRIVACY_POLICY)
                 })
            }
            3 -> {
                 navigate(Intent(this,StaticContentActivity::class.java).apply {
                     putExtra(TYPE,StaticContentActivity.TERMS_AND_CONDITION)
                 })
            }
            4 -> {
                 navigationOnly(NotificationSettingActivity())
            }
            5 -> {
                 navigationOnly(MeasurementSettingActivity())
            }
            6 -> {
                 navigate(Intent(this,StaticContentActivity::class.java).apply {
                     putExtra(TYPE,StaticContentActivity.FAQ)
                 })
            }
            7 -> {
                navigationOnly(ContactUsActivity())
            }
        }
    }

    override fun onUnSelect(o: Any?, o1: Any?) {}

}