package com.app.okra.ui.logbook.test

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
import com.app.okra.data.repo.TestLogsRepoImpl
import com.app.okra.extension.beGone
import com.app.okra.extension.beVisible
import com.app.okra.extension.viewModelFactory
import com.app.okra.models.Data
import com.app.okra.ui.DashBoardActivity
import com.app.okra.ui.add_meal.AddMealActivity
import com.app.okra.ui.connected_devices.ConnectionStatusFragment
import com.app.okra.ui.logbook.test.contract.TestLogContract
import com.app.okra.utils.EventLiveData


import com.app.okra.utils.Listeners
import com.app.okra.utils.getDateFromISOInString
import com.app.okra.utils.navigateToLogin
import kotlinx.android.synthetic.main.fragment_test_logs.*
import kotlinx.android.synthetic.main.fragment_test_logs.progressBar_loadMore

class TestLogsFragment(val listeners: Listeners.EventClickListener?) : BaseFragmentWithoutNav(),
    Listeners.ItemClickListener {

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var requestAdapter: TestLogsAdapter
    private var pageNo :Int = 1
    private var totalPage: Int = 0
    private var nextHit: Int = 0
    private var hashMapKeyList  = ArrayList<String>()
    private var hashMapTestLog = LinkedHashMap<String,  ArrayList<Data>>()

    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    private val viewModel by lazy {
        ViewModelProvider(this,
            viewModelFactory {
                TestLogsViewModel(TestLogsRepoImpl(apiServiceAuth))
            }
        ).get(TestLogsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_test_logs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
      //  getData(pageNo)
        setObserver()
        setListener()
        checkForFilter()
    }

    public fun getData(pageNo: Int,
                        testingTime: String?=null,
                        fromDate: String?=null,
                        toDate: String?=null) {
        viewModel.prepareRequest(pageNo,testingTime, fromDate, toDate)
        viewModel.getTestLogs()
    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, observeError = false)
        viewModel._testListLiveData.observe(viewLifecycleOwner) { it ->
            swipe_request.isRefreshing = false

            if (it.totalPage != null) {
                totalPage = it.totalPage
            }
            if (it.nextHit != null) {
                nextHit = it.nextHit
            }
            it.data?.let{
                if (pageNo == 1 && hashMapTestLog.size > 0) {
                    hashMapTestLog.clear()
                    hashMapKeyList.clear()
                }

                it.data?.let { it1 -> prepareDateWiseData(it1)
                    /*requestList.addAll(it1)*/ }
                requestAdapter.notifyDataSetChanged()
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
            if((requireActivity() as DashBoardActivity).currentFragment() == 1) {
                event.peekContent().let {
                    if (!it.type.isNullOrEmpty() && it.type == ConnectionStatusFragment::class.java.simpleName) {
                        event.update()
                        checkForFilter()
                    }
                }
            }
        }

    }

    private fun prepareDateWiseData(testLogData: ArrayList<Data>) {
        val hashMap = LinkedHashMap<String,  ArrayList<Data>>()
        if(testLogData.isNotEmpty()) {
            for ((index, data) in testLogData.withIndex()){
                val date = data.date
                date?.let{
                    val dateToSet = getDateFromISOInString(it, formatYouWant = "dd/MM/yyyy")

                    val list: java.util.ArrayList<Data> = if(hashMap.containsKey(dateToSet)){
                       hashMap[dateToSet] as ArrayList<Data>
                    }else{
                        ArrayList()
                    }
                    list.add(data)
                    hashMap[dateToSet]  = list
                }
            }
        }
        hashMapTestLog.putAll(hashMap)
        hashMapKeyList.addAll(hashMap.keys.toList())
    }

    private fun manageViewVisibility() {
        if(hashMapTestLog.isNullOrEmpty()){
            tvNoTestLogged.beVisible()
            rv_test_list.beGone()
        }else{
            tvNoTestLogged.beGone()
            rv_test_list.beVisible()
        }
    }

    private fun setAdapter() {
        requestAdapter = TestLogsAdapter(this, hashMapKeyList, hashMapTestLog)
        layoutManager = LinearLayoutManager(requireContext())
        rv_test_list.layoutManager = layoutManager
        rv_test_list.adapter = requestAdapter
    }

    private fun setListener() {
        rv_test_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount: Int = rv_test_list.childCount
                val totalItemCount: Int = rv_test_list.layoutManager!!.itemCount
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

    private val activityForResult = registerForActivityResult(TestLogContract()){ result ->
            if(result){
                pageNo=1
                getData(1)
            }
    }

    override fun onSelect(o: Any?, o1: Any?) {
        val data = o1 as Data

        activityForResult.launch(data)

      /*  startActivity(Intent(activity, TestDetailsActivity::class.java)
            .putExtra("data",data))*/
    }

    override fun onUnSelect(o: Any?, o1: Any?) {
    }


    fun checkForFilter(){
        listeners?.onEventClick(TestLogsFragment::class.java.simpleName, null)
    }

}