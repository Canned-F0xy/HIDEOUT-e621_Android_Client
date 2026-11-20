package com.CannedF0xy.hideout.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import coil.ImageLoader
import com.CannedF0xy.hideout.data.AppPreferences
import com.CannedF0xy.hideout.network.model.Post

@Composable
fun ListDetailPagerScreen(
    posts: List<Post>,
    initialIndex: Int,
    appPrefs: AppPreferences,
    isDarkTheme: Boolean,
    isAutoPlay: Boolean,
    gifEnabledLoader: ImageLoader,
    cfClearanceCookie: String,
    onClose: () -> Unit,
    onTagClick: (String) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (posts.size - 1).coerceAtLeast(0)),
        pageCount = { posts.size }
    )

    BackHandler { onClose() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { index -> posts[index].id }
        ) { pageIndex ->
            DetailScreen(
                post = posts[pageIndex],
                isActivePage = pagerState.currentPage == pageIndex,
                appPrefs = appPrefs,
                isDarkTheme = isDarkTheme,
                isAutoPlay = isAutoPlay,
                gifEnabledLoader = gifEnabledLoader,
                cfClearanceCookie = cfClearanceCookie,
                onClose = onClose,
                onTagClick = onTagClick
            )
        }
    }
}
