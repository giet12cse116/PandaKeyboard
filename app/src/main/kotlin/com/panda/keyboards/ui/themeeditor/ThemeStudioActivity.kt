package com.panda.keyboards.ui.themeeditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.panda.keyboards.ui.theme.PandaKeyboardsTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Dedicated Activity hosting the Stitch-styled 3-Step Create Custom Theme flow.
 */
@AndroidEntryPoint
class ThemeStudioActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()

        setContent {
            PandaKeyboardsTheme {
                val editorViewModel: CustomThemeEditorViewModel = hiltViewModel()
                CustomThemeEditorScreen(
                    viewModel = editorViewModel,
                    onBackClick = { finish() },
                    onThemeSaved = { appliedId ->
                        val intent = android.content.Intent().apply {
                            putExtra("OPEN_FONTS_TAB", true)
                            putExtra("APPLIED_THEME_ID", appliedId)
                        }
                        setResult(RESULT_OK, intent)
                        finish()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
