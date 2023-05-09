package org.kafka.library.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kafka.data.entities.Item
import org.kafka.common.plus
import org.kafka.common.snackbar.UiMessage
import org.kafka.common.widgets.FullScreenMessage
import org.kafka.favorites.R
import org.kafka.ui.components.bottomScaffoldPadding
import org.kafka.ui.components.item.Item
import org.kafka.ui.components.item.LayoutType
import org.kafka.ui.components.item.LibraryItem
import ui.common.theme.theme.Dimens

@Composable
internal fun Favorites(
    items: List<Item>,
    layoutType: LayoutType,
    changeLayoutType: (LayoutType) -> Unit,
    openItemDetail: (String) -> Unit
) {
    if (items.isEmpty()) {
        FullScreenMessage(UiMessage(stringResource(id = R.string.no_favorites_items_message)))
    } else {
        when (layoutType) {
            LayoutType.List -> FavoriteItemList(
                favoriteItems = items,
                openItemDetail = openItemDetail,
                header = {
                    LayoutType(layoutType = layoutType, changeViewType = changeLayoutType)
                }
            )

            LayoutType.Grid -> FavoriteItemGrid(
                favoriteItems = items,
                openItemDetail = openItemDetail,
                header = {
                    LayoutType(layoutType = layoutType, changeViewType = changeLayoutType)
                }
            )
        }
    }
}

@Composable
private fun FavoriteItemList(
    favoriteItems: List<Item>,
    openItemDetail: (String) -> Unit,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val padding =
        PaddingValues(Dimens.Spacing08) + PaddingValues(bottom = bottomScaffoldPadding())

    LazyColumn(
        contentPadding = padding,
        modifier = modifier.fillMaxSize()
    ) {
        item { header() }

        itemsIndexed(
            items = favoriteItems,
            key = { _, item -> item.itemId }
        ) { _, item ->
            Item(
                item = item,
                modifier = Modifier
                    .clickable { openItemDetail(item.itemId) }
                    .padding(vertical = Dimens.Spacing06, horizontal = Dimens.Gutter)
            )
        }
    }
}

@Composable
private fun FavoriteItemGrid(
    favoriteItems: List<Item>,
    openItemDetail: (String) -> Unit,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val padding =
        PaddingValues(Dimens.Spacing08) + PaddingValues(bottom = bottomScaffoldPadding())
    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        columns = GridCells.Fixed(LayoutType.values().size),
        contentPadding = padding
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            header()
        }

        items(favoriteItems, key = { it.itemId }) {
            LibraryItem(item = it, openItemDetail = openItemDetail)
        }
    }
}
