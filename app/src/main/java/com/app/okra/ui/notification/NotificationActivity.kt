package com.app.okra.ui.notification

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.data.repo.SettingRepoImpl
import com.app.okra.extension.beVisible
import com.app.okra.extension.viewModelFactory
import com.app.okra.ui.my_account.setting.SettingsViewModel
import com.app.okra.utils.AppConstants
import kotlinx.android.synthetic.main.activity_notification.*

class NotificationActivity : BaseActivity(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory {
            SettingsViewModel(SettingRepoImpl(apiServiceAuth))
        }).get(SettingsViewModel::class.java)
    }
    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    private lateinit var navHost: NavHostFragment

    private var pushStatus = PreferenceManager.getBoolean(AppConstants.Pref_Key.PUSH_NOTIFICATION)
    private var inAppStatus = PreferenceManager.getBoolean(AppConstants.Pref_Key.IN_APP_NOTIFICATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        setViews()
        setListener()
        setObserver()
    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, this)
        viewModel._settingLiveData.observe(this){
            saveDataInPreference(
                pushNotificationStatus = pushStatus,
                inAppNotification = inAppStatus,
            )
        }
    }

    private fun setListener() {
        ivBack.setOnClickListener(this)
        btnSave.setOnClickListener {
            val fragment: Fragment = navHost.childFragmentManager.fragments[0]!!
            if(fragment is NotificationFragment)
                fragment.clearAll()
        }
    }

    private fun setViews() {
        navHost = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment

        tvTitle.text = getString(R.string.notification)
        tvTitle.beVisible()
        btnSave.text = getString(R.string.clear_all)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivBack -> {
                finish()
            }
        }
    }

    override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
        when(p0?.id){
            R.id.toggle_push ->{
                pushStatus = p1

            }else -> inAppStatus = p1
        }
        viewModel.setSettingRequest(
            inAppStatus = inAppStatus,
            pushNotification = pushStatus
        )
        viewModel.updateSettings()
    }

    fun showClear(b: Boolean) {
        if(b)
            btnSave.visibility = View.VISIBLE
        else
            btnSave.visibility = View.GONE
    }
}