package com.riku1227.viewrchat.util

import com.riku1227.viewrchat.data.MinimumVRCUser
import com.riku1227.vrchatlin.model.VRChatUser

fun VRChatUser.toMinimum(): MinimumVRCUser {
    return MinimumVRCUser(
        this.id,
        this.username,
        this.displayName,
        this.currentAvatarThumbnailImageUrl,
        this.location,
        this.status,
        this.statusDescription,
        this.last_platform,
        this.tags
    )
}