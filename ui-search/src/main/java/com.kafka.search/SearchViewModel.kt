package com.kafka.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kafka.base.debug
import org.kafka.base.extensions.stateInDefault
import org.kafka.domain.interactors.AddRecentSearch
import org.kafka.domain.interactors.RemoveRecentSearch
import org.kafka.domain.observers.ObserveRecentSearch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    observeRecentSearch: ObserveRecentSearch,
    private val addRecentSearch: AddRecentSearch,
    private val removeRecentSearch: RemoveRecentSearch,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val keyword = savedStateHandle.getStateFlow("keyword", "")
    val recentSearches = observeRecentSearch.flow.stateInDefault(viewModelScope, emptyList())

    init {
        observeRecentSearch(Unit)
        debug { "SearchViewModel created ${keyword.value}" }
    }

    fun updateKeyword(keyword: String) {
        savedStateHandle["keyword"] = keyword
    }

    fun addRecentSearch(keyword: String) {
        viewModelScope.launch {
            addRecentSearch.invoke(keyword).collect()
        }
    }

    fun removeRecentSearch(keyword: String) {
        viewModelScope.launch {
            removeRecentSearch.invoke(keyword).collect()
        }
    }
}


