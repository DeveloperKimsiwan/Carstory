package com.like.drive.motorfeed.ui.notification.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.like.drive.motorfeed.R
import com.like.drive.motorfeed.data.notification.NotificationType
import com.like.drive.motorfeed.databinding.FragmentNotificationBinding
import com.like.drive.motorfeed.ui.base.BaseFragment
import com.like.drive.motorfeed.ui.feed.detail.activity.FeedDetailActivity
import com.like.drive.motorfeed.ui.feed.list.fragment.FeedListFragment
import com.like.drive.motorfeed.ui.main.activity.MainActivity
import com.like.drive.motorfeed.ui.notification.adapter.NotificationAdapter
import com.like.drive.motorfeed.ui.notification.viewmodel.NotificationViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_notification.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class NotificationFragment :
    BaseFragment<FragmentNotificationBinding>(R.layout.fragment_notification) {

    private val viewModel: NotificationViewModel by viewModel()
    private val adapter by lazy { NotificationAdapter(viewModel) }

    override fun onBind(dataBinding: FragmentNotificationBinding) {
        super.onBind(dataBinding)
        withViewModel()
        dataBinding.rvNotification.apply {
            adapter = this@NotificationFragment.adapter

            val dividerItemDecoration =
                DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL).apply {
                    ContextCompat.getDrawable(requireContext(), R.drawable.divider_feed_list)?.let {
                        setDrawable(it)
                    }
                }

            addItemDecoration(dividerItemDecoration)

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.init()

        (requireActivity() as MainActivity).navBottomView.removeBadge(R.id.action_notification)
    }

    private fun withViewModel() {

        with(viewModel) {
            getList()
            moveToPage()
        }
    }

    private fun NotificationViewModel.getList() {
        notificationList.observe(viewLifecycleOwner, Observer {
            adapter.apply {
                list.clear()
                list.addAll(it)
                notifyDataSetChanged()
            }
        })
    }

    private fun NotificationViewModel.moveToPage() {
        clickItemEvent.observe(viewLifecycleOwner, Observer {
            when (it.notificationType) {
                NotificationType.COMMENT.value, NotificationType.RE_COMMENT.value -> {
                    startAct(FeedDetailActivity::class, Bundle().apply {
                        putString(FeedDetailActivity.KEY_FEED_ID, it.fid)
                    })
                }
            }
        })
    }

}