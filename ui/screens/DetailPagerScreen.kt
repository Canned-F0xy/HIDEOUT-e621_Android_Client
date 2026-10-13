package com.CannedF0xy.hideout.ui.screens

import androidx.annotation.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import coil.ImageLoader
import com.CannedF0xy.hideout.data.AppPreferences
import com.CannedF0xy.hideout.network.model.Post
import com.CannedF0xy.hideout.ui.components.shimmerEffect

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailPagerScreen(
    lazyPagingItems: LazyPagingItems<Post>,
    initialIndex: Int,
    appPrefs: AppPreferences,
    isDarkTheme: Boolean,
    isAutoPlay: Boolean,
    gifEnabledLoader: ImageLoader,
    cfClearanceCookie: String,
    onClose: () -> Unit,
    onTagClick: (String) -> Unit,
    onPageChanged: (Int) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { lazyPagingItems.itemCount })

    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
    }

    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { pageIndex ->
        val isActive = pagerState.currentPage == pageIndex
        val currentPost = if (pageIndex < lazyPagingItems.itemCount) lazyPagingItems[pageIndex] else null

        if (currentPost != null) {
            DetailScreen(
                post = currentPost,
                isActivePage = isActive,
                appPrefs = appPrefs,
                isDarkTheme = isDarkTheme,
                isAutoPlay = isAutoPlay,
                gifEnabledLoader = gifEnabledLoader,
                cfClearanceCookie = cfClearanceCookie,
                onClose = onClose,
                onTagClick = onTagClick
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.fillMaxSize().shimmerEffect())
            }
        }
    }
}
