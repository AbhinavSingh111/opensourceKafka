package org.kafka.favorites

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.kafka.common.asImmutable
import org.kafka.navigation.LeafScreen
import org.kafka.navigation.LocalNavigator
import org.kafka.navigation.RootScreen
import org.kafka.ui.components.ProvideScaffoldPadding
import org.kafka.ui.components.item.LayoutType

@Composable
fun LibraryScreen(
    favoriteViewModel: FavoriteViewModel = hiltViewModel(),
    downloadsViewModel: DownloadsViewModel = hiltViewModel()
) {
    val favoriteViewState by favoriteViewModel.state.collectAsStateWithLifecycle()
    val downloadViewState by downloadsViewModel.state.collectAsStateWithLifecycle()

    val navigator = LocalNavigator.current
    val openItemDetail: (String) -> Unit = {
        navigator.navigate(LeafScreen.ItemDetail.buildRoute(it, RootScreen.Library))
    }

    Scaffold { padding ->
        ProvideScaffoldPadding(padding = padding) {
            Favorites(
                favoriteViewState = favoriteViewState,
                downloadViewState = downloadViewState,
                changeLayoutType = { favoriteViewModel.updateLayoutType(it) },
                openItemDetail = openItemDetail
            )
        }
    }
}

@Composable
private fun Favorites(
    favoriteViewState: FavoriteViewState,
    downloadViewState: DownloadViewState,
    changeLayoutType: (LayoutType) -> Unit,
    openItemDetail: (String) -> Unit
) {
    val pagerState = rememberPagerState()

    Column(modifier = Modifier.fillMaxSize()) {
        Tabs(
            pagerState = pagerState,
            tabs = LibraryTab.values().map { it.name }.asImmutable(),
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            pageCount = LibraryTab.values().size,
            state = pagerState
        ) { page ->
            Column {
                when (LibraryTab.values()[page]) {
                    LibraryTab.Favorites -> Favorites(
                        items = favoriteViewState.favoriteItems,
                        layoutType = favoriteViewState.layoutType,
                        changeLayoutType = changeLayoutType,
                        openItemDetail = openItemDetail
                    )

                    LibraryTab.Downloads -> Downloads(
                        items = downloadViewState.downloadedItems,
                        layoutType = favoriteViewState.layoutType,
                        changeLayoutType = changeLayoutType,
                        openItemDetail = openItemDetail
                    )
                }
            }
        }
    }
}

internal enum class LibraryTab { Favorites, Downloads }
