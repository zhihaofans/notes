package com.zhihaofans.androidbox.util

import android.support.customtabs.CustomTabsIntent

class SystemUtil {
    fun chromeCustomTabs(context: Context, url: String) {
        val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
        val customTabsIntent: CustomTabsIntent = builder.build()
        builder.setToolbarColor(context.getColor(R.color.colorPrimaryDark))
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }
}
