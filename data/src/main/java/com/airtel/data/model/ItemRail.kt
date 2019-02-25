package com.airtel.data.model

import com.airtel.data.entities.Item

/**
 * @author Vipul Kumar; dated 14/02/19.
 */
data class ItemRail(
    val title: String = "",
    val items: List<Item>?
)
