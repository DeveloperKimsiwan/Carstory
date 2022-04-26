package com.like.drive.carstory.ui.search.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.like.drive.carstory.R
import com.like.drive.carstory.analytics.AnalyticsEventLog
import com.like.drive.carstory.common.livedata.SingleLiveEvent
import com.like.drive.carstory.pref.UserPref
import com.like.drive.carstory.ui.base.BaseViewModel
import com.like.drive.carstory.ui.search.data.RecentlyData
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val userPref: UserPref,
    private val analyticsEventLog: AnalyticsEventLog
) :
    BaseViewModel() {

    private val recentlyListPref = userPref.recentlyData

    private val _recentlyList = MutableLiveData<ArrayList<RecentlyData>>()
    val recentlyList: LiveData<ArrayList<RecentlyData>> get() = _recentlyList

    val tagValueEvent = SingleLiveEvent<String?>()
    val tagBlankMessageEvent = SingleLiveEvent<Int>()

    val tag = MutableLiveData<String?>()

    val isSearchStatus = ObservableBoolean(true)

    val searchTagAction: (String?) -> Unit = this::tagListener

    val allRemoveClickEvent = SingleLiveEvent<Unit>()

    init {
        setRecentlyData()
    }

    private fun setRecentlyData() {

        userPref.recentlyData = recentlyListPref

        recentlyListPref.run {
            sortByDescending {
                it.createDate
            }
            _recentlyList.value = this
        }
    }

    fun removeRecentlyData(recentlyData: RecentlyData) {
        if (recentlyListPref.remove(recentlyData)) {
            setRecentlyData()
        }
    }

    fun tagListener(str: String?) {

        if (str.isNullOrBlank()) {
            tagBlankMessageEvent.value = R.string.tag_warning_message
            return
        }

        tag.value = str
        tagValueEvent.value = str
        analyticsEventLog.searchTagEvent(str)
        recentlyListPref.apply {
            find { it.tag == str }?.let {
                remove(it)
            }
            if (add(RecentlyData(str, Date()))) {
                setRecentlyData()
            }
        }
    }

    fun tagAllRemove() {
        recentlyListPref.clear()
        setRecentlyData()
    }
}