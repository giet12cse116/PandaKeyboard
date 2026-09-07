package com.panda.keyboards.ui.fonts

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.panda.keyboards.ime.ImeStatusChecker

private val PrimaryViolet = Color(0xFF7C4DFF)
private val StepCompletedColor = Color(0xFF2E7D32)

/**
 * Dedicated IME setup dialog for the Fonts screen.
 *
 * Appears when the user taps on the font preview EditText without Panda Keyboard
 * being fully configured (enabled and selected as default).
 *
 * Operates independently from the Keyboards gallery setup flow.
 */
@Composable
fun FontSetupDialog(
    imeStatusChecker: ImeStatusChecker,
    onDismiss: () -> Unit
) {
    val imeStatus by imeStatusChecker.imeStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Auto-dismiss when both enabling and selection steps are completed
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
                .padding(horizontal = 20.dp),
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
                // ── Header Icon & Title ───────────────────────────────
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🔤", fontSize = 32.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Activate Panda Keyboard",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "To type with custom fonts across all your apps, Panda Keyboard needs to be enabled and selected as your active input method.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Step 1: Enable ───────────────────────────────────
                FontSetupStepCard(
                    stepNumber = 1,
                    title = "Enable Panda Keyboard",
                    description = "Turn on in system input settings",
                    isCompleted = imeStatus.isEnabled,
                    buttonLabel = if (imeStatus.isEnabled) "Enabled ✓" else "Enable",
                    onButtonClick = {
                        if (!imeStatus.isEnabled) {
                            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ── Step 2: Select ───────────────────────────────────
                FontSetupStepCard(
                    stepNumber = 2,
                    title = "Select Panda Keyboard",
                    description = "Set as active default keyboard",
                    isCompleted = imeStatus.isDefault,
                    isEnabled = imeStatus.isEnabled,
                    buttonLabel = if (imeStatus.isDefault) "Active ✓" else "Select",
                    onButtonClick = {
                        if (imeStatus.isEnabled && !imeStatus.isDefault) {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                            imm?.showInputMethodPicker()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ── Dismiss Button ───────────────────────────────────
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Maybe Later",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun FontSetupStepCard(
    stepNumber: Int,
    title: String,
    description: String,
    isCompleted: Boolean,
    isEnabled: Boolean = true,
    buttonLabel: String,
    onButtonClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBgColor)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circle with step number
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) StepCompletedColor
                    else PrimaryViolet
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$stepNumber",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

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
                color = descTextColor,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onButtonClick,
            enabled = isEnabled && !isCompleted,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCompleted) StepCompletedColor else PrimaryViolet,
                contentColor = Color.White,
                disabledContainerColor = if (isCompleted) StepCompletedColor.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                disabledContentColor = if (isCompleted) Color.White
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
        ) {
            Text(
                text = buttonLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCompleted || (isEnabled && !isCompleted)) Color.White
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
        }
    }
}
