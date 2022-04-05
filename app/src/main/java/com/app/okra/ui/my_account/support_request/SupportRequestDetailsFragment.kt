package com.app.okra.ui.my_account.support_request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.okra.R
import com.app.okra.base.BaseFragment
import com.app.okra.base.BaseViewModel
import com.app.okra.data.repo.SupportRequestRepoImpl
import com.app.okra.extension.viewModelFactory
import com.app.okra.models.SupportResponse
import kotlinx.android.synthetic.main.fragment_support_request_details.*

/**
 * A fragment representing a list of Items.
 */
class SupportRequestDetailsFragment : BaseFragment(){


    private var supportResponseData: SupportResponse?=null
    private lateinit var layoutManager: LinearLayoutManager
    private val requestList  = ArrayList<SupportResponse>()

    private val viewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory {
            SupportRequestViewModel(SupportRequestRepoImpl(apiServiceAuth))
        }).get(SupportRequestViewModel::class.java)
    }

    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    companion object {
        const val DATA="DATA"
    }

    override fun onResume() {
        super.onResume()
        if(requireActivity() is SupportRequestActivity){
            (requireActivity() as SupportRequestActivity)
                    .setTitle(getString(R.string.title_my_support_request_details))
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_support_request_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData()
        setViews()
    }

    private fun getData() {
        supportResponseData = arguments?.getParcelable(DATA)
    }

    private fun setViews() {
        supportResponseData?.let{ it ->
            tvTitle.text = it.title?:""
            tvDescription.text = it.description?:""

        }
    }

}