package com.like.drive.motorfeed.ui.upload.holder

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.like.drive.motorfeed.R
import com.like.drive.motorfeed.databinding.HolderUploadPhotoBinding
import com.like.drive.motorfeed.ui.base.ext.showShortToast
import com.like.drive.motorfeed.ui.upload.data.PhotoData
import com.like.drive.motorfeed.ui.upload.viewmodel.UploadViewModel
import com.like.drive.motorfeed.util.photo.PickImageUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UploadPhotoHolder(val binding:HolderUploadPhotoBinding):RecyclerView.ViewHolder(binding.root),
    LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private var wasPaused: Boolean = false
    private val context = binding.root.context

    fun bind(activity: Activity, item: PhotoData, vm: UploadViewModel) {

        item.uri?.let {
            binding.progress.visibility = View.VISIBLE

            lifecycle.coroutineScope.launch {

                withContext(Dispatchers.IO) {

                    PickImageUtil.createUriImageFile(activity, it)?.let {

                        withContext(Dispatchers.IO) { PickImageUtil.setImage(it.path) }

                        vm.addFile(it)

                    }?:makeFileError(vm)

                }
                binding.progress.visibility = View.GONE
            }
        }

        binding.item = item
        binding.vm= vm
        binding.position =adapterPosition

    }

    private fun makeFileError(vm:UploadViewModel){
        vm.deletePhoto(adapterPosition)
        context.showShortToast(context.getString(R.string.error_make_file))
    }

    companion object {
        fun from(parent: ViewGroup): UploadPhotoHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = HolderUploadPhotoBinding.inflate(layoutInflater, parent, false)

            return UploadPhotoHolder(binding)
        }
    }

    fun lifeCycleCreated() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    fun lifeCycleAttach() {
        if (wasPaused) {
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
            wasPaused = false
        } else {
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
        }
    }

    fun lifeCycleDetach() {
        wasPaused = true
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    fun lifeCycleDestroyed() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }


    override fun getLifecycle(): Lifecycle  = lifecycleRegistry
}