package com.like.drive.motorfeed.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment


abstract class BaseFragmentDialog<V : ViewDataBinding>(
    @get:LayoutRes val layoutId: Int
) : DialogFragment() {
    private lateinit var binding: V


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this,object :OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? =
        DataBindingUtil.inflate<V>(inflater, layoutId, container, false).apply {
            onBindBefore(this)
            onBind(this)
            onBindAfter(this)
        }.root

    protected open fun onBindBefore(dataBinding :V) = Unit
    protected open fun onBind(dataBinding :V) = dataBinding.run {
        executePendingBindings()
        this.lifecycleOwner=this@BaseFragmentDialog
        this@BaseFragmentDialog.binding = this
    }
    protected open fun onBindAfter(dataBinding :V) = Unit

    val intent: Intent? get() = activity?.intent


    protected fun binding(action: V.() -> Unit) {
        binding.run(action)
    }


    protected open fun onBackPressed(): Boolean = false
}