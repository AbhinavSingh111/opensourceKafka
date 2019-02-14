package com.airtel.data.feature.item

import com.airtel.data.entities.Item
import com.airtel.data.feature.Repository
import com.airtel.data.model.data.Result
import com.airtel.data.model.data.Success
import com.airtel.data.query.ArchiveQuery

/**
 * @author Vipul Kumar; dated 29/11/18.
 *
 */
class SearchRepository constructor(
    private val localSource: SearchLocalSource,
    private val remoteSource: SearchRemoteSource
) : Repository {

    fun observeSearch(archiveQuery: ArchiveQuery) = localSource.observeSearch(archiveQuery)

    suspend fun updateItemsByCreator(archiveQuery: ArchiveQuery): Result<List<Item>> {
        val result = remoteSource.fetchItemsByCreator(archiveQuery)
        when (result) {
            is Success -> {
                result.data.let {
                    localSource.saveItems(it)
                }
            }
        }
        return result
    }
}
