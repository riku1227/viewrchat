package com.riku1227.viewrchat.util

class VRCUtil {
    companion object {
        fun getInstanceTypeFromInstanceID(instanceID: String): String {
            return if(instanceID.indexOf("hidden") != -1) {
                "FRIENDS+"
            } else if(instanceID.indexOf("friends") != -1)  {
                "FRIENDS"
            }else {
                "PUBLIC"
            }
        }
    }
}