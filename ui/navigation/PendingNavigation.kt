package com.CannedF0xy.hideout.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object PendingNavigation {
    var openTag by mutableStateOf<String?>(null)
}
