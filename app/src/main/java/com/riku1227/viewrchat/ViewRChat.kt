package com.riku1227.viewrchat

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.riku1227.viewrchat.service.DownloadFileService
import com.riku1227.viewrchat.util.FileUtil
import com.riku1227.viewrchat.util.RadiusOutlineProvider
import com.riku1227.viewrchat.util.SettingsUtil
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.io.File

class ViewRChat: Application() {
    companion object {
        var isPhotographingMode = false
        val imageRadiusOutlineProvider = RadiusOutlineProvider(8F)

        private var downloadService: DownloadFileService? = null

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

        fun getDownloadFileService(): DownloadFileService {
            if(downloadService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://vrchat.com/")
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
                downloadService = retrofit.create(DownloadFileService::class.java)
            }
            return downloadService!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        SettingsUtil.initDayNightTheme(applicationContext)
        isPhotographingMode = SettingsUtil.isPhotographingMode(applicationContext)

        val imageFolder = File(cacheDir, "assets/images")
        if(!imageFolder.exists()) {
            imageFolder.mkdirs()
        }

        val photographingImage = File(imageFolder, "photographing_mode.png")
        if(!photographingImage.exists()) {
            FileUtil.copyFromAssetsToCache(applicationContext, "images/photographing_mode.png", photographingImage)
        }
    }
}