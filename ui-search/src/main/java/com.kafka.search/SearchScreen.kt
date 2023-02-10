package com.kafka.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kafka.data.entities.RecentSearch
import org.kafka.common.logging.LogCompositions
import org.kafka.item.ArchiveQueryViewModel
import org.kafka.item.ArchiveQueryViewState
import org.kafka.item.SearchFilter
import org.kafka.navigation.LeafScreen
import org.kafka.navigation.LocalNavigator
import org.kafka.navigation.RootScreen
import org.kafka.ui.components.ProvideScaffoldPadding
import org.kafka.ui.components.item.Item
import org.kafka.ui.components.progress.InfiniteProgressBar
import org.kafka.ui.components.scaffoldPadding
import ui.common.theme.theme.Dimens
import ui.common.theme.theme.textSecondary

@Composable
fun SearchScreen() {
    LogCompositions(tag = "Search")

    val queryViewModel: ArchiveQueryViewModel = hiltViewModel()
    val searchViewModel: SearchViewModel = hiltViewModel()
    val queryViewState by queryViewModel.state.collectAsStateWithLifecycle()
    val recentSearches by searchViewModel.recentSearches.collectAsStateWithLifecycle()
    val keywordState by searchViewModel.keyword.collectAsStateWithLifecycle()

    val searchText by remember {
        derivedStateOf { TextFieldValue(text = keywordState, selection = TextRange(0)) }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        ProvideScaffoldPadding(padding = padding) {
            Search(
                searchText = searchText,
                setSearchText = { searchViewModel.updateKeyword(it.text) },
                queryViewModel = queryViewModel,
                searchViewModel = searchViewModel,
                queryViewState = queryViewState,
                recentSearches = recentSearches,
            )
        }
    }
}

@Composable
private fun Search(
    searchText: TextFieldValue,
    setSearchText: (TextFieldValue) -> Unit,
    queryViewModel: ArchiveQueryViewModel,
    searchViewModel: SearchViewModel,
    queryViewState: ArchiveQueryViewState,
    recentSearches: List<RecentSearch>,
) {
    val navigator = LocalNavigator.current
    val scaffoldPadding = scaffoldPadding()
    val selectedFilters = remember { mutableStateListOf(*SearchFilter.values()) }

    LaunchedEffect(Unit) {
        if (searchText.text.isNotEmpty()) {
            queryViewModel.submitQuery(searchText.text, selectedFilters)
        }
    }

    val onSearchClicked: (String) -> Unit = {
        queryViewModel.submitQuery(it, selectedFilters)
        searchViewModel.addRecentSearch(it)
    }

    Column(modifier = Modifier.padding(top = scaffoldPadding.calculateTopPadding())) {
        SearchWidget(
            searchText = searchText,
            setSearchText = setSearchText,
            onImeAction = onSearchClicked
        )

        SearchFilterChips(selectedFilters)

        AnimatedVisibility(visible = queryViewState.items != null) {
            val padding = PaddingValues(bottom = scaffoldPadding.calculateBottomPadding())
            LazyColumn(contentPadding = padding) {
                items(queryViewState.items!!) {
                    Item(item = it) { itemId ->
                        navigator.navigate(
                            LeafScreen.ItemDetail.buildRoute(itemId, RootScreen.Search)
                        )
                    }
                }
            }
        }

        if (queryViewState.items == null) {
            RecentSearches(
                recentSearches = recentSearches,
                queryViewState = queryViewState,
                searchViewModel = searchViewModel,
                setSearchText = setSearchText,
                onSearchClicked = onSearchClicked
            )
        }

        InfiniteProgressBar(show = queryViewState.isLoading)
    }
}

@Composable
private fun RecentSearches(
    recentSearches: List<RecentSearch>,
    queryViewState: ArchiveQueryViewState,
    searchViewModel: SearchViewModel,
    setSearchText: (TextFieldValue) -> Unit,
    onSearchClicked: (String) -> Unit
) {
    if (recentSearches.isNotEmpty() && queryViewState.items.isNullOrEmpty() && !queryViewState.isLoading) {
        RecentSearches(recentSearches = recentSearches.map { it.searchTerm }, onSearchClicked = {
            setSearchText(TextFieldValue(text = it, selection = TextRange(it.lastIndex)))
            onSearchClicked(it)
        }, onRemoveSearch = { searchViewModel.removeRecentSearch(it) })
    }
}

@Composable
fun SearchResultLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.padding(
            start = Dimens.Spacing12, end = 24.dp, bottom = Dimens.Spacing12
        ),
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.textSecondary
    )
}
