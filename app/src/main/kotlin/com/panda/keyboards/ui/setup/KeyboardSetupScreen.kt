package com.panda.keyboards.ui.setup

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panda.keyboards.theme.KeyboardTheme
import com.panda.keyboards.ui.keyboards.KeyboardThemePreviewImage

private val GradientStart = Color(0xFF7C4DFF)
private val StepCompletedColor = Color(0xFF2E7D32)

/**
 * Dedicated Keyboard Setup screen with UI/UX polish pass.
 *
 * Provides a full-screen guided setup flow with a theme heading at the top,
 * constrained keyboard preview card, clear 2-step permission action cards with
 * theme-aware contrast, and exact privacy disclaimer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardSetupScreen(
    viewModel: KeyboardSetupViewModel,
    pendingThemeId: String?,
    onBackClick: () -> Unit,
    onSetupComplete: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val imeStatus by viewModel.imeStatus.collectAsStateWithLifecycle()
    val pendingTheme by viewModel.pendingTheme.collectAsStateWithLifecycle()
    val setupCompleted by viewModel.setupCompleted.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Initialize pending theme state in ViewModel
    LaunchedEffect(pendingThemeId) {
        viewModel.initPendingTheme(pendingThemeId)
    }

    // Auto-navigate to Sprint 10c when setup completes
    LaunchedEffect(setupCompleted) {
        if (setupCompleted) {
            onSetupComplete(pendingThemeId)
        }
    }

    // Re-evaluate IME status when resuming activity (e.g. returning from System Settings)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshImeStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Set Up Panda Keyboard",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Gallery"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val currentTheme = pendingTheme

            // ── Section 1: Theme Name Heading at Top & Constrained Preview ─────
            if (currentTheme != null) {
                Text(
                    text = currentTheme.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Selected Theme Preview",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(max = 210.dp)
                        .aspectRatio(1.6f)
                        .clip(RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        KeyboardThemePreviewImage(theme = currentTheme)
                    }
                }
            } else {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🐼", fontSize = 36.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Section 2: Honest Plain-Language Explanation ──────────────────
            Text(
                text = "Two Quick System Steps",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Android requires all custom keyboards to be explicitly enabled in System Settings before they can deliver typed characters across your apps. This ensures you remain in full control of your active input method.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Section 3: Live 2-Step Action Cards ───────────────────────────
            SetupStepCard(
                stepNumber = 1,
                title = "Enable Panda Keyboard",
                description = "Turn on Panda Keyboard in system input settings",
                isCompleted = imeStatus.isEnabled,
                buttonText = if (imeStatus.isEnabled) "Enabled ✓" else "Enable",
                onAction = {
                    if (!imeStatus.isEnabled) {
                        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            SetupStepCard(
                stepNumber = 2,
                title = "Select Panda Keyboard",
                description = "Choose Panda Keyboard as your default active keyboard",
                isCompleted = imeStatus.isDefault,
                isEnabled = imeStatus.isEnabled,
                buttonText = if (imeStatus.isDefault) "Active ✓" else "Select",
                onAction = {
                    if (imeStatus.isEnabled && !imeStatus.isDefault) {
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.showInputMethodPicker()
                    }
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Section 4: De-emphasized Verbatim Privacy Disclaimer ─────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Privacy & Security Note",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "We never collect or sell your typing. You can even enable offline mode in settings to stop collection of anonymous usage from themes and fonts.\n\nWhen activating a third-party keyboard, Android will display a system reminder. This is completely normal and does not compromise privacy in any way.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Single numbered setup step card with real-time status reflection.
 *
 * Requirements 3 & 4:
 * - Keeps step number ("1", "2") intact even when step is granted.
 * - Changes indicator circle to StepCompletedColor (green).
 * - Theme-aware contrast logic supporting both Light and Dark system modes.
 */
@Composable
private fun SetupStepCard(
    stepNumber: Int,
    title: String,
    description: String,
    isCompleted: Boolean,
    isEnabled: Boolean = true,
    buttonText: String,
    onAction: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // Requirement 3: Theme-aware contrast color pairings for light & dark mode
    val cardBgColor = when {
        isCompleted && isDark -> Color(0xFF1B382B)
        isCompleted && !isDark -> Color(0xFFE8F5E9)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }

    val titleTextColor = when {
        isCompleted && isDark -> Color(0xFFA5D6A7)
        isCompleted && !isDark -> Color(0xFF1B5E20)
        isEnabled -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    }

    val descTextColor = when {
        isCompleted && isDark -> Color(0xFFC8E6C9)
        isCompleted && !isDark -> Color(0xFF2E7D32)
        isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Requirement 4: Keep step number intact, circle background changes to StepCompletedColor when granted
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) StepCompletedColor
                        else GradientStart
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$stepNumber",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = titleTextColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = descTextColor
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onAction,
                enabled = isEnabled && !isCompleted,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) StepCompletedColor else GradientStart,
                    contentColor = Color.White,
                    disabledContainerColor = if (isCompleted) StepCompletedColor.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = if (isCompleted) Color.White
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            ) {
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted || (isEnabled && !isCompleted)) Color.White
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    fontSize = 13.sp
                )
            }
        }
    }
}
