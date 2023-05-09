package org.kafka.ui.components.item

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import coil.compose.AsyncImagePainter.State
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import org.kafka.common.widgets.shadowMaterial
import org.kafka.ui.components.R
import ui.common.theme.theme.Dimens

@Composable
fun CoverImage(
    data: Any?,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    size: DpSize = DpSize.Unspecified,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(backgroundColor = containerColor),
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = RoundedCornerShape(Dimens.Spacing04),
    placeholder: Painter = painterResource(id = R.drawable.ic_absurd_bulb),
    iconPadding: Dp = 0.dp,
    bitmapPlaceholder: Bitmap? = null,
    contentDescription: String? = null,
    elevation: Dp = 2.dp,
) {
    val sizeMod = if (size.isSpecified) Modifier.size(size) else Modifier
    Surface(
        tonalElevation = elevation,
        color = containerColor,
        shape = shape,
        modifier = modifier
            .then(sizeMod)
            .shadowMaterial(Dimens.Spacing08, shape)
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(data)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
        ) {
            val state = painter.state
            when (state) {
                is State.Error, State.Empty, is State.Loading -> {
                    Icon(
                        painter = placeholder,
                        tint = contentColor,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(iconPadding)
                    )
                }

                else -> SubcomposeAsyncImageContent(imageModifier.fillMaxSize())
            }

            if (bitmapPlaceholder != null && state is State.Loading) {
                Image(
                    painter = rememberAsyncImagePainter(bitmapPlaceholder),
                    contentDescription = null,
                    contentScale = contentScale,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape)
                )
            }
        }
    }
}
