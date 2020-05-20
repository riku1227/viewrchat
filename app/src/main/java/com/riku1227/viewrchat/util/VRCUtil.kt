package com.riku1227.viewrchat.util

import android.content.Context
import androidx.core.content.ContextCompat
import com.riku1227.viewrchat.R

class VRCUtil {
    companion object {
        fun getInstanceTypeFromInstanceID(instanceID: String): String {
            return if(instanceID.indexOf("hidden") != -1) {
                "FRIENDS+"
            } else if(instanceID.indexOf("friends") != -1)  {
                "FRIENDS"
            }else {
                "PUBLIC"
            }
        }

        fun getLastLoginPlatform(platform: String?): String {
            return when(platform) {
                "standalonewindows" -> "Windows"
                "android" -> "Quest"
                null -> "NULL"
                else -> "Other"
            }
        }

        fun getStatusIconColor(context: Context, status: String?): Int {
            return when(status) {
                "active" -> ContextCompat.getColor(context, R.color.statusIconColorOnline)
                "join me" -> ContextCompat.getColor(context, R.color.statusIconColorJoinMe)
                "ask me" -> ContextCompat.getColor(context, R.color.statusIconColorAskMe)
                "busy" -> ContextCompat.getColor(context, R.color.statusIconColorDND)
                else -> ContextCompat.getColor(context, R.color.statusIconColorOffline)
            }
        }

        fun getTrustRank(tags: List<String>): String {
            return when {
                tags.contains("system_trust_legend") -> "Trusted User (Veteran)"
                tags.contains("system_trust_veteran") -> "Trusted User"
                tags.contains("system_trust_trusted") -> "Known User"
                tags.contains("system_trust_known") -> "User"
                tags.contains("system_trust_basic") -> "New User"
                else -> "Visitor"
            }
        }
    }
}