package com.kafka.content.ui.library

import com.kafka.data.entities.Item
import com.kafka.ui_common.base.BaseViewState

/**
 * @author Vipul Kumar; dated 27/12/18.
 */
data class LibraryViewState(
    var favorites: List<Item>? = null
) : BaseViewState
