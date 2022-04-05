package com.app.okra.ui.my_account.setting.measurement

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.ViewModelProvider
import com.app.okra.R
import com.app.okra.base.BaseActivity
import com.app.okra.base.BaseViewModel
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.data.repo.SettingRepoImpl
import com.app.okra.extension.viewModelFactory
import com.app.okra.ui.my_account.setting.SettingsViewModel
import com.app.okra.utils.AppConstants
import com.app.okra.utils.AppConstants.Companion.MG_DL
import com.app.okra.utils.AppConstants.Companion.MM_OL
import com.app.okra.utils.convertMGDLtoMMOL
import com.app.okra.utils.convertMMOLtoMGDL
import kotlinx.android.synthetic.main.activity_measurements_setting.*
import kotlinx.android.synthetic.main.layout_button.*
import kotlinx.android.synthetic.main.layout_header.*


class MeasurementSettingActivity : BaseActivity(), View.OnClickListener{

    private var selectedHyper: String?=null
    private var selectedHypo: String?=null
    private lateinit var customSpinner: CustomSpinnerAdapter
    private var isFirstTime  = true
    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory {
            SettingsViewModel(SettingRepoImpl(apiServiceAuth))
        }).get(SettingsViewModel::class.java)
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private val bloodGlucoseList by lazy {
        arrayListOf<String>()
    }

    private var bloodGlucoseUnit = PreferenceManager.getString(AppConstants.Pref_Key.BLOOD_GLUCOSE_UNIT)
    private var hyperBloodGlucoseUnit = PreferenceManager.getString(AppConstants.Pref_Key.HYPER_BLOOD_GLUCOSE_UNIT)
    private var hypoBloodGlucoseUnit = PreferenceManager.getString(AppConstants.Pref_Key.HYPO_BLOOD_GLUCOSE_UNIT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measurements_setting)
        setViews()
        setAdapter()
        setListener()
        setObserver()
    }

    private fun setAdapter() {
        customSpinner = CustomSpinnerAdapter(this, bloodGlucoseList)
        spinner.adapter = customSpinner

        var index = 0
        if(!bloodGlucoseUnit.isNullOrEmpty()){
            for((i, data) in bloodGlucoseList.withIndex()){
                if(bloodGlucoseUnit == data){
                    index = i
                    break;
                }
            }
        }
        spinner.setSelection(index)
        tvSpinner.text = bloodGlucoseList[index]
    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, this)
        viewModel._settingLiveData.observe(this){
            PreferenceManager.putString(AppConstants.Pref_Key.BLOOD_GLUCOSE_UNIT, tvSpinner.text.toString())
            println("::: Hyper Saving: $selectedHyper , Hypo Saving: $selectedHypo")

            if(!selectedHyper.isNullOrEmpty()) {
                PreferenceManager.putString(AppConstants.Pref_Key.HYPER_BLOOD_GLUCOSE_UNIT,
                        selectedHyper
                )
            }
            if(!selectedHypo.isNullOrEmpty())
                PreferenceManager.putString(AppConstants.Pref_Key.HYPO_BLOOD_GLUCOSE_UNIT,
                        selectedHypo
                )
            finish()
        }
    }

    private fun setListener() {
        ivBack.setOnClickListener(this)
        btnCommon.setOnClickListener(this)
        tvSpinner.setOnClickListener(this)
        setTextChangeListener()

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                tvSpinner.text = bloodGlucoseList[p2]
                updateHyperHypo(bloodGlucoseList[p2])
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun updateHyperHypo(type: String) {
        if(isFirstTime){
            isFirstTime=false
            return
        }

        if(type == MM_OL){
            if(!etHyperBlood.text.isNullOrEmpty()) {
                etHyperBlood.setText(convertMGDLtoMMOL(etHyperBlood.text.toString().toFloat()))
            }
            if(!etHypoBlood.text.isNullOrEmpty()) {
                etHypoBlood.setText(convertMGDLtoMMOL(etHypoBlood.text.toString().toFloat()))
            }
        }else{
            if(!etHyperBlood.text.isNullOrEmpty()) {
                etHyperBlood.setText(convertMMOLtoMGDL(etHyperBlood.text.toString().toFloat()))
            }
            if(!etHypoBlood.text.isNullOrEmpty()) {
                etHypoBlood.setText(convertMMOLtoMGDL(etHypoBlood.text.toString().toFloat()))
            }
        }
    }

    private fun setTextChangeListener() {
        /*   etHyperBlood.addTextChangedListener(object : TextWatcher {
               override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

               override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                   if (!p0.isNullOrEmpty()) {
                       var valueToModify = p0.toString()
                       val unit = tvSpinner.text.toString()
                       if (!isHyperEdited) {
                           isHyperEdited = true

                           if (valueToModify.contains(unit)) {
                               valueToModify = valueToModify.replace(unit, "")
                           }
                           val textToSet = "$valueToModify ${tvSpinner.text}"
                           etHyperBlood.setText(textToSet)
                    //       etHyperBlood.setText(etHyperBlood.text!!.length)
                       }else{
                           isHyperEdited = false
                       }
                   }
               }

               override fun afterTextChanged(p0: Editable?) {}
           })

           etHypoBlood.addTextChangedListener(object : TextWatcher {
               override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

               override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                   if (!p0.isNullOrEmpty()) {
                       var valueToModify = p0.toString()
                       val unit = tvSpinner.text.toString()
                       if (!isHypoEdited) {
                           isHypoEdited = true

                           if (valueToModify.contains(unit)) {
                               valueToModify = valueToModify.replace(unit, "")
                           }
                           val textToSet = "$valueToModify ${tvSpinner.text}"
                           etHypoBlood.setText(textToSet)
                        //   etHypoBlood.setText(etHypoBlood.text!!.length)
                       }else{
                           isHypoEdited = false
                       }
                   }
               }

               override fun afterTextChanged(p0: Editable?) {}
           })
   */

    }

    private fun setViews() {
        bloodGlucoseList.add(MG_DL)
        bloodGlucoseList.add(MM_OL)
        tvTitle.text = getString(R.string.title_measurement_setting)
        btnCommon.text = getString(R.string.btn_save)
        if(bloodGlucoseUnit!=null){
            println("::: Hyper Saved: $hyperBloodGlucoseUnit , Hypo Saved: $hypoBloodGlucoseUnit")
            if(bloodGlucoseUnit == MM_OL) {
                if(!hyperBloodGlucoseUnit.isNullOrEmpty() && hyperBloodGlucoseUnit!!.toFloat() !=0.0F) {
                    etHyperBlood.setText(convertMGDLtoMMOL(hyperBloodGlucoseUnit!!.toFloat()))
                }

                if(!hypoBloodGlucoseUnit.isNullOrEmpty() && hypoBloodGlucoseUnit!!.toFloat() !=0.0F) {
                    etHypoBlood.setText(convertMGDLtoMMOL(hypoBloodGlucoseUnit!!.toFloat()))
                }

            }else{
                if(!hyperBloodGlucoseUnit.isNullOrEmpty() && hyperBloodGlucoseUnit!!.toFloat() !=0.0F) {
                    etHyperBlood.setText(hyperBloodGlucoseUnit.toString())
                }

                if(!hypoBloodGlucoseUnit.isNullOrEmpty() && hypoBloodGlucoseUnit!!.toFloat() !=0.0F) {
                    etHypoBlood.setText(hypoBloodGlucoseUnit.toString())
                }
            }
        }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.tvSpinner -> {
                spinner.performClick()
            }
            R.id.btnCommon -> {
                selectedHyper = if (!etHyperBlood.text.isNullOrEmpty()) {
                    if(tvSpinner.text.toString() == MM_OL){
                        convertMMOLtoMGDL(etHyperBlood.text.toString().trim().toFloat())
                    }else {
                        etHyperBlood.text.toString().trim()
                    }
                }else{
                    null
                }

                selectedHypo = if (!etHypoBlood.text.isNullOrEmpty()) {
                    if(tvSpinner.text.toString() == MM_OL){
                        convertMMOLtoMGDL(etHypoBlood.text.toString().trim().toFloat())
                    }else {
                        etHypoBlood.text.toString().trim()
                    }
                }else{
                    null
                }

                viewModel.setSettingRequest(
                        bloodGlucoseUnit = tvSpinner.text.toString(),
                        hyperBloodGlucoseValue = selectedHyper,
                        hypoBloodGlucoseValue = selectedHypo
                )
                viewModel.updateSettings()
            }
        }
    }

}