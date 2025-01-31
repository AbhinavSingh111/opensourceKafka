package org.kafka.item.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.kafka.common.extensions.AnimatedVisibilityFade
import org.kafka.common.image.Icons
import org.kafka.common.widgets.IconButton
import org.kafka.common.widgets.IconResource
import org.kafka.ui.components.material.TopBar
import ui.common.theme.theme.Dimens

@Composable
internal fun TopBar(
    onShareClicked: () -> Unit,
    onBackPressed: () -> Unit,
    lazyListState: LazyListState = rememberLazyListState(),
    isShareVisible: Boolean = false
) {
    val isRaised by remember { derivedStateOf { lazyListState.firstVisibleItemIndex > 2 } }

    val containerColor by animateColorAsState(
        if (isRaised) MaterialTheme.colorScheme.primary else Color.Transparent
    )
    val contentColor by animateColorAsState(
        if (isRaised) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    )

    TopBar(
        containerColor = Color.Transparent,
        navigationIcon = {
            BackIcon(onBackPressed, containerColor, contentColor)
        },
        actions = {
            if (isShareVisible) {
                ShareIcon(isRaised, onShareClicked)
            }
        }
    )
}

@Composable
private fun ShareIcon(isRaised: Boolean, onShareClicked: () -> Unit) {
    AnimatedVisibilityFade(!isRaised) {
        IconButton(
            onClick = { onShareClicked() },
            modifier = Modifier.padding(Dimens.Spacing08)
        ) {
            IconResource(
                imageVector = Icons.Share,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BackIcon(
    onBackPressed: () -> Unit,
    containerColor: Color,
    contentColor: Color
) {
    IconButton(
        onClick = { onBackPressed() },
        modifier = Modifier
            .padding(Dimens.Spacing08)
            .clip(CircleShape)
            .background(containerColor)
    ) {
        IconResource(imageVector = Icons.Back, tint = contentColor)
    }
}

