package com.panda.keyboards.ui

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panda.keyboards.ime.ImeStatusChecker
import com.panda.keyboards.ui.fonts.FontSetupDialog
import com.panda.keyboards.ui.fonts.FontsScreen
import com.panda.keyboards.ui.fonts.FontsViewModel
import com.panda.keyboards.ui.keyboards.KeyboardsScreen
import com.panda.keyboards.ui.keyboards.KeyboardsViewModel
import com.panda.keyboards.ui.navigation.MainTab
import com.panda.keyboards.ui.setup.ImeSetupDialog
import com.panda.keyboards.ui.widgets.WidgetsScreen
import com.panda.keyboards.theme.KeyboardTheme
import com.panda.keyboards.ui.theme.PandaKeyboardsTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main entry point activity for Panda Keyboards.
 *
 * Provides a 3-tab navigation shell (Keyboards Gallery, Fonts, Widgets)
 * and hosts the interactive IME setup dialog.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var imeStatusChecker: ImeStatusChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()

        setContent {
            PandaKeyboardsTheme {
                // Controlled setup dialog (only shown when requested by user or theme click)
                var manualSetupRequest by rememberSaveable { mutableStateOf(false) }
                var showFontSetupDialog by rememberSaveable { mutableStateOf(false) }
                var activeSetupThemeId by rememberSaveable { mutableStateOf<String?>(null) }
                var showThemeReadyScreen by rememberSaveable { mutableStateOf(false) }
                var readyAppliedThemeId by rememberSaveable { mutableStateOf<String?>(null) }
                var autoFocusFontsInput by rememberSaveable { mutableStateOf(false) }
                var showCustomThemeEditor by rememberSaveable { mutableStateOf(false) }
                var selectedTab by rememberSaveable { mutableStateOf(MainTab.KEYBOARDS) }
                var showExitDialog by rememberSaveable { mutableStateOf(false) }

                val openSettings = {
                    startActivity(android.content.Intent(this@MainActivity, com.panda.keyboards.ui.settings.SettingsActivity::class.java))
                }

                // System Back button handler:
                // 1. If in Theme Ready placeholder -> return to Keyboards tab
                // 2. If in Custom Theme Editor -> return to Keyboards tab
                // 3. If in Keyboard Setup screen -> return to Keyboards tab with zero side effects
                // 4. If in non-Keyboards tab -> return to Keyboards tab
                // 5. If in Keyboards tab -> prompt with "Do you want to exit?" dialog
                BackHandler {
                    if (showThemeReadyScreen) {
                        showThemeReadyScreen = false
                    } else if (showCustomThemeEditor) {
                        showCustomThemeEditor = false
                    } else if (activeSetupThemeId != null) {
                        activeSetupThemeId = null
                    } else if (selectedTab != MainTab.KEYBOARDS) {
                        selectedTab = MainTab.KEYBOARDS
                    } else {
                        showExitDialog = true
                    }
                }

                if (manualSetupRequest) {
                    ImeSetupDialog(
                        imeStatusChecker = imeStatusChecker,
                        onDismiss = {
                            manualSetupRequest = false
                        }
                    )
                }

                if (showFontSetupDialog) {
                    FontSetupDialog(
                        imeStatusChecker = imeStatusChecker,
                        onDismiss = {
                            showFontSetupDialog = false
                        }
                    )
                }

                if (showExitDialog) {
                    AlertDialog(
                        onDismissRequest = { showExitDialog = false },
                        title = { Text(text = "Exit App") },
                        text = { Text(text = "Do you want to exit Panda Keyboards?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showExitDialog = false
                                    finish()
                                }
                            ) {
                                Text(text = "Exit")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showExitDialog = false }) {
                                Text(text = "Cancel")
                            }
                        }
                    )
                }

                if (showThemeReadyScreen) {
                    val themeReadyViewModel: com.panda.keyboards.ui.setup.ThemeReadyViewModel = hiltViewModel()
                    com.panda.keyboards.ui.setup.ThemeReadyScreen(
                        viewModel = themeReadyViewModel,
                        appliedThemeId = readyAppliedThemeId,
                        onStartWritingClick = {
                            showThemeReadyScreen = false
                            selectedTab = MainTab.FONTS
                            autoFocusFontsInput = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (showCustomThemeEditor) {
                    val editorViewModel: com.panda.keyboards.ui.themeeditor.CustomThemeEditorViewModel = hiltViewModel()
                    com.panda.keyboards.ui.themeeditor.CustomThemeEditorScreen(
                        viewModel = editorViewModel,
                        onBackClick = { showCustomThemeEditor = false },
                        onThemeSaved = { appliedId ->
                            showCustomThemeEditor = false
                            if (imeStatusChecker.imeStatus.value.isFullyConfigured) {
                                readyAppliedThemeId = appliedId
                                showThemeReadyScreen = true
                            } else {
                                activeSetupThemeId = appliedId
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (activeSetupThemeId != null) {
                    val setupViewModel: com.panda.keyboards.ui.setup.KeyboardSetupViewModel = hiltViewModel()
                    com.panda.keyboards.ui.setup.KeyboardSetupScreen(
                        viewModel = setupViewModel,
                        pendingThemeId = activeSetupThemeId,
                        onBackClick = { activeSetupThemeId = null },
                        onSetupComplete = { appliedId ->
                            activeSetupThemeId = null
                            readyAppliedThemeId = appliedId
                            showThemeReadyScreen = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            NavigationBar {
                                MainTab.entries.forEach { tab ->
                                    val isSelected = tab == selectedTab
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { selectedTab = tab },
                                        icon = {
                                            Icon(
                                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.title
                                            )
                                        },
                                        label = { Text(text = tab.title) }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        val isImeVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp
                        val contentModifier = Modifier.padding(
                            bottom = if (isImeVisible) 0.dp else innerPadding.calculateBottomPadding()
                        )

                        when (selectedTab) {
                            MainTab.KEYBOARDS -> {
                                val keyboardsViewModel: KeyboardsViewModel = hiltViewModel()
                                KeyboardsScreen(
                                    viewModel = keyboardsViewModel,
                                    onOpenSetupDialog = { themeId ->
                                        activeSetupThemeId = themeId
                                    },
                                    onThemeApplied = { appliedId ->
                                        readyAppliedThemeId = appliedId
                                        showThemeReadyScreen = true
                                    },
                                    onOpenCustomEditor = { showCustomThemeEditor = true },
                                    onSettingsClick = openSettings,
                                    modifier = contentModifier
                                )
                            }


                            MainTab.FONTS -> {
                                val fontsViewModel: FontsViewModel = hiltViewModel()
                                FontsScreen(
                                    viewModel = fontsViewModel,
                                    autoFocusInput = autoFocusFontsInput,
                                    onAutoFocusConsumed = { autoFocusFontsInput = false },
                                    onOpenSetupDialog = {
                                        showFontSetupDialog = true
                                    },
                                    onSettingsClick = openSettings,
                                    modifier = contentModifier
                                )
                            }

                            MainTab.WIDGETS -> {
                                WidgetsScreen(modifier = contentModifier)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        imeStatusChecker.refresh()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
