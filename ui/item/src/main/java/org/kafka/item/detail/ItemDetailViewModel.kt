package org.kafka.item.detail

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kafka.data.model.ArchiveQuery
import com.kafka.data.model.SearchFilter.Creator
import com.kafka.data.model.SearchFilter.Subject
import com.kafka.data.model.booksByAuthor
import com.kafka.remote.config.RemoteConfig
import com.kafka.remote.config.isShareEnabled
import com.sarahang.playback.core.PlaybackConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.kafka.analytics.AppReviewManager
import org.kafka.analytics.logger.Analytics
import org.kafka.base.debug
import org.kafka.base.extensions.stateInDefault
import org.kafka.common.ObservableLoadingCounter
import org.kafka.common.collectStatus
import org.kafka.common.shareText
import org.kafka.common.snackbar.SnackbarManager
import org.kafka.common.snackbar.UiMessage
import org.kafka.domain.interactors.UpdateFavorite
import org.kafka.domain.interactors.UpdateItemDetail
import org.kafka.domain.interactors.UpdateItems
import org.kafka.domain.interactors.recent.AddRecentItem
import org.kafka.domain.observers.ObserveCreatorItems
import org.kafka.domain.observers.ObserveItemDetail
import org.kafka.domain.observers.library.ObserveFavoriteStatus
import org.kafka.item.R
import org.kafka.navigation.Navigator
import org.kafka.navigation.RootScreen
import org.kafka.navigation.Screen
import org.kafka.navigation.Screen.ItemDescription
import org.kafka.navigation.Screen.Search
import org.kafka.navigation.deeplink.Config
import org.kafka.navigation.deeplink.DeepLinksNavigation
import org.kafka.navigation.deeplink.Navigation
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    observeItemDetail: ObserveItemDetail,
    private val updateItemDetail: UpdateItemDetail,
    private val observeCreatorItems: ObserveCreatorItems,
    private val updateItems: UpdateItems,
    private val addRecentItem: AddRecentItem,
    private val observeFavoriteStatus: ObserveFavoriteStatus,
    private val updateFavorite: UpdateFavorite,
    private val playbackConnection: PlaybackConnection,
    private val navigator: Navigator,
    private val remoteConfig: RemoteConfig,
    private val snackbarManager: SnackbarManager,
    private val analytics: Analytics,
    private val appReviewManager: AppReviewManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val itemId: String = checkNotNull(savedStateHandle["itemId"])
    private val loadingState = ObservableLoadingCounter()
    private val currentRoot
        get() = navigator.currentRoot.value

    val state: StateFlow<ItemDetailViewState> = combine(
        observeItemDetail.flow.onEach { updateItemsByAuthor(it?.creator) },
        observeCreatorItems.flow,
        observeFavoriteStatus.flow,
        loadingState.observable,
    ) { itemDetail, itemsByCreator, isFavorite, isLoading ->
        debug { "ItemDetailViewModel: $itemDetail" }
        ItemDetailViewState(
            itemDetail = itemDetail,
            itemsByCreator = itemsByCreator,
            isFavorite = isFavorite,
            isLoading = isLoading
        )
    }.stateInDefault(
        scope = viewModelScope,
        initialValue = ItemDetailViewState(),
    )

    init {
        observeItemDetail(ObserveItemDetail.Param(itemId))
        observeFavoriteStatus(ObserveFavoriteStatus.Params(itemId))

        refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            updateItemDetail(UpdateItemDetail.Param(itemId))
                .collectStatus(loadingState, snackbarManager)
        }

        observeCreatorItems(ObserveCreatorItems.Params(itemId))
        observeFavoriteStatus(ObserveFavoriteStatus.Params(itemId))
    }

    fun onPrimaryAction(itemId: String) {
        if (state.value.itemDetail!!.isAudio) {
            addRecentItem(itemId)
            analytics.log { playItem(itemId) }
            playbackConnection.playAlbum(itemId)
        } else {
            analytics.log { readItem(itemId) }
            openReader(itemId)
        }
    }

    fun openFiles(itemId: String) {
        analytics.log { this.openFiles(itemId) }
        navigator.navigate(Screen.Files.createRoute(navigator.currentRoot.value, itemId))
    }

    private fun openReader(itemId: String) {
        val itemDetail = state.value.itemDetail
        itemDetail?.primaryFile?.let {
            addRecentItem(itemId)
            navigator.navigate(Screen.Reader.createRoute(navigator.currentRoot.value, it))
        } ?: viewModelScope.launch {
            snackbarManager.addMessage(UiMessage(R.string.file_type_is_not_supported))
        }
    }

    fun updateFavorite() {
        viewModelScope.launch {
            updateFavorite(UpdateFavorite.Params(itemId, !state.value.isFavorite)).collect()
        }
    }

    private fun updateItemsByAuthor(creator: String?) {
        creator?.let { ArchiveQuery().booksByAuthor(it) }?.let {
            viewModelScope.launch {
                updateItems(UpdateItems.Params(it))
                    .collectStatus(loadingState, snackbarManager)
            }
        }
    }

    private fun addRecentItem(itemId: String) {
        viewModelScope.launch {
            analytics.log { this.addRecentItem(itemId) }
            addRecentItem(AddRecentItem.Params(itemId)).collect()
        }
    }

    fun openItemDetail(itemId: String) {
        analytics.log { this.openItemDetail(itemId) }
        navigator.navigate(Screen.ItemDetail.createRoute(currentRoot, itemId))
    }

    fun goToSubjectSubject(keyword: String) {
        analytics.log { this.openSubject(keyword, "item_detail") }
        navigator.navigate(Search.createRoute(RootScreen.Search, keyword, Subject.name))
    }

    fun goToCreator(keyword: String?) {
        navigator.navigate(Search.createRoute(RootScreen.Search, keyword, Creator.name))
    }

    fun showDescription(itemId: String) {
        navigator.navigate(ItemDescription.createRoute(currentRoot, itemId))
    }

    fun isShareEnabled() = remoteConfig.isShareEnabled() && state.value.itemDetail != null

    fun shareItemText(context: Context) {
        analytics.log { this.shareItem(itemId) }
        val itemTitle = state.value.itemDetail!!.title

        val link = DeepLinksNavigation.findUri(Navigation.ItemDetail(itemId)).toString()
        val text = context.getString(R.string.check_out_on_kafka, itemTitle, link).trimIndent()

        context.shareText(text)
    }

    fun openArchiveItem() {
        analytics.log { this.openArchiveItem(itemId) }
        navigator.navigate(Screen.Web.createRoute(currentRoot, Config.archiveDetailUrl(itemId)))
    }

    fun showAppRatingIfNeeded(context: Context) {
        viewModelScope.launch { appReviewManager.incrementItemOpenCount() }

        if (appReviewManager.totalItemOpens % itemOpenThresholdForAppReview == 0) {
            context.getActivity()?.let { appReviewManager.showReviewDialog(it) }
        }
    }

    private fun Context.getActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.getActivity()
        else -> null
    }
}

private const val itemOpenThresholdForAppReview = 20
