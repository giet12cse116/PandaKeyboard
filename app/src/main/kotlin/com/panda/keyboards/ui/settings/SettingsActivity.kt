package com.panda.keyboards.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.panda.keyboards.ime.ImeStatusChecker
import com.panda.keyboards.ui.setup.ImeSetupDialog
import com.panda.keyboards.ui.theme.PandaKeyboardsTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Standalone Activity hosting the Settings screen.
 *
 * Launched directly by the IME's top toolbar Settings button and
 * from gear icon entry points within the host application.
 */
@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    @Inject
    lateinit var imeStatusChecker: ImeStatusChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()

        setContent {
            PandaKeyboardsTheme {
                var showSetupDialog by rememberSaveable { mutableStateOf(false) }

                if (showSetupDialog) {
                    ImeSetupDialog(
                        imeStatusChecker = imeStatusChecker,
                        onDismiss = { showSetupDialog = false }
                    )
                }

                val settingsViewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBackClick = { finish() },
                    onOpenSetupDialog = { showSetupDialog = true },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
