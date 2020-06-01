package com.riku1227.viewrchat.util

import android.content.Context
import androidx.core.content.ContextCompat
import com.riku1227.viewrchat.R

class VRCUtil {
    companion object {
        const val TRUST_VISITOR = "Visitor"
        const val TRUST_NEW_USER = "New User"
        const val TRUST_USER = "User"
        const val TRUST_KNOWN_USER = "Known User"
        const val TRUST_TRUSTED_USER = "Trusted User"
        const val TRUST_VETERAN_USER = "Trusted User (Veteran)"

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
                tags.contains("system_trust_legend") -> TRUST_VETERAN_USER
                tags.contains("system_trust_veteran") -> TRUST_TRUSTED_USER
                tags.contains("system_trust_trusted") -> TRUST_KNOWN_USER
                tags.contains("system_trust_known") -> TRUST_USER
                tags.contains("system_trust_basic") -> TRUST_NEW_USER
                else -> TRUST_VISITOR
            }
        }
    }
}