package com.app.okra.ui.my_account.support_request

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.okra.R
import com.app.okra.base.BaseFragment
import com.app.okra.base.BaseViewModel
import com.app.okra.data.repo.SupportRequestRepo
import com.app.okra.data.repo.SupportRequestRepoImpl
import com.app.okra.extension.beGone
import com.app.okra.extension.beVisible
import com.app.okra.extension.viewModelFactory
import com.app.okra.models.ItemModel
import com.app.okra.models.SupportResponse
import com.app.okra.utils.AppConstants
import com.app.okra.utils.Listeners
import kotlinx.android.synthetic.main.fragment_support_request_list.*

class SupportRequestListFragment : BaseFragment(), Listeners.ItemClickListener,
    View.OnClickListener {


    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var requestAdapter: SupportRequestAdapter
    private var pageNo :Int = 1
    private var totalPage: Int = 0
    private var nextHit: Int = 0

    private val requestList  = ArrayList<SupportResponse>()
    private val viewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory {
            SupportRequestViewModel(SupportRequestRepoImpl(apiServiceAuth))
        }).get(SupportRequestViewModel::class.java)
    }

    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    override fun onResume() {
        super.onResume()
        if(requireActivity() is SupportRequestActivity){
            (requireActivity() as SupportRequestActivity).setTitle(getString(R.string.title_my_support_request))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_support_request_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        getData()
        setObserver()
        setListener()
    }

    private fun getData() {
        viewModel.setRequest(pageNo = pageNo.toString(), limit = AppConstants.DATA_LIMIT.toString())
        viewModel.getSupportListApi()
    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, observeError = false)
        viewModel._supportListLiveData.observe(viewLifecycleOwner) { it ->
            swipe_request.isRefreshing = false

            if (it.totalPage != null) {
                totalPage = it.totalPage
            }
            if (it.nextHit != null) {
                nextHit = it.nextHit
            }
            it.data?.let{
                if (pageNo == 1 && requestList.size > 0)
                    requestList.clear()

                requestList.addAll(it)
                requestAdapter.notifyDataSetChanged()
            }
            manageViewVisibility()
        }

        viewModel._errorObserver.observe(viewLifecycleOwner){
            swipe_request.isRefreshing = false
        }

        viewModel._updateListLiveData.observe(viewLifecycleOwner){
            getData()
        }
    }

    private fun manageViewVisibility() {
        if(requestList.isNullOrEmpty()){
            clNoData.beVisible()
            cvSupportList.beGone()
        }else{
            clNoData.beGone()
            cvSupportList.beVisible()
        }
    }

    private fun setAdapter() {
        requestAdapter = SupportRequestAdapter(
            this,
            requestList
        )
        layoutManager =LinearLayoutManager(requireContext())
        rv_support_request.layoutManager = layoutManager
        rv_support_request.adapter = requestAdapter
    }

    private fun setListener() {

        fab.setOnClickListener(this)
        swipe_request.setOnRefreshListener {
            pageNo = 1
            getData()
        }

        rv_support_request.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount: Int = rv_support_request.childCount
                val totalItemCount: Int = rv_support_request.layoutManager!!.itemCount
                val firstVisibleItem: Int =layoutManager.findFirstVisibleItemPosition()

                if(nextHit>0) {
                    if (visibleItemCount + firstVisibleItem >= totalItemCount) {
                        pageNo += 1
                        progressBar_loadMore.visibility = View.VISIBLE
                        getData()
                    }
                }
            }
        })
    }

    override fun onSelect(o: Any?, o1: Any?) {
        val pos = o as Int
        val type = o1 as SupportResponse
        val bundle = bundleOf(SupportRequestDetailsFragment.DATA to type)
        navController.navigate(R.id.action_supportRequestList_to_supportRequestDetailsFragment, bundle)

    }

    override fun onUnSelect(o: Any?, o1: Any?) {}
    override fun onClick(p0: View?) {
            when(p0?.id){
                R.id.fab ->{
                    navController.navigate(R.id.action_supportRequestList_to_addSupportRequestFragment)
                }
            }
    }

}