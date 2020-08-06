package com.like.drive.motorfeed.ui.gallery.fragment

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.like.drive.motorfeed.R
import com.like.drive.motorfeed.databinding.FragmentGalleryDirectoryBinding
import com.like.drive.motorfeed.ui.base.BaseFragmentDialog
import com.like.drive.motorfeed.ui.gallery.adapter.GalleryDirectoryAdapter
import com.like.drive.motorfeed.ui.gallery.viewmodel.GalleryViewModel
import kotlinx.android.synthetic.main.fragment_gallery_directory.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class GalleryDirectoryFragment : BaseFragmentDialog<FragmentGalleryDirectoryBinding>(R.layout.fragment_gallery_directory) {

    private val galleryViewModel: GalleryViewModel by sharedViewModel()
    private val directoryAdapter: GalleryDirectoryAdapter by lazy {
        GalleryDirectoryAdapter(galleryViewModel)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()
        initView()
    }

    private fun initData() {
        binding.vm = galleryViewModel
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun initView() {
        val decorationItem = DividerItemDecoration(
            rvGalleryDirectory.context, DividerItemDecoration.VERTICAL
        ).apply {
            ContextCompat.getDrawable(
                requireContext(), R.drawable.line_solid_grey_6
            )?.let { setDrawable(it) }
        }
        rvGalleryDirectory?.run {
            adapter = directoryAdapter
            addItemDecoration(decorationItem)
        }

        galleryViewModel.getGalleryDirectory()
    }
}