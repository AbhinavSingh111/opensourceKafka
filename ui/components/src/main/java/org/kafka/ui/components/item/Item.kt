package org.kafka.ui.components.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kafka.data.entities.Item
import org.kafka.common.image.Icons
import org.kafka.common.widgets.IconResource
import ui.common.theme.theme.Dimens

@Composable
fun Item(
    item: Item,
    modifier: Modifier = Modifier,
    openItemDetail: (String) -> Unit
) {
    Item(
        title = item.title,
        creator = item.creator?.name,
        mediaType = item.mediaType,
        coverImage = item.coverImage,
        itemId = item.itemId,
        modifier = modifier,
        openItemDetail = openItemDetail
    )
}

@Composable
fun Item(
    title: String?,
    creator: String?,
    mediaType: String?,
    coverImage: String?,
    itemId: String,
    modifier: Modifier = Modifier,
    openItemDetail: (String) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { openItemDetail(itemId) },
        horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing16)
    ) {
        CoverImage(
            data = coverImage,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .widthIn(max = 80.dp)
                .heightIn(max = 92.dp)
        )

        ItemDescription(
            title = { ItemTitleMedium(title) },
            creator = { ItemCreator(creator) },
            mediaType = { ItemMediaType(mediaType) }
        )
    }
}

//@Composable
//fun CoverImage(
//    coverImage: String?,
//    modifier: Modifier = Modifier,
//    size: DpSize = Dimens.CoverSizeMedium
//) {
//    Box(
//        modifier = modifier
//            .size(size)
//            .shadowMaterial(
//                elevation = Dimens.Spacing08,
//                shape = RoundedCornerShape(Dimens.RadiusSmall)
//            )
//    ) {
//        AsyncImage(
//            model = coverImage,
//            placeholder = painterResource(id = R.drawable.ic_absurd_bulb),
//            contentDescription = "Cover",
//            modifier = Modifier
//                .fillMaxSize()
//                .background(MaterialTheme.colorScheme.surface),
//            contentScale = ContentScale.Crop
//        )
//    }
//}

@Composable
fun ItemDescription(
    title: @Composable () -> Unit,
    creator: @Composable () -> Unit,
    mediaType: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        title()
        Spacer(modifier = Modifier.height(Dimens.Spacing02))
        Row(verticalAlignment = Alignment.CenterVertically) {
            creator()
        }
        Spacer(modifier = Modifier.height(Dimens.Spacing04))
        mediaType()
    }
}

@Composable
fun ItemTitleMedium(title: String?, maxLines: Int = 2) {
    Text(
        text = title.orEmpty(),
        style = MaterialTheme.typography.titleMedium,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun ItemCreator(creator: String?, modifier: Modifier = Modifier) {
    Text(
        text = creator.orEmpty(),
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.secondary,
        modifier = modifier
    )
}

@Composable
fun ItemMediaType(mediaType: String?, modifier: Modifier = Modifier) {
    IconResource(
        imageVector = if (mediaType == "audio") Icons.Audio else Icons.Texts,
        tint = MaterialTheme.colorScheme.secondary,
        modifier = modifier.size(16.dp)
    )
}
