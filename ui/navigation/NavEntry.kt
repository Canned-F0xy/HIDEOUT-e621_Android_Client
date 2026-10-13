package com.CannedF0xy.hideout.ui.navigation

data class NavEntry(
    val query: String,
    val scrollIdx: Int = 0,
    val scrollOff: Int = 0,
    val detailIdx: Int? = null
)
