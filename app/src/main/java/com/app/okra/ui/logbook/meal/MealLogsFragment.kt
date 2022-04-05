package com.app.okra.ui.logbook.meal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.okra.R
import com.app.okra.base.BaseFragmentWithoutNav
import com.app.okra.base.BaseViewModel
import com.app.okra.data.repo.MealLogsRepoImpl
import com.app.okra.extension.beGone
import com.app.okra.extension.beVisible
import com.app.okra.extension.viewModelFactory
import com.app.okra.models.MealData
import com.app.okra.ui.DashBoardActivity


import com.app.okra.ui.add_meal.AddMealActivity
import com.app.okra.ui.add_meal.contract.AddMealContracts
import com.app.okra.ui.logbook.contract.MealLogContract
import com.app.okra.ui.logbook.test.TestLogsFragment
import com.app.okra.utils.*
import kotlinx.android.synthetic.main.fragment_meal_logs.*
import kotlinx.android.synthetic.main.fragment_meal_logs.progressBar_loadMore
import kotlinx.android.synthetic.main.fragment_meal_logs.rv_meal_list
import kotlinx.android.synthetic.main.fragment_meal_logs.tvNoTestLogged

class MealLogsFragment(val listeners: Listeners.EventClickListener?)
    : BaseFragmentWithoutNav(), Listeners.ItemClickListener {

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var mealLogsAdapter: MealLogsAdapter
    private var pageNo :Int = 1
    private var totalPage: Int = 0
    private var nextHit: Int = 0
    // private val mealList  = ArrayList<MealData>()

    private var hashMapKeyList  = ArrayList<String>()
    private var hashMapMealLog = LinkedHashMap<String,  ArrayList<MealData>>()


    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    private val viewModel by lazy {
        ViewModelProvider(this,
                viewModelFactory {
                    MealLogsViewModel(MealLogsRepoImpl(apiServiceAuth))
                }
        ).get(MealLogsViewModel::class.java)
    }

    val activityForResult = registerForActivityResult(MealLogContract()){ result ->
        if(result){
            pageNo=1
            getData(1)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_meal_logs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        getData(pageNo)
        setObserver()
        setListener()
        checkForFilter()
    }

    fun getData(pageNo: Int,
                fromDate: String?=null,
                toDate: String?=null,
                type: String?=null,
    ) {
        viewModel.prepareRequest(pageNo,fromDate,toDate, type)
        viewModel.getMealLogs()
    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, observeError = false)
        viewModel._mealLogLiveData.observe(viewLifecycleOwner) { it ->
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
                mealLogsAdapter.notifyDataSetChanged()

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

                    if (!it.type.isNullOrEmpty() && it.type == AddMealActivity::class.java.simpleName) {
                        event.update()
                        checkForFilter()
                    }
                }
            }
        }
    }

    private fun prepareDateWiseData(testLogData: ArrayList<MealData>) {
        val hashMap = LinkedHashMap<String,  ArrayList<MealData>>()
        if(testLogData.isNotEmpty()) {
            for ((index, data) in testLogData.withIndex()){
                val date = data.date
                date?.let{
                    val dateToSet = getDateFromISOInString(it, formatYouWant = "dd/MM/yyyy")

                    val list: java.util.ArrayList<MealData> = if(hashMap.containsKey(dateToSet)){
                        hashMap[dateToSet] as ArrayList<MealData>
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
            rv_meal_list.beGone()
        }else{
            tvNoTestLogged.beGone()
            rv_meal_list.beVisible()
        }
    }

    private fun setAdapter() {
        mealLogsAdapter = MealLogsAdapter(this, hashMapKeyList, hashMapMealLog)
        layoutManager = LinearLayoutManager(requireContext())
        rv_meal_list.layoutManager = layoutManager
        rv_meal_list.adapter = mealLogsAdapter
    }

    private fun setListener() {
        rv_meal_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount: Int = rv_meal_list.childCount
                val totalItemCount: Int = rv_meal_list.layoutManager!!.itemCount
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
        ivAdd.setOnClickListener {
            startActivity(Intent(activity,AddMealActivity::class.java))
        }
    }

    override fun onSelect(o: Any?, o1: Any?) {
        val data = o1 as MealData
        activityForResult.launch(data)
    }

    override fun onUnSelect(o: Any?, o1: Any?) {
    }

    fun checkForFilter(){
        listeners?.onEventClick(MealLogsFragment::class.java.simpleName, null)
    }


}