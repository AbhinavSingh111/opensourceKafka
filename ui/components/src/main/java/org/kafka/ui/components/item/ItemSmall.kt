package org.kafka.ui.components.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.kafka.data.entities.Item
import com.theapache64.rebugger.Rebugger
import ui.common.theme.theme.Dimens

@Composable
fun ItemSmall(item: Item, modifier: Modifier = Modifier) {
    Rebugger(trackMap = mapOf("item" to item))

    ItemSmall(
        title = item.title,
        creator = item.creator?.name,
        mediaType = item.mediaType,
        coverImage = item.coverImage,
        modifier = modifier
    )
}

@Composable
fun ItemSmall(
    title: String?,
    creator: String?,
    mediaType: String?,
    coverImage: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing12)
    ) {
        CoverImage(
            data = coverImage,
            placeholder = placeholder(mediaType),
            modifier = Modifier.align(Alignment.CenterVertically),
            size = Dimens.CoverSizeSmall
        )
        ItemDescription(
            title = { ItemTitleSmall(title) },
            creator = { ItemCreatorSmall(creator) },
            mediaType = { ItemMediaType(mediaType) },
            modifier = Modifier.padding(vertical = Dimens.Spacing02)
        )
    }
}

@Composable
fun ItemTitleSmall(title: String?, maxLines: Int = 1) {
    Text(
        text = title.orEmpty(),
        style = MaterialTheme.typography.titleSmall,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun ItemCreatorSmall(creator: String?, modifier: Modifier = Modifier) {
    Text(
        text = creator.orEmpty(),
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.secondary,
        modifier = modifier
    )
}
