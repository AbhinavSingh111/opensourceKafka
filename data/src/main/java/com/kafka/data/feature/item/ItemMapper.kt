package com.kafka.data.feature.item

import com.kafka.data.entities.Creator
import com.kafka.data.entities.Item
import com.kafka.data.model.item.Doc
import com.kafka.data.model.item.ItemDetailResponse
import com.kafka.data.model.item.SearchResponse
import javax.inject.Inject

class ItemMapper @Inject constructor() {
     fun map(from: SearchResponse): List<Item> {
        return from.response!!.docs.map { doc -> doc.toItem() }
    }

    private fun Doc.toItem() = Item(
        itemId = this.identifier,
        language = this.language,
        title = this.title.joinToString().dismissUpperCase(),
        description = this.description?.get(0)?.trim(),
        creator = this.creator?.get(0)?.sanitizeForRoom()?.let { Creator(it, it) },
        mediaType = this.mediatype,
        coverImage = "https://archive.org/services/img/$identifier",
        collection = this.collection,
        genre = this.subject,
        position = this.downloads,
        subject = subject?.joinToString(","),
        rating = this.rating
    )
}

fun String.dismissUpperCase() = if (this.isUpperCase()) {
    this.lowercase().replaceFirstChar { it.uppercase() }
} else {
    this
}

fun String.sanitizeForRoom() = this.replace("'", "")

fun String.isUpperCase() = toCharArray().all { it.isUpperCase() }
