package com.panda.keyboards.ui.setup

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panda.keyboards.ime.ImeStatus
import com.panda.keyboards.ime.ImeStatusChecker

// ── Brand colors for the setup dialog ───────────────────────────────────
private val GradientStart = Color(0xFF7C4DFF)
private val GradientEnd = Color(0xFFB388FF)
private val StepCompletedColor = Color(0xFF69F0AE)

/**
 * IME activation dialog with a 2-step setup flow.
 *
 * Guides the user through:
 * 1. Enabling Panda Keyboards in system input method settings
 * 2. Switching to Panda Keyboards as the active input method
 *
 * The dialog auto-dismisses once both steps are completed, driven
 * by [ImeStatusChecker]'s reactive [ImeStatus] flow.
 *
 * @param imeStatusChecker Provides reactive IME status updates.
 * @param onDismiss Called when the dialog should close (either user
 *        skips or setup is complete).
 */
@Composable
fun ImeSetupDialog(
    imeStatusChecker: ImeStatusChecker,
    onDismiss: () -> Unit
) {
    val imeStatus by imeStatusChecker.imeStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Auto-dismiss when fully configured
    LaunchedEffect(imeStatus.isFullyConfigured) {
        if (imeStatus.isFullyConfigured) {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Header ──────────────────────────────────────────
                Text(
                    text = "🐼",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Set Up Panda Keyboards",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Two quick steps to get started",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Step 1: Enable ───────────────────────────────────
                SetupStep(
                    stepNumber = 1,
                    title = "Enable Panda Keyboards",
                    description = "Turn on Panda Keyboards in your device's input method settings",
                    icon = Icons.Default.Keyboard,
                    isCompleted = imeStatus.isEnabled,
                    buttonLabel = if (imeStatus.isEnabled) "Enabled ✓" else "Enable",
                    onButtonClick = {
                        if (!imeStatus.isEnabled) {
                            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Step 2: Switch ───────────────────────────────────
                SetupStep(
                    stepNumber = 2,
                    title = "Switch to Panda Keyboards",
                    description = "Select Panda Keyboards as your active keyboard",
                    icon = Icons.Default.SwapHoriz,
                    isCompleted = imeStatus.isDefault,
                    isEnabled = imeStatus.isEnabled, // Can't switch before enabling
                    buttonLabel = if (imeStatus.isDefault) "Active ✓" else "Switch",
                    onButtonClick = {
                        if (imeStatus.isEnabled && !imeStatus.isDefault) {
                            val imm = context.getSystemService(
                                Context.INPUT_METHOD_SERVICE
                            ) as InputMethodManager
                            imm.showInputMethodPicker()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Skip button ──────────────────────────────────────
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Skip for now",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * A single numbered setup step with title, description, status indicator,
 * and action button.
 */
@Composable
private fun SetupStep(
    stepNumber: Int,
    title: String,
    description: String,
    icon: ImageVector,
    isCompleted: Boolean,
    isEnabled: Boolean = true,
    buttonLabel: String,
    onButtonClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isCompleted) StepCompletedColor.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Step number / checkmark indicator
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted)
                        Brush.linearGradient(listOf(StepCompletedColor, StepCompletedColor))
                    else
                        Brush.linearGradient(listOf(GradientStart, GradientEnd))
                ),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = isCompleted,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (!isCompleted) {
                Text(
                    text = "$stepNumber",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and description
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isEnabled)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isEnabled)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Action button
        Button(
            onClick = onButtonClick,
            enabled = isEnabled && !isCompleted,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCompleted) StepCompletedColor else GradientStart,
                disabledContainerColor = if (isCompleted) StepCompletedColor.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            ),
            contentPadding = ButtonDefaults.ContentPadding
        ) {
            Text(
                text = buttonLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
