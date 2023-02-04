package org.kafka.domain.observers

import com.kafka.data.feature.item.ItemWithDownload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.kafka.base.AppCoroutineDispatchers
import org.kafka.base.domain.SubjectInteractor
import javax.inject.Inject

class ObserveDownloadItem @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    private val observeDownloadedItems: ObserveDownloadedItems,
) : SubjectInteractor<String, ItemWithDownload>() {

    override fun createObservable(params: String): Flow<ItemWithDownload> {
        return observeDownloadedItems.createObservable(Unit).map { downloadItems ->
            downloadItems.firstOrNull { it.file.fileId == params }
        }.filterNotNull().flowOn(dispatchers.io)
    }
}
