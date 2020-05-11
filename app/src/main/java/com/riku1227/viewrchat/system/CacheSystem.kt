package com.riku1227.viewrchat.system

import android.content.Context
import com.riku1227.viewrchat.data.CacheTimeData
import com.riku1227.viewrchat.db.CacheTimeDataDB
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException

class CacheSystem {
    class CacheType {
        companion object {
            const val WORLD_IMAGE = "world_image"
        }
    }
    companion object {
        private val httpClient = OkHttpClient.Builder().build()

        const val DB_CLEAR_TIME = 604800 //one weak

        private const val WORLD_IMAGE_CACHE = "world_image/"
        private const val WORLD_IMAGE_CACHE_TIME = 604800 //one week

        private fun getCacheDir(context: Context, cacheType: String): File {
            val dirName = when(cacheType) {
                CacheType.WORLD_IMAGE -> WORLD_IMAGE_CACHE
                else -> WORLD_IMAGE_CACHE
            }
            File(context.cacheDir, dirName).let {
                if(!it.exists()) {
                    it.mkdirs()
                }

                return it
            }
        }

        private fun isExpiredCache(context: Context, id: String, cacheType: String): Boolean {
            val expiredTime = when (cacheType) {
                CacheType.WORLD_IMAGE -> WORLD_IMAGE_CACHE_TIME
                else -> WORLD_IMAGE_CACHE_TIME
            }
            CacheTimeDataDB.getInstance(context).readData(id)?.let {
                val nowTime = System.currentTimeMillis() / 1000
                val diffTime = nowTime - it.cacheTime
                return diffTime >= expiredTime
            }
            return true
        }

        fun loadImage(context: Context, cacheType: String, id: String, url: String): Single<File> {
            return Single.create {
                val cacheFile = File(getCacheDir(context, cacheType), id)
                if(!cacheFile.exists() || isExpiredCache(context, id, cacheType)) {
                    val request = Request.Builder().url(url).build()
                    try {
                        val response = httpClient.newCall(request).execute()
                        if(response.isSuccessful) {
                            val responseBody = response.body
                            if(responseBody != null) {
                                cacheFile.sink().buffer().let { bufferedSink ->
                                    bufferedSink.writeAll(responseBody.source())
                                    bufferedSink.flush()
                                    bufferedSink.close()
                                }
                                CacheTimeDataDB.getInstance(context).saveData( CacheTimeData(id, cacheType, System.currentTimeMillis() / 1000) )
                                it.onSuccess(cacheFile)
                            } else {
                                it.onError(Throwable("Response body is null"))
                            }
                        } else {
                            it.onError(Throwable("Response is not successful"))
                        }
                    } catch (e: IOException) {
                        it.onError(e)
                    }
                }
                it.onSuccess(cacheFile)
            }
        }

        fun deleteCacheFile(context: Context, id: String, cacheType: String) {
            val cacheFile = File(getCacheDir(context, cacheType), id)
            if(cacheFile.exists()) {
                cacheFile.delete()
            }
        }
    }
}