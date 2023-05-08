package org.kafka.homepage.components

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import org.kafka.common.CarouselItem
import org.kafka.common.carousels
import org.kafka.common.widgets.shadowMaterial
import org.kafka.navigation.LocalNavigator
import org.kafka.navigation.Navigator
import org.kafka.navigation.RootScreen
import org.kafka.navigation.Screen
import ui.common.theme.theme.Dimens

@Composable
internal fun Carousels(
    carouselItems: List<CarouselItem> = carousels,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current

    BoxWithConstraints {
        LazyRow(
            modifier = modifier
                .padding(vertical = Dimens.Spacing04)
                .heightIn(max = Dimens.Spacing196),
            contentPadding = PaddingValues(horizontal = Dimens.Spacing12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            itemsIndexed(carouselItems) { _, item ->
                CarouselItem(item, navigator)
            }
        }
    }
}

@Composable
private fun CarouselItem(item: CarouselItem, navigator: Navigator) {
    val currentRoot by navigator.currentRoot.collectAsStateWithLifecycle()
    AsyncImage(
        model = item.image,
        contentScale = ContentScale.Fit,
        contentDescription = null,
        modifier = Modifier
            .padding(Dimens.Spacing02)
            .shadowMaterial(
                elevation = Dimens.Spacing12,
                shape = RoundedCornerShape(Dimens.Spacing08)
            )
            .clickable {
                when (item.itemId) {
                    "kafka_archives" -> navigator.navigate(
                        Screen.Search.createRoute(RootScreen.Search, "kafka%20archives")
                    )

                    "adbi-duniya" -> navigator.navigate(
                        Screen.Search.createRoute(RootScreen.Search, "adbi-duniya")
                    )

                    else -> navigator.navigate(
                        Screen.ItemDetail.createRoute(currentRoot, item.itemId)
                    )
                }
            }
    )
}
