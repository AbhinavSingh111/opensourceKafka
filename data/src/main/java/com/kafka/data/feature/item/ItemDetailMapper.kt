package com.kafka.data.feature.item

import android.text.Html
import com.kafka.data.dao.FileDao
import com.kafka.data.entities.File.Companion.supportedExtensions
import com.kafka.data.entities.ItemDetail
import com.kafka.data.model.item.File
import com.kafka.data.model.item.ItemDetailResponse
import org.kafka.base.debug
import javax.inject.Inject

class ItemDetailMapper @Inject constructor(
    private val fileDao: FileDao,
    private val fileMapper: FileMapper
) {
    suspend fun map(from: ItemDetailResponse): ItemDetail {
        debug { "mapping item detail: ${from.files.joinToString { it.name }}" }
        return ItemDetail(
            itemId = from.metadata.identifier,
            language = from.metadata.licenseurl,
            title = from.metadata.title?.dismissUpperCase(),
            description = (from.metadata.description?.joinToString()?.format() ?: ""),
            creator = from.metadata.creator?.joinToString(),
            collection = from.metadata.collection?.joinToString(),
            mediaType = from.metadata.mediatype,
            files = from.files.filter { it.fileId.isNotEmpty() }.map { it.fileId },
            coverImage = from.findCoverImage(),
            metadata = from.metadata.collection,
            primaryTextFile = from.files.getTextFile()?.fileId
        ).also {
            insertFiles(from, it)
        }
    }

    private fun List<File>.getTextFile() = firstOrNull { it.name.endsWith("pdf", true) }
        ?: firstOrNull { it.name.endsWith("epub", true) }
        ?: firstOrNull { it.name.endsWith("txt", true) }


    private fun ItemDetailResponse.findCoverImage() = files.firstOrNull {
        it.name.startsWith("cover_1")
                && (it.name.endsWith("jpg") || it.name.endsWith("png"))
    }?.let { "https://archive.org/download/${metadata.identifier}/${it.name}" }
        ?: "https://archive.org/services/img/${metadata.identifier}"

    private suspend fun insertFiles(from: ItemDetailResponse, item: ItemDetail) {
        val files = from.files.map {
            fileMapper.map(
                file = it,
                itemId = from.metadata.identifier,
                itemTitle = item.title,
                prefix = from.dirPrefix(),
                localUri = fileDao.fileOrNull(it.name)?.localUri,
                coverImage = item.coverImage
            )
        }.filter { supportedExtensions.contains(it.extension) }

        fileDao.insertAll(files)
    }
}

fun String?.format() = Html.fromHtml(this, 0)?.toString()

fun ItemDetailResponse.dirPrefix() = "https://$server$dir"

