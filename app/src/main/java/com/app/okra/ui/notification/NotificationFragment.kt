package com.app.okra.ui.notification

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.okra.R
import com.app.okra.base.BaseFragment
import com.app.okra.base.BaseViewModel
import com.app.okra.data.repo.NotificationRepoImpl
import com.app.okra.extension.*
import com.app.okra.models.Notification
import com.app.okra.utils.*
import com.app.okra.utils.swipe.RecyclerTouchListener
import kotlinx.android.synthetic.main.fragment_notification.*
import kotlinx.android.synthetic.main.fragment_notification.swipe_request

class NotificationFragment : BaseFragment(), Listeners.ItemClickListener {

    private lateinit var notificationAdapterToday: NotificationRecyclerAdapter
    private lateinit var notificationAdapterEarlier: NotificationRecyclerAdapter
    private var touchListener1: RecyclerTouchListener? = null
    private var touchListener2: RecyclerTouchListener? = null
    private val notificationToday by lazy { ArrayList<Notification>() }
    private val notificationEarlier by lazy { ArrayList<Notification>() }

    private var pageNo: Int = 1
    private var totalPage: Int = 0
    private var nextHit: Int = 0

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory {
            NotificationViewModel(NotificationRepoImpl(apiServiceAuth))
        }).get(NotificationViewModel::class.java)
    }

    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        setObserver()
        setListener()
        viewModel.getNotification(pageNo)
    }

       private fun setAdapter() {
        notificationAdapterToday = NotificationRecyclerAdapter(requireContext(), notificationToday)
        notificationAdapterEarlier =
            NotificationRecyclerAdapter(requireContext(), notificationEarlier)

        rv_today.adapter = notificationAdapterToday
        rv_earlier.adapter = notificationAdapterEarlier

        touchListener1 = RecyclerTouchListener(requireActivity(), rv_today)
        touchListener2 = RecyclerTouchListener(requireActivity(), rv_earlier)
        touchListener1!!.setClickable(object : RecyclerTouchListener.OnRowClickListener {
            override fun onRowClicked(position: Int) {
              //  navController.navigate(R.id.action_notificationFragment_to_addSupportRequestFragment, null)
            }

            override fun onIndependentViewClicked(independentViewID: Int, position: Int) {}
        }).setSwipeOptionViews(R.id.clRowBG)
            .setSwipeable(R.id.clRowFG, R.id.clRowBG
            ) { viewID, position ->
                when (viewID) {
                    R.id.clRowBG -> {
                        showCustomAlertDialog(
                            requireContext(),
                            object : Listeners.DialogListener {
                                override fun onOkClick(dialog: DialogInterface?) {
                                    notificationToday[position]._id?.let {
                                        val list = ArrayList<String>()
                                        list.add(it)
                                        viewModel.deleteNotification(
                                            list
                                        )
                                        notificationToday.removeAt(position)
                                        notificationAdapterToday.notifyDataSetChanged()
                                    }
                                    dialog?.dismiss()
                                }

                                override fun onCancelClick(dialog: DialogInterface?) {
                                    dialog?.dismiss()
                                }
                            },
                            getString(R.string.are_you_sure_you_want_to_clear_this_your_notifications),
                            true,
                            positiveButtonText = getString(R.string.btn_ok),
                            getString(R.string.btn_cancel),
                            title = getString(R.string.clear_notification),
                        )
                    }
                }
            }

        touchListener2!!.setClickable(object : RecyclerTouchListener.OnRowClickListener {
            override fun onRowClicked(position: Int) {
                navController.navigate(R.id.action_notificationFragment_to_addSupportRequestFragment, null)
            }

            override fun onIndependentViewClicked(independentViewID: Int, position: Int) {}
        })
            .setSwipeOptionViews(R.id.clRowBG)
            .setSwipeable(
                R.id.clRowFG, R.id.clRowBG
            ) { viewID, position ->
                when (viewID) {
                    R.id.clRowBG -> {
                        showCustomAlertDialog(
                            requireContext(),
                            object : Listeners.DialogListener {
                                override fun onOkClick(dialog: DialogInterface?) {
                                    notificationEarlier[position]._id?.let {
                                        val list = ArrayList<String>()
                                        list.add(it)
                                        viewModel.deleteNotification(
                                            list
                                        )
                                        notificationEarlier.removeAt(position)
                                        notificationAdapterEarlier.notifyDataSetChanged()
                                    }
                                    dialog?.dismiss()
                                }

                                override fun onCancelClick(dialog: DialogInterface?) {
                                    dialog?.dismiss()
                                }
                            },
                            getString(R.string.are_you_sure_you_want_to_clear_this_your_notifications),
                            true,
                            positiveButtonText = getString(R.string.btn_ok),
                            getString(R.string.btn_cancel),
                            title = getString(R.string.clear_notification),
                        )
                    }
                }
            }

        rv_today.addOnItemTouchListener(touchListener1!!)
        rv_earlier.addOnItemTouchListener(touchListener2!!)
    }

    private fun setObserver() {
        setBaseObservers(viewModel, this, observeToast = false)

        viewModel._notificationLiveData.observe(viewLifecycleOwner) { it ->
            swipe_request.isRefreshing = false
            if (it.totalPage != null) {
                totalPage = it.totalPage
            }
            if (it.nextHit != null) {
                nextHit = it.nextHit
            }
            it.data?.let {
                if (pageNo == 1) {
                    notificationToday.clear()
                    notificationEarlier.clear()
                }

                if (it.today?.size!! == 0 && it.earlier?.size!! == 0) {
                    (activity as NotificationActivity).showClear(false)
                    tvToday.visibility = View.GONE
                    rv_today.visibility = View.GONE
                    tvEarlier.visibility = View.GONE
                    rv_earlier.visibility = View.GONE
                    clNoData.visibility = View.VISIBLE
                } else {
                    (activity as NotificationActivity).showClear(true)
                    clNoData.visibility = View.GONE
                    if (it.today?.size!! > 0) {
                        tvToday.beVisible()
                        rv_today.beVisible()
                        notificationToday.addAll(it.today!!)
                    } else {
                        tvToday.visibility = View.GONE
                        rv_today.visibility = View.GONE
                    }

                    if (it.earlier?.size!! > 0) {
                        tvEarlier.beVisible()
                        rv_earlier.beVisible()
                        notificationEarlier.addAll(it.earlier!!)
                    } else {
                        tvEarlier.visibility = View.GONE
                        rv_earlier.visibility = View.GONE
                    }
                }
                notificationAdapterToday.notifyDataSetChanged()
                notificationAdapterEarlier.notifyDataSetChanged()
            }
        }

        viewModel._deleteNotificationLiveData.observe(viewLifecycleOwner) {
            if(notificationEarlier.isEmpty() && notificationToday.isEmpty()){
                tvEarlier.beGone()
                tvToday.beGone()
                rv_today.beGone()
                rv_earlier.beGone()
                clNoData.beVisible()
            }
        }
    }

    private fun setListener() {
        /*rv_today.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount: Int = rv_today.childCount
                val totalItemCount: Int = rv_today.layoutManager!!.itemCount
                val firstVisibleItem: Int =layoutManager.findFirstVisibleItemPosition()

                if(nextHit>0) {
                    if (visibleItemCount + firstVisibleItem >= totalItemCount) {
                        pageNo += 1
                        progressBar_loadMore.visibility = View.VISIBLE
                        viewModel.getNotification(pageNo)
                    }
                }
            }
        })*/

        swipe_request.setOnRefreshListener {
            pageNo = 1
            viewModel.getNotification(1)
        }
    }

    override fun onSelect(o: Any?, o1: Any?) {
    }

    override fun onUnSelect(o: Any?, o1: Any?) {}

    fun clearAll() {
        showCustomAlertDialog(
            requireContext(),
            object : Listeners.DialogListener {
                override fun onOkClick(dialog: DialogInterface?) {
                    val list = prepareIdList()

                    viewModel.deleteNotification(list)
                    notificationEarlier.clear()
                    notificationToday.clear()
                    notificationAdapterEarlier.notifyDataSetChanged()
                    notificationAdapterToday.notifyDataSetChanged()
                    dialog?.dismiss()
                    (activity as NotificationActivity).showClear(false)
                }

                override fun onCancelClick(dialog: DialogInterface?) {
                    dialog?.dismiss()
                }
            },
            getString(R.string.are_you_sure_you_want_to_clear_all_your_notifications),
            true,
            positiveButtonText = getString(R.string.btn_ok),
            getString(R.string.btn_cancel),
            title = getString(R.string.clear_notifications),
        )

    }

    private fun prepareIdList():ArrayList<String> {
        val list = ArrayList<String>()
        for (i in 0 until notificationToday.size) {
            notificationToday[i]._id?.let { list.add(it) }
        }
        for (i in 0 until notificationEarlier.size) {
            notificationEarlier[i]._id?.let { list.add(it) }
        }
        return list

    }

}