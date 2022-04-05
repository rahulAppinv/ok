package com.app.okra.ui.add_medication

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.okra.R
import com.app.okra.base.BaseFragment
import com.app.okra.base.BaseViewModel
import com.app.okra.data.preference.PreferenceManager
import com.app.okra.data.repo.MedicationRepoImpl
import com.app.okra.extension.setMaxLength
import com.app.okra.extension.viewModelFactory
import com.app.okra.models.MedicationData
import com.app.okra.models.MedicineName
import com.app.okra.ui.logbook.medication.MedicationViewModel
import com.app.okra.utils.AppConstants
import com.app.okra.utils.Listeners
import com.app.okra.utils.dialog
import com.app.okra.utils.navigateToLogin
import kotlinx.android.synthetic.main.fragment_search_medication.*
import kotlinx.android.synthetic.main.fragment_search_medication.rv_medication
import kotlinx.android.synthetic.main.layout_header.*
import kotlinx.android.synthetic.main.layout_header.tvTitle
import java.util.*
import kotlin.collections.ArrayList

class SearchMedicationFragment : BaseFragment(), Listeners.ItemClickListener {

    val TAG = SearchMedicationFragment::class.java.simpleName
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var medicationAdapter: MedicineAdapter
    private lateinit var recentMedicationAdapter: RecentMedicineAdapter
    private val data by lazy { ArrayList<MedicineName>() }
    private var isMG: Boolean = true
    private var isPill: Boolean = false
    private var isML: Boolean = false
    private val recentMedicine by lazy { ArrayList<String>() }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_medication, container, false)
    }

    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    private val viewModel by lazy {
        ViewModelProvider(this,
                viewModelFactory {
                    MedicationViewModel(MedicationRepoImpl(apiServiceAuth))
                }
        ).get(MedicationViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        setViews()
        setListener()
        setObserver()
    }

    private fun setAdapter() {

        val myList = ArrayList(Arrays.asList(
            PreferenceManager.getString(AppConstants.Pref_Key.RECENT_MEDICINE)?.replace("[","")?.replace("]","")?.trim()?.split(",")))
        for (i in 0 until (myList[0]?.size ?: 0)){
            myList[0]?.let {
                if(!TextUtils.isEmpty(it.get(i)))
                    recentMedicine.add(it.get(i).trim())
            }
        }

        if(recentMedicine.size==0){
            tvRecentSearch.visibility = View.GONE
        }
        medicationAdapter = MedicineAdapter(this, data)
        layoutManager = LinearLayoutManager(requireContext())
        rv_medication.layoutManager = layoutManager
        rv_medication.adapter = medicationAdapter

        recentMedicationAdapter = RecentMedicineAdapter(this, recentMedicine)
        layoutManager = LinearLayoutManager(requireContext())
        rv_recent_medication.layoutManager = layoutManager
        rv_recent_medication.adapter = recentMedicationAdapter
    }

    private fun setViews() {
        tvTitle.text = getString(R.string.medication)
    }

    private fun setListener() {
        ivBack.setOnClickListener {
            activity?.finish()
        }

        tvAddMed.setOnClickListener {
            navController.navigate(R.id.action_searchMed_to_addMed, null)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0?.length!! == 0) {
                    tvRecentSearch.visibility = View.VISIBLE
                    rv_recent_medication.visibility = View.VISIBLE
                    tvMedicine.visibility = View.GONE
                    data.clear()
                    medicationAdapter.notifyDataSetChanged()
                } else {
                    tvRecentSearch.visibility = View.GONE
                    rv_recent_medication.visibility = View.GONE
                    tvMedicine.visibility = View.VISIBLE
                    if (p0.length > 2) {
                        viewModel.searchMedication(p0.toString())
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

    }

    override fun onSelect(o: Any?, o1: Any?) {
        val data = o1 as MedicineName
        if(!recentMedicine.contains(etSearch.text.toString()))
            recentMedicine.add(0, etSearch.text.toString())
        PreferenceManager.putString(AppConstants.Pref_Key.RECENT_MEDICINE, recentMedicine.toString())
        recentMedicationAdapter.notifyDataSetChanged()
        showUnitDialog(data.medicineName)
    }

    override fun onUnSelect(o: Any?, o1: Any?) {
        val data = o1 as String
        etSearch.setText(data.replace("[","").replace("]","").trim())
    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, observeError = false)
        viewModel._searchMedicationLiveData.observe(viewLifecycleOwner) { it ->
            it.data?.let {
                it.data?.let { it1 ->
                    data.clear()
                    data.addAll(it1)
                }
                medicationAdapter.notifyDataSetChanged()
            }
        }

        viewModel._errorObserver.observe(viewLifecycleOwner) {
            val data = it.getContent()
            data?.message?.let { it1 -> showToast(it1) }

            if (data?.message == getString(R.string.your_login_session_has_been_expired)) {
                navigateToLogin(requireActivity())
                requireActivity().finish()
            }
        }
    }

    fun showUnitDialog(medicineName: String?) {
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

            tvTitle.text = medicineName

            tvMG.setOnClickListener {
                isMG = true
                isPill = false
                isML = false
                etUnit.setMaxLength(4)
                tvMG.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.bg_button_green, null)
                tvMG.setTextColor(ContextCompat.getColor(context, R.color.white))
                tvPill.background = null
                tvPill.setTextColor(ContextCompat.getColor(context, R.color.grey_3))
                tvML.background = null
                tvML.setTextColor(ContextCompat.getColor(context, R.color.grey_3))
                etUnit.setText("")
            }

            tvPill.setOnClickListener {
                isPill = true
                isMG = false
                isML = false
                etUnit.setMaxLength(2)
                tvPill.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.bg_button_green, null)
                tvPill.setTextColor(ContextCompat.getColor(context, R.color.white))
                tvMG.background = null
                tvMG.setTextColor(ContextCompat.getColor(context, R.color.grey_3))
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
                if (etUnit.text.isNullOrEmpty()) {
                    showToast(getString(R.string.please_enter_medicine_unit))
                } else {
                    if (isPill) {
                        if (etUnit.text.toString().toInt() < 1) {
                            showToast("Enter atleast 1 pill")
                        } else if (etUnit.text.toString().toInt() > 10) {
                            showToast("You can not exceed 10 pills")
                        } else {
                            addMedicationApi(etUnit.text.toString().toInt(), medicineName)
                            dialog?.dismiss()
                        }
                    } else {
                        addMedicationApi(etUnit.text.toString().toInt(), medicineName)
                        dialog?.dismiss()
                    }
                }
            }
            show()
        }
    }



    private fun addMedicationApi(quant: Int, medicineName: String?) {
        var unit = ""
        unit = when {
            isMG -> AppConstants.MG
            isPill -> AppConstants.PILLES
            else -> AppConstants.ML
        }

        val medicationData = MedicationData()
        medicationData.medicineName = medicineName
        medicationData.unit = unit
        medicationData.quantity = quant

        val bundle = Bundle()
        bundle.putString(AppConstants.MEDICATION_TYPE, "1")
        bundle.putParcelable(AppConstants.DATA, medicationData)
        bundle.putString(AppConstants.Intent_Constant.FROM_SCREEN, TAG)

        navController.navigate(R.id.action_searchMed_to_medicationDetail, bundle)
    }
}