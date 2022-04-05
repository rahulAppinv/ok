package com.app.okra.ui.my_account.support_request

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.app.okra.R
import com.app.okra.base.BaseFragment
import com.app.okra.base.BaseViewModel
import com.app.okra.data.repo.SupportRequestRepoImpl
import com.app.okra.extension.*
import com.app.okra.ui.Message2Activity
import com.app.okra.ui.connected_devices.BluetoothActivity
import com.app.okra.utils.AppConstants.Companion.SCREEN_TYPE
import kotlinx.android.synthetic.main.fragment_add_support_request.*
import kotlinx.android.synthetic.main.layout_button.*

/**
 * A fragment representing a list of Items.
 */
class AddSupportRequestFragment : BaseFragment(),
    View.OnClickListener {


    private val screenType by lazy {
        (requireActivity() as SupportRequestActivity).screenType
    }
    private val viewModel by lazy {
        ViewModelProvider(requireActivity(), viewModelFactory {
            SupportRequestViewModel(SupportRequestRepoImpl(apiServiceAuth))
        }).get(SupportRequestViewModel::class.java)
    }

    override fun getViewModel(): BaseViewModel? {
        return viewModel
    }

    companion object {
        @JvmStatic
        fun newInstance() = AddSupportRequestFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(requireActivity() is SupportRequestActivity){
            (requireActivity() as SupportRequestActivity).setTitle(getString(R.string.title_new_support_request))

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_support_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
        setObserver()
        setListener()
    }


    private fun setObserver() {
        setBaseObservers(viewModel, this, observeToast = false)
        viewModel._sendSupportLiveData.observe(viewLifecycleOwner) { it ->
            it.getContent()?.let {
                viewModel.updateListAfterAdd()
                requireActivity().navigate(Intent(requireActivity(), Message2Activity::class.java).apply {
                    putExtra(SCREEN_TYPE, AddSupportRequestFragment::class.java.simpleName)
                })

                if(!screenType.isNullOrEmpty() && screenType == BluetoothActivity::class.java.simpleName){
                    requireActivity().finish()
                }else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        navController.popBackStack()
                    }, 300)
                }
            }
        }

        viewModel._toastObserver.observe(viewLifecycleOwner){
            val data = it.getContent()!!
            showToast(data.message)

            tvErrorTitle.beGone()
            tvErrorDescription.beGone()
            etDescription.setNormalView(requireActivity())
            etTitle.setNormalView(requireActivity())

            if(!checkAndLogout(data.message)){
                when(data.type) {
                    SupportRequestViewModel.TITLE-> {
                        tvErrorTitle.text = data.message
                        tvErrorTitle.beVisible()
                        etTitle.setErrorView(requireActivity())
                    }
                    else-> {
                        tvErrorDescription.text = data.message
                        tvErrorDescription.beVisible()
                        etDescription.setErrorView(requireActivity())
                    }
                }
            }
        }
    }


    private fun setListener() {
        btnCommon.setOnClickListener(this)
    }


    private fun setViews() {
        btnCommon.text = getString(R.string.btn_submit)
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.btnCommon -> {
                viewModel.setRequest(
                    title = etTitle.text.toString().trim(),
                    description = etDescription.text.toString().trim())
                viewModel.sendSupportRequestApi()
            }
        }
    }

}