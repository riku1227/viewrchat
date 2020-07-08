package com.riku1227.viewrchat.util

import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import androidx.core.content.ContextCompat
import com.riku1227.viewrchat.R
import java.text.SimpleDateFormat
import java.util.*

class VRCUtil {
    companion object {
        const val TRUST_VISITOR = "Visitor"
        const val TRUST_NEW_USER = "New User"
        const val TRUST_USER = "User"
        const val TRUST_KNOWN_USER = "Known User"
        const val TRUST_TRUSTED_USER = "Trusted User"
        const val TRUST_VETERAN_USER = "Trusted User (Veteran)"

        fun getInstanceTypeFromInstanceID(instanceID: String): String {
            return when {
                instanceID.indexOf("hidden") != -1 -> {
                    "FRIENDS+"
                }
                instanceID.indexOf("friends") != -1 -> {
                    "FRIENDS"
                }
                else -> {
                    "PUBLIC"
                }
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

        fun getLanguagesList(tags: List<String>): List<String> {
            val result = arrayListOf<String>()

            for (value in tags) {
                when (value.replace("language_", "")) {
                    "eng" -> result.add("[ eng ] English")
                    "kor" -> result.add("[ kor ] 한국어")
                    "rus" -> result.add("[ rus ] Русский")
                    "spa" -> result.add("[ spa ] Español")
                    "por" -> result.add("[ por ] Português")
                    "zho" -> result.add("[ zho ] 中文")
                    "deu" -> result.add("[ deu ] Deutsch")
                    "jpn" -> result.add("[ jpn ] 日本語")
                    "fra" -> result.add("[ fra ] Français")
                    "swe" -> result.add("[ swe ] Svenska")
                    "nld" -> result.add("[ nld ] Nederlands")
                    "pol" -> result.add("[ pol ] Polski")
                    "dan" -> result.add("[ dan ] Dansk")
                    "nor" -> result.add("[ nor ] Norsk")
                    "ita" -> result.add("[ ita ] Italiano")
                    "tha" -> result.add("[ tha ] ภาษาไทย")
                    "fin" -> result.add("[ fin ] Suomi")
                    "hun" -> result.add("[ hun ] Magyar")
                    "ces" -> result.add("[ ces ] Čeština")
                    "tur" -> result.add("[ tur ] Türkçe")
                    "ara" -> result.add("[ ara ] العربية")
                    "ase" -> result.add("[ ase ] American Sign Language")
                    "bfi" -> result.add("[ bfi ] British Sign Language")
                    "dse" -> result.add("[ dse ] Dutch Sign Language")
                    "fsl" -> result.add("[ fsl ] French Sign Language")
                    "kvk" -> result.add("[ kvk ] Korean Sign Language")
                }
            }

            return result
        }

        fun getWorldTagList(tagList: List<String>): List<String> {
            val result = arrayListOf<String>()

            for(value in tagList) {
                when  {
                    value.indexOf("author_tag_") != -1 -> result.add( value.replace("author_tag_", "") )
                }
            }

            return result
        }

        fun getPublishTime(dateString: String): String {
            return if(dateString == "none") {
                "NONE"
            } else {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
                val date = sdf.parse(dateString)

                if(date == null) {
                    "ERROR"
                } else {
                    SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH).format(date)
                }
            }
        }
    }
}