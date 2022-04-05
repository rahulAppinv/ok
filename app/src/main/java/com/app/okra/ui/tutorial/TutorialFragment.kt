package com.app.okra.ui.tutorial

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.okra.R
import com.app.okra.base.BaseFragment
import com.app.okra.base.BaseFragmentWithoutNav
import com.app.okra.base.BaseViewModel
import com.app.okra.models.TutorialModel
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_tutorial.*

private const val DATA = "data"
private const val POSITION = "position"

class TutorialFragment : BaseFragmentWithoutNav() {
    private var data: TutorialModel? = null
    private var position: Int? = null

    override fun getViewModel(): BaseViewModel? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            data = it.getParcelable(DATA)
            position = it.getInt(POSITION)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tutorial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvHeader.text = data?.headerText
        tvSubHeader.text = data?.subText
        ivImage.setImageResource(data?.image!!)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Any, param2: Int) =
            TutorialFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(DATA, param1 as TutorialModel)
                    putInt(POSITION, param2)
                }
            }
    }
}