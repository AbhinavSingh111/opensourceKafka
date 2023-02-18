package org.kafka.item.detail

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kafka.data.entities.Item
import com.kafka.data.entities.ItemDetail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.kafka.base.debug
import org.kafka.common.animation.Delayed
import org.kafka.common.extensions.AnimatedVisibilityFade
import org.kafka.common.simpleClickable
import org.kafka.common.widgets.FullScreenMessage
import org.kafka.common.widgets.LoadImage
import org.kafka.common.widgets.shadowMaterial
import org.kafka.item.preloadImages
import org.kafka.navigation.LocalNavigator
import org.kafka.navigation.Navigator
import org.kafka.navigation.RootScreen
import org.kafka.navigation.Screen
import org.kafka.ui.components.ProvideScaffoldPadding
import org.kafka.ui.components.bottomScaffoldPadding
import org.kafka.ui.components.progress.InfiniteProgressBar
import org.kafka.ui.components.scaffoldPadding
import ui.common.theme.theme.Dimens
import ui.common.theme.theme.textPrimary

@Composable
fun ItemDetail(viewModel: ItemDetailViewModel = hiltViewModel()) {
    debug { "Item Detail launch" }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val navigator = LocalNavigator.current
    val context = LocalContext.current

    LaunchedEffect(state.itemsByCreator) {
        preloadImages(context, state.itemsByCreator)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                lazyListState = lazyListState,
                isShareVisible = state.itemDetail != null,
                onShareClicked = { viewModel.shareItemText(context) }
            )
        }
    ) { padding ->
        ProvideScaffoldPadding(padding = padding) {
            ItemDetail(state, viewModel, navigator, lazyListState)
        }
    }
}

@Composable
private fun ItemDetail(
    state: ItemDetailViewState,
    viewModel: ItemDetailViewModel,
    navigator: Navigator,
    lazyListState: LazyListState
) {
    Box(Modifier.fillMaxSize()) {
        InfiniteProgressBar(
            show = state.isFullScreenLoading,
            modifier = Modifier.align(Alignment.Center)
        )

        FullScreenMessage(state.message, state.isFullScreenError, viewModel::retry)

        AnimatedVisibilityFade(state.itemDetail != null) {
            val currentRoot by navigator.currentRoot.collectAsStateWithLifecycle()
            ItemDetail(
                itemDetail = state.itemDetail!!,
                relatedItems = state.itemsByCreator,
                isLoading = state.isLoading,
                isFavorite = state.isFavorite,
                toggleFavorite = { viewModel.updateFavorite() },
                openItemDetail = { itemId ->
                    navigator.navigate(Screen.ItemDetail.createRoute(currentRoot, itemId))
                },
                openFiles = { itemId ->
                    viewModel.openFiles(itemId)
                },
                onPrimaryAction = { itemId ->
                    viewModel.onPrimaryAction(itemId)
                },
                goToCreator = { creator ->
                    navigator.navigate(Screen.Search.createRoute(RootScreen.Search, creator))
                },
                lazyListState = lazyListState
            )
        }
    }
}

@Composable
private fun ItemDetail(
    itemDetail: ItemDetail,
    relatedItems: List<Item>?,
    isLoading: Boolean,
    isFavorite: Boolean,
    toggleFavorite: () -> Unit,
    openItemDetail: (String) -> Unit,
    onPrimaryAction: (String) -> Unit,
    openFiles: (String) -> Unit,
    goToCreator: (String?) -> Unit,
    lazyListState: LazyListState
) {
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    HandleBackPress(bottomSheetState, coroutineScope)

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            DescriptionDialog(itemDetail = itemDetail)
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            contentPadding = scaffoldPadding()
        ) {
            item {
                ItemDescription(
                    itemDetail = itemDetail,
                    showDescription = { coroutineScope.launch { bottomSheetState.show() } },
                    goToCreator = goToCreator
                )
            }

            item {
                ItemDetailActions(
                    itemDetail = itemDetail,
                    onPrimaryAction = onPrimaryAction,
                    openFiles = openFiles,
                    isFavorite = isFavorite,
                    toggleFavorite = toggleFavorite
                )
            }

            relatedContent(relatedItems, openItemDetail)

            if (isLoading) {
                item {
                    Delayed(modifier = Modifier.animateItemPlacement()) {
                        InfiniteProgressBar()
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemDescription(
    itemDetail: ItemDetail,
    showDescription: () -> Unit,
    goToCreator: (String?) -> Unit
) {
    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.Spacing24),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoadImage(
                data = itemDetail.coverImage,
                modifier = Modifier
                    .size(196.dp, 248.dp)
                    .shadowMaterial(Dimens.Spacing12, RoundedCornerShape(Dimens.Spacing04))
            )

            Spacer(Modifier.height(Dimens.Spacing24))

            Text(
                text = itemDetail.title.orEmpty(),
                style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
                color = MaterialTheme.colorScheme.textPrimary,
                modifier = Modifier.padding(horizontal = Dimens.Spacing24)
            )

            Spacer(Modifier.height(Dimens.Spacing04))

            Text(
                text = itemDetail.creator.orEmpty(),
                style = MaterialTheme.typography.titleSmall.copy(textAlign = TextAlign.Center),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .simpleClickable { goToCreator(itemDetail.creator) }
                    .padding(horizontal = Dimens.Spacing24)
            )

            Text(
                text = ratingText(MaterialTheme.colorScheme.secondary) +
                        AnnotatedString(itemDetail.description.orEmpty()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(Dimens.Spacing24)
                    .clickable { showDescription() }
            )
        }
    }
}

@Composable
private fun DescriptionDialog(itemDetail: ItemDetail, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = Dimens.Spacing24)
            .padding(top = Dimens.Spacing24)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp, 4.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.height(Dimens.Spacing36))
        Text(
            text = ratingText(MaterialTheme.colorScheme.secondary) +
                    AnnotatedString(itemDetail.description.orEmpty()),
            style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Justify),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = bottomScaffoldPadding())
        )
    }
}

@Composable
private fun HandleBackPress(
    bottomSheetState: ModalBottomSheetState,
    coroutineScope: CoroutineScope
) {
    val backPressDispatcher = LocalOnBackPressedDispatcherOwner.current
    backPressDispatcher?.onBackPressedDispatcher
        ?.addCallback(LocalLifecycleOwner.current, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (bottomSheetState.isVisible) {
                    isEnabled = true
                    coroutineScope.launch { bottomSheetState.hide() }
                } else {
                    isEnabled = false
                    backPressDispatcher.onBackPressedDispatcher.onBackPressed()
                }
            }
        })
}
