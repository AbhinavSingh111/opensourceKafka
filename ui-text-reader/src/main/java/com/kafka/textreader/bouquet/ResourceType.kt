package com.kafka.textreader.bouquet

import android.net.Uri
import android.os.Parcelable

sealed class ResourceType {
    data class Local(val uri: Uri) : ResourceType()

    data class Remote(val url: String) : ResourceType()

    data class Base64(val file: String) : ResourceType()
}
