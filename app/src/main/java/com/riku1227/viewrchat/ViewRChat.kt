package com.riku1227.viewrchat

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.riku1227.viewrchat.util.RadiusOutlineProvider

class ViewRChat {
    companion object {
        val imageRadiusOutlineProvider = RadiusOutlineProvider(8F)

        fun getVRChatCookiePreferences(context: Context): SharedPreferences {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            return EncryptedSharedPreferences.create(
                "vrchat_cookie",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }

        fun getGeneralPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences("general_preference", Context.MODE_PRIVATE)
        }
    }
}