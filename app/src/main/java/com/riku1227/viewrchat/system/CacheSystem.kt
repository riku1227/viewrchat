package com.riku1227.viewrchat.system

import android.content.Context
import com.riku1227.viewrchat.data.CacheTimeData
import com.riku1227.viewrchat.db.CacheTimeDataDB
import com.riku1227.viewrchat.util.FileUtil
import com.riku1227.vrchatlin.VRChatlin
import com.riku1227.vrchatlin.model.VRChatWorld
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
            const val WORLD_JSON = "world_json"
        }
    }
    companion object {
        private val httpClient = OkHttpClient.Builder().build()

        const val DB_CLEAR_TIME = 604800 //one weak

        private const val WORLD_IMAGE_CACHE = "world_image/"
        private const val WORLD_IMAGE_CACHE_TIME = 604800 //one week

        private const val WORLD_JSON_CACHE = "world_json/"
        private const val WORLD_JSON_CACHE_TIME = 604800 //one week

        private fun getCacheDir(context: Context, cacheType: String): File {
            val dirName = when(cacheType) {
                CacheType.WORLD_IMAGE -> WORLD_IMAGE_CACHE
                CacheType.WORLD_JSON -> WORLD_JSON_CACHE

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
            val removeTypeID = id.replace("${cacheType}_", "")
            val expiredTime = when (cacheType) {
                CacheType.WORLD_IMAGE -> WORLD_IMAGE_CACHE_TIME
                CacheType.WORLD_JSON -> WORLD_JSON_CACHE_TIME
                else -> WORLD_IMAGE_CACHE_TIME
            }
            CacheTimeDataDB.getInstance(context).readData("${cacheType}_$removeTypeID")?.let {
                val nowTime = System.currentTimeMillis() / 1000
                val diffTime = nowTime - it.cacheTime
                return diffTime >= expiredTime
            }
            return true
        }

        fun loadImage(context: Context, cacheType: String, id: String, url: String): Single<File> {
            return Single.create {
                val removeTypeID = id.replace("${cacheType}_", "")
                val cacheFile = File(getCacheDir(context, cacheType), removeTypeID)
                if(!cacheFile.exists() || isExpiredCache(context, removeTypeID, cacheType)) {
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
                                CacheTimeDataDB.getInstance(context).saveData( CacheTimeData("${cacheType}_$removeTypeID", cacheType, System.currentTimeMillis() / 1000) )
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

        fun loadVRChatWorld(context: Context, id: String): Single<VRChatWorld> {
            return Single.create {
                val removeTypeID = id.replace("${CacheType.WORLD_JSON}_", "")
                val cacheFile = File(getCacheDir(context, CacheType.WORLD_JSON), removeTypeID)
                if(!cacheFile.exists() || isExpiredCache(context, removeTypeID, CacheType.WORLD_JSON)) {
                    VRChatlin.get(context).APIService().getWorldByID(removeTypeID).subscribe(
                        { world ->
                            FileUtil.encodeJsonToFile(context, cacheFile, world)
                            CacheTimeDataDB.getInstance(context).saveData( CacheTimeData("${CacheType.WORLD_JSON}_$removeTypeID", CacheType.WORLD_JSON, System.currentTimeMillis() / 1000) )
                            it.onSuccess(world)
                        },
                        { error ->
                            it.onError(error)
                        }
                    )
                } else {
                    val json = FileUtil.decodeFileToJson(context, cacheFile, VRChatWorld::class.java)
                    if(json != null) {
                        it.onSuccess(json)
                    } else {
                        it.onError(Throwable("Json Decode Error"))
                    }
                }
            }
        }

        fun deleteCacheFile(context: Context, id: String, cacheType: String) {
            val removeTypeID = id.replace("${cacheType}_", "")
            val cacheFile = File(getCacheDir(context, cacheType), removeTypeID)
            if(cacheFile.exists()) {
                cacheFile.delete()
            }
        }
    }
}