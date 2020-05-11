package com.riku1227.viewrchat.system

import android.content.Context
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException

class CacheSystem {
    enum class CacheType {
        WORLD_IMAGE
    }
    companion object {
        private val httpClient = OkHttpClient.Builder().build()
        private const val WORLD_IMAGE_CACHE = "world_image/"

        private fun getCacheDir(context: Context, cacheType: CacheType): File {
            val dirName = when(cacheType) {
                CacheType.WORLD_IMAGE -> WORLD_IMAGE_CACHE
            }
            File(context.cacheDir, dirName).let {
                if(!it.exists()) {
                    it.mkdirs()
                }

                return it
            }
        }

        fun loadImage(context: Context, cacheType: CacheType, cacheFileName: String, url: String): Single<File> {
            return Single.create {
                val cacheFile = File(getCacheDir(context, cacheType), cacheFileName)
                if(!cacheFile.exists()) {
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
    }
}