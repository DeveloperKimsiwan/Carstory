package com.like.drive.motorfeed.ui.upload.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.like.drive.motorfeed.databinding.HolderUploadPhotoBinding
import com.like.drive.motorfeed.data.PhotoData
import com.like.drive.motorfeed.ui.upload.viewmodel.UploadViewModel

class UploadPhotoHolder(val binding:HolderUploadPhotoBinding):RecyclerView.ViewHolder(binding.root){

    fun bind(item: PhotoData, vm: UploadViewModel) {
        binding.item = item
        binding.vm= vm
    }

    companion object {
        fun from(parent: ViewGroup): UploadPhotoHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = HolderUploadPhotoBinding.inflate(layoutInflater, parent, false)

            return UploadPhotoHolder(binding)
        }
    }

}