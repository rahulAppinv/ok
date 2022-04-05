package com.app.okra.ui.add_medication

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.app.okra.R
import com.app.okra.base.BaseFragment
import com.app.okra.base.BaseViewModel
import com.app.okra.extension.beDisable
import com.app.okra.extension.beEnable
import com.app.okra.extension.setMaxLength
import com.app.okra.models.MedicationData
import com.app.okra.utils.AppConstants
import com.app.okra.utils.dialog
import kotlinx.android.synthetic.main.fragment_add_medication.*
import kotlinx.android.synthetic.main.layout_button.view.*
import kotlinx.android.synthetic.main.layout_header.*

class AddMedicationFragment : BaseFragment() {

    private var isMG:Boolean = true
    private var isPill:Boolean = false
    private var isML:Boolean = false
    private var name:String = ""

    override fun getViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_medication, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
        setListener()
    }

    private fun setViews() {
        tvTitle.text = getString(R.string.add_medicine)
        layout_button.btnCommon.text = getString(R.string.next)
    }

    private fun setListener() {
        ivBack.setOnClickListener {
            activity?.finish()
        }

        layout_button.btnCommon.setOnClickListener {
            if(etName.text.isNullOrEmpty()){
                showToast(getString(R.string.please_enter_medicine_name))
            }else {
                name = etName.text.toString()
                showUnitDialog()
            }
        }
    }

    fun showUnitDialog() {
        dialog = activity?.let { Dialog(it, R.style.MyCustomTheme) }
        val view: View = LayoutInflater.from(activity).inflate(R.layout.dialog_medicine_unit, null)
        dialog?.apply {
            setContentView(view)
            setCanceledOnTouchOutside(false)

            val lp = dialog!!.window!!.attributes
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.gravity = Gravity.CENTER
            lp.dimAmount = 0.5f
            window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            lp.windowAnimations = R.style.DialogAnimation
            window?.attributes = lp

            val btnAdd: Button = findViewById(R.id.btnAdd)
            val tvTitle: TextView = findViewById(R.id.tvTitle)
            val etUnit: EditText = findViewById(R.id.etUnit)
            val tvMG: TextView = findViewById(R.id.tvMG)
            val tvPill: TextView = findViewById(R.id.tvPill)
            val tvML: TextView = findViewById(R.id.tvML)

            tvTitle.text = name
            btnAdd.beDisable()


            etUnit.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if(!p0.isNullOrEmpty()){
                        btnAdd.beEnable()
                    }else{
                        btnAdd.beDisable()
                    }
                }

                override fun afterTextChanged(p0: Editable?) { }
            })


            tvMG.setOnClickListener {
                isMG = true
                isPill = false
                isML = false
                tvMG.background = resources.getDrawable(R.drawable.bg_button_green)
                tvMG.setTextColor(ContextCompat.getColor(context,R.color.white))
                tvPill.background = null
                tvPill.setTextColor(ContextCompat.getColor(context,R.color.grey_3))
                tvML.background = null
                tvML.setTextColor(ContextCompat.getColor(context, R.color.grey_3))
                etUnit.setText("")
            }

            tvPill.setOnClickListener {
                isPill = true
                isMG = false
                isML = false
                tvPill.background = resources.getDrawable(R.drawable.bg_button_green)
                tvPill.setTextColor(ContextCompat.getColor(context,R.color.white))
                tvMG.background = null
                tvMG.setTextColor(ContextCompat.getColor(context,R.color.grey_3))
                tvML.background = null
                tvML.setTextColor(ContextCompat.getColor(context, R.color.grey_3))
                etUnit.setText("")
            }

            tvML.setOnClickListener {
                isML = true
                isMG = false
                isPill = false
                etUnit.setMaxLength(3)
                tvML.background =
                        ResourcesCompat.getDrawable(resources, R.drawable.bg_button_green, null)
                tvML.setTextColor(ContextCompat.getColor(context, R.color.white))
                tvMG.background = null
                tvMG.setTextColor(ContextCompat.getColor(context, R.color.grey_3))
                tvPill.background = null
                tvPill.setTextColor(ContextCompat.getColor(context, R.color.grey_3))
                etUnit.setText("")
            }

            btnAdd.setOnClickListener {
                if(etUnit.text.isNullOrEmpty()){
                    showToast(getString(R.string.please_enter_medicine_unit))
                }else {
                    if(isPill){
                        if(etUnit.text.toString().toInt() < 1){
                            showToast("Enter atleast 1 pill")
                        } else if(etUnit.text.toString().toInt() > 10){
                            showToast("You can not exceed 10 pills")
                        }else {
                            addMedicationApi(etUnit.text.toString().toInt())
                            dialog?.dismiss()
                        }
                    }else {
                        addMedicationApi(etUnit.text.toString().toInt())
                        dialog?.dismiss()
                    }
                }
            }
            show()
        }
    }

    private fun addMedicationApi(quant: Int) {
        val data = MedicationData()
        var unit = ""
        unit = when {
            isMG -> AppConstants.MG
            isPill -> AppConstants.PILLES
            else -> AppConstants.ML
        }

        data.medicineName =name
        data.unit =unit
        data.quantity =quant

        val bundle = Bundle()
        bundle.putString(AppConstants.MEDICATION_TYPE,"2")
        bundle.putParcelable(AppConstants.DATA,data)
        navController.navigate(R.id.action_addMed_to_medicationDetail, bundle)
    }

}