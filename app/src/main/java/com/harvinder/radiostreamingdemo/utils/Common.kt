package com.harvinder.radiostreamingdemo.utils

import android.content.Context
import android.net.ConnectivityManager

class Common {

    companion object {
        fun isNetworkAvailable(context: Context?): Boolean {
            var outcome = false
            if (context != null) {
                val cm = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfos = cm.allNetworkInfo
                for (tempNetworkInfo in networkInfos) {
                    if (tempNetworkInfo.isConnected) {
                        outcome = true
                        break
                    }
                }
            }
            return outcome
        }
    }
}