package com.riku1227.viewrchat.system

import android.content.Context
import com.riku1227.viewrchat.ViewRChat
import com.riku1227.viewrchat.data.CacheTimeData
import com.riku1227.viewrchat.db.CacheTimeDataDB
import com.riku1227.viewrchat.util.FileUtil
import com.riku1227.vrchatlin.VRChatlin
import com.riku1227.vrchatlin.model.VRChatUser
import com.riku1227.vrchatlin.model.VRChatWorld
import io.reactivex.Single
import okio.buffer
import okio.sink
import java.io.File

class CacheSystem {
    class CacheType {
        companion object {
            const val WORLD_IMAGE = "world_image"
            const val WORLD_JSON = "world_json"
            const val USER_AVATAR_IMAGE = "user_avatar_image"
            const val USER_JSON = "user_json"
        }
    }
    companion object {
        const val VRC_ASSETS_PRIVATE_WORLD_IMAGE_URL = "https://assets.vrchat.com/www/images/default_private_image.png"

        const val DB_CLEAR_TIME = 604800 //one weak

        private const val WORLD_IMAGE_CACHE = "world_image/"
        private const val WORLD_IMAGE_CACHE_TIME = 604800 //one week

        private const val WORLD_JSON_CACHE = "world_json/"
        private const val WORLD_JSON_CACHE_TIME = 604800 //one week

        private const val USER_AVATAR_IMAGE = "user_avatar_image/"
        private const val USER_AVATAR_IMAGE_TIME = 300 //5 minute

        private const val USER_JSON_CACHE = "user_json/"
        private const val USER_JSON_CACHE_TIME = 259200 //3 day
        const val LOGIN_USER_ID = "login_user"

        private fun getCacheDir(context: Context, cacheType: String): File {
            val dirName = when(cacheType) {
                CacheType.WORLD_IMAGE -> WORLD_IMAGE_CACHE
                CacheType.WORLD_JSON -> WORLD_JSON_CACHE
                CacheType.USER_AVATAR_IMAGE -> USER_AVATAR_IMAGE
                CacheType.USER_JSON -> USER_JSON_CACHE

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
                CacheType.USER_AVATAR_IMAGE -> USER_AVATAR_IMAGE_TIME
                CacheType.USER_JSON -> USER_JSON_CACHE_TIME
                else -> WORLD_IMAGE_CACHE_TIME
            }
            CacheTimeDataDB.getInstance(context).readData("${cacheType}_$removeTypeID")?.let {
                val nowTime = System.currentTimeMillis() / 1000
                val diffTime = nowTime - it.cacheTime
                return diffTime >= expiredTime
            }
            return true
        }

        fun loadImage(context: Context, cacheType: String, id: String, url: String, notUpdate: Boolean = false): Single<File> {
            return Single.create {
                if(ViewRChat.isPhotographingMode) {
                    val imageFolder = File(context.cacheDir, "assets/images")
                    if(!imageFolder.exists()) {
                        imageFolder.mkdirs()
                    }

                    val photographingImage = File(imageFolder, "photographing_mode.png")
                    if(!photographingImage.exists()) {
                        FileUtil.copyFromAssetsToCache(context, "images/photographing_mode.png", photographingImage)
                    }

                    it.onSuccess(photographingImage)
                } else {
                    val removeTypeID = id.replace("${cacheType}_", "")
                    val cacheFile = File(getCacheDir(context, cacheType), removeTypeID)
                    val isUpdate: Boolean = if(notUpdate) {
                        false
                    } else isExpiredCache(context, removeTypeID, cacheType)
                    if(!cacheFile.exists() || isUpdate) {
                        val result = ViewRChat.getDownloadFileService().downloadFile(url).subscribe(
                            { responseBody ->
                                cacheFile.sink().buffer().let { bufferedSink ->
                                    bufferedSink.writeAll(responseBody.source())
                                    bufferedSink.flush()
                                    bufferedSink.close()
                                }
                                CacheTimeDataDB.getInstance(context).saveData( CacheTimeData("${cacheType}_$removeTypeID", cacheType, System.currentTimeMillis() / 1000) )
                                it.onSuccess(cacheFile)
                            },
                            { error ->
                                if(!it.isDisposed) {
                                    it.onError(error)
                                }
                            }
                        )
                    }
                    it.onSuccess(cacheFile)
                }
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
                            if(!it.isDisposed) {
                                it.onError(error)
                            }
                        }
                    )
                } else {
                    val json = FileUtil.decodeFileToJson(context, cacheFile, VRChatWorld::class.java)
                    if(json != null) {
                        it.onSuccess(json)
                    } else {
                        if(!it.isDisposed) {
                            it.onError(Throwable("Json Decode Error"))
                        }
                    }
                }
            }
        }

        fun loadVRChatUser(context: Context, id: String, isUpdate: Boolean = false): Single<VRChatUser> {
            return Single.create {
                val removeTypeID = id.replace("${CacheType.USER_JSON}_", "")
                val cacheFile = File(getCacheDir(context, CacheType.USER_JSON), removeTypeID)
                if(!cacheFile.exists() || isUpdate || isExpiredCache(context, removeTypeID, CacheType.USER_JSON)) {
                    if(removeTypeID == LOGIN_USER_ID) {
                        VRChatlin.get(context).APIService().getCurrentUserInfo().subscribe(
                            { user ->
                                FileUtil.encodeJsonToFile(context, cacheFile, user)
                                CacheTimeDataDB.getInstance(context).saveData( CacheTimeData("${CacheType.USER_JSON}_$removeTypeID", CacheType.USER_JSON, System.currentTimeMillis() / 1000) )
                                it.onSuccess(user)
                            },
                            { error ->
                                if(!it.isDisposed) {
                                    it.onError(error)
                                }
                            }
                        )
                    } else {
                        VRChatlin.get(context).APIService().getUserByID(removeTypeID).subscribe(
                            { user ->
                                FileUtil.encodeJsonToFile(context, cacheFile, user)
                                CacheTimeDataDB.getInstance(context).saveData( CacheTimeData("${CacheType.USER_JSON}_$removeTypeID", CacheType.USER_JSON, System.currentTimeMillis() / 1000) )
                                it.onSuccess(user)
                            },
                            { error ->
                                if(!it.isDisposed) {
                                    it.onError(error)
                                }
                            }
                        )
                    }
                } else {
                    val json = FileUtil.decodeFileToJson(context, cacheFile, VRChatUser::class.java)
                    if(json != null) {
                        it.onSuccess(json)
                    } else {
                        if(!it.isDisposed) {
                            it.onError(Throwable("Json Decode Error"))
                        }
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

        fun deleteCacheFolder(context: Context, cacheType: String) {
            getCacheDir(context, cacheType).deleteRecursively()
        }
    }
}