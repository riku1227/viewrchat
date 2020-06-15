package com.riku1227.viewrchat.service

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface DownloadFileService {
    @GET
    fun downloadFile(@Url fileUrl: String): Single<ResponseBody>
}