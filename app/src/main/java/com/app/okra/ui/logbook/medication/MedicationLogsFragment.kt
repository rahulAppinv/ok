package com.app.okra.ui.logbook.medication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.okra.R
import com.app.okra.base.BaseFragmentWithoutNav
import com.app.okra.base.BaseViewModel
import com.app.okra.data.repo.MedicationRepoImpl
import com.app.okra.extension.beGone
import com.app.okra.extension.beVisible
import com.app.okra.extension.navigate
import com.app.okra.extension.viewModelFactory
import com.app.okra.models.MedicationData
import com.app.okra.ui.DashBoardActivity
import com.app.okra.ui.add_meal.AddMealActivity
import com.app.okra.ui.add_medication.AddMedicationActivity
import com.app.okra.ui.add_medication.MedicationDetailsFragment
import com.app.okra.ui.logbook.meal.MealLogsFragment
import com.app.okra.utils.*
import com.app.okra.utils.AppConstants.Intent_Constant.Companion.RELOAD_SCREEN

import kotlinx.android.synthetic.main.fragment_medication.*

class MedicationLogsFragment(val listeners: Listeners.EventClickListener?) : BaseFragmentWithoutNav(), Listeners.ItemClickListener {

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var medicationAdapter: MedicationAdapter
    private var pageNo :Int = 1
    private var totalPage: Int = 0
    private var nextHit: Int = 0

    private var hashMapKeyList  = ArrayList<String>()
    private var hashMapMealLog = LinkedHashMap<String,  ArrayList<MedicationData>>()


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

    private val activityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        result?.let {
            if (result.data!=null && result.data!!.hasExtra(RELOAD_SCREEN)) {
                pageNo = 1
                getData(pageNo)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_medication, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        getData(pageNo)
        setObserver()
        setListener()
        checkForFilter()
    }

     fun getData(pageNo: Int,fromDate: String?=null,
                        toDate: String?=null,type: String?=null) {
        viewModel.prepareRequest(pageNo,fromDate,toDate,type)
        viewModel.getMedicationList()
    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, observeError = false)
        viewModel._medicationLiveData.observe(viewLifecycleOwner) { it ->
            swipe_request.isRefreshing = false

            if (it.totalPage != null) {
                totalPage = it.totalPage
            }
            if (it.nextHit != null) {
                nextHit = it.nextHit
            }
            it.data?.let{
                if (pageNo == 1 && hashMapKeyList.size > 0) {
                    hashMapMealLog.clear()
                    hashMapKeyList.clear()
                }
                it.data?.let { it1 -> prepareDateWiseData(it1)}
                medicationAdapter.notifyDataSetChanged()

            }
            manageViewVisibility()
        }

        viewModel._errorObserver.observe(viewLifecycleOwner){
            swipe_request.isRefreshing = false
            val data = it.getContent()
            data?.message?.let { it1 -> showToast(it1) }

            if (data?.message == getString(R.string.your_login_session_has_been_expired)) {
                navigateToLogin(requireActivity())
                requireActivity().finish()
            }
        }

        EventLiveData.eventLiveData.observe(viewLifecycleOwner){ event ->
            event.peekContent().let{
                if((requireActivity() as DashBoardActivity).currentFragment() == 1) {
                    if (!it.type.isNullOrEmpty() && it.type == MedicationDetailsFragment::class.java.simpleName) {
                        event.update()
                        checkForFilter()
                    }
                }
            }
        }
    }

    private fun prepareDateWiseData(testLogData: ArrayList<MedicationData>) {
        val hashMap = LinkedHashMap<String,  ArrayList<MedicationData>>()
        if(testLogData.isNotEmpty()) {
            for ((index, data) in testLogData.withIndex()){
                val date = data.createdAt
                date?.let{
                    val dateToSet = getDateFromISOInString(it, formatYouWant = "dd/MM/yyyy")

                    val list: java.util.ArrayList<MedicationData> = if(hashMap.containsKey(dateToSet)){
                        hashMap[dateToSet] as ArrayList<MedicationData>
                    }else{
                        ArrayList()
                    }
                    list.add(data)
                    hashMap[dateToSet]  = list
                }
            }
        }
        hashMapMealLog.putAll(hashMap)
        hashMapKeyList.addAll(hashMap.keys.toList())
    }


    private fun manageViewVisibility() {
        if(hashMapKeyList.isNullOrEmpty()){
            tvNoTestLogged.beVisible()
            rv_medication.beGone()
        }else{
            tvNoTestLogged.beGone()
            rv_medication.beVisible()
        }
    }

    private fun setAdapter() {
        medicationAdapter = MedicationAdapter(this, hashMapKeyList, hashMapMealLog)
        layoutManager = LinearLayoutManager(requireContext())
        rv_medication.layoutManager = layoutManager
        rv_medication.adapter = medicationAdapter
    }

    private fun setListener() {
        rv_medication.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount: Int = rv_medication.childCount
                val totalItemCount: Int = rv_medication.layoutManager!!.itemCount
                val firstVisibleItem: Int =layoutManager.findFirstVisibleItemPosition()

                if(nextHit>0) {
                    if (visibleItemCount + firstVisibleItem >= totalItemCount) {
                        pageNo += 1
                        progressBar_loadMore.visibility = View.VISIBLE
                        getData(pageNo)
                    }
                }
            }
        })

        swipe_request.setOnRefreshListener {
            checkForFilter()
        }
    }

    override fun onSelect(o: Any?, o1: Any?) {
        val data = o1 as MedicationData
        val intent = Intent(requireContext(), AddMedicationActivity::class.java)
        intent.putExtra(AppConstants.DATA, data)
        intent.putExtra(
            AppConstants.Intent_Constant.FROM_SCREEN,
            MedicationLogsFragment::class.java.simpleName
        )
        activityForResult.launch(intent)
    }

    override fun onUnSelect(o: Any?, o1: Any?) {
    }

    fun checkForFilter(){
        listeners?.onEventClick(MedicationLogsFragment::class.java.simpleName, null)
    }
}