package com.riku1227.viewrchat.util

import android.content.Context
import com.riku1227.vrchatlin.VRChatlin
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.nio.charset.StandardCharsets

class FileUtil {
    companion object {
        fun <T : Any>  encodeJsonToFile(context: Context, output: File, jsonObject: T) {
            val jsonStr = VRChatlin.get(context).getMoshi().adapter(jsonObject.javaClass).toJson(jsonObject)
            output.sink().buffer().let {
                it.writeString(jsonStr, StandardCharsets.UTF_8)
                it.flush()
                it.close()
            }
        }

        fun <T : Any> decodeFileToJson(context: Context, input: File, jsonObject: Class<T>) : T? {
            var jsonText: String
            input.source().buffer().let {
                jsonText = it.readUtf8()
                it.close()
            }

            return VRChatlin.get(context).getMoshi().adapter(jsonObject).fromJson(jsonText)
        }

        fun copyFromAssetsToCache(context: Context, fileName: String, outputFile: File) {
            val inputStream = context.assets.open(fileName)
            outputFile.sink().buffer().let {
                it.writeAll(inputStream.source())
                it.flush()
                it.close()
            }
        }
    }
}