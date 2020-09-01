package com.like.drive.motorfeed.ui.home.data

import com.like.drive.motorfeed.MotorFeedApplication
import com.like.drive.motorfeed.R

enum class HomeTab(val resId:Int) {
    NEWS_FEED(R.string.home_news_feed),
    FILTER(R.string.home_filter);

    fun getTitle():String = MotorFeedApplication.getContext().getString(resId)
}