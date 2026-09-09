package com.panda.keyboards.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation tabs for the Panda Keyboards main application bottom bar.
 */
enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    KEYBOARDS(
        title = "Keyboards",
        selectedIcon = Icons.Filled.Keyboard,
        unselectedIcon = Icons.Outlined.Keyboard
    ),
    FONTS(
        title = "Fonts",
        selectedIcon = Icons.Filled.TextFields,
        unselectedIcon = Icons.Outlined.TextFields
    ),
    WIDGETS(
        title = "Widgets",
        selectedIcon = Icons.Filled.Widgets,
        unselectedIcon = Icons.Outlined.Widgets
    )
}
