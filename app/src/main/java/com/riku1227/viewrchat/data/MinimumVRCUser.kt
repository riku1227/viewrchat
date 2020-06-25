package com.riku1227.viewrchat.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MinimumVRCUser(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarThumbnailImageUrl: String,
    val location: String?,
    val status: String?,
    val statusDescription: String?,
    val last_platform: String?,
    val tags: List<String>
): Parcelable