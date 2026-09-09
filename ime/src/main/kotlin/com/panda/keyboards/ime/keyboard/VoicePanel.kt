package com.panda.keyboards.ime.keyboard

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Dedicated in-keyboard Voice Recording UI replacing the main keyboard view.
 *
 * Renders:
 * - Real-time amplitude waveform visualizer driven by [rmsDb]
 * - Live partial speech transcription feedback
 * - "Listening..." / "Processing..." status states
 * - Inline fallback messaging for permission denial and offline mode constraints
 * - Prominent Cancel (X) and Done (✓) control actions
 */
@Composable
fun VoicePanel(
    statusText: String,
    partialText: String,
    rmsDb: Float,
    isRecording: Boolean,
    hasPermission: Boolean,
    isOfflineUnavailable: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    resolvedTheme: ResolvedTheme,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Top Header Row ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎙️ Voice Input",
                    color = resolvedTheme.keyText,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onCancelRecording() },
                    shape = CircleShape,
                    color = resolvedTheme.specialKeyBackground
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "✕",
                            color = resolvedTheme.specialKeyText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Center Content Area ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    // State A: Permission Denied
                    !hasPermission -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "🎙️",
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Microphone access needed — enable it in system settings",
                                color = resolvedTheme.keyText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onRequestPermission() },
                                    shape = RoundedCornerShape(8.dp),
                                    color = resolvedTheme.accent
                                ) {
                                    Text(
                                        text = "Grant Permission",
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onOpenSettings() },
                                    shape = RoundedCornerShape(8.dp),
                                    color = resolvedTheme.specialKeyBackground
                                ) {
                                    Text(
                                        text = "Open Settings",
                                        color = resolvedTheme.specialKeyText,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // State B: Offline Mode Active & On-Device Voice Unavailable
                    isOfflineUnavailable -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "🌐❌",
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Offline mode is active and on-device speech recognition is unavailable. Disable Offline Mode in Settings to use voice input.",
                                color = resolvedTheme.keyText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onCancelRecording() },
                                shape = RoundedCornerShape(8.dp),
                                color = resolvedTheme.specialKeyBackground
                            ) {
                                Text(
                                    text = "Back to Keyboard",
                                    color = resolvedTheme.specialKeyText,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // State C: Active Recording / Listening View
                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Large Centered Mic Button
                            Surface(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        if (isRecording) onStopRecording() else onStartRecording()
                                    },
                                shape = CircleShape,
                                color = if (isRecording) resolvedTheme.accent else resolvedTheme.specialKeyBackground,
                                shadowElevation = 4.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "🎙️",
                                        fontSize = 30.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Status Indicator
                            Text(
                                text = statusText.ifEmpty { if (isRecording) "Listening..." else "Tap mic to speak" },
                                color = resolvedTheme.keyText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Live Amplitude Waveform Visualizer (7 Bars driven by real rmsDb)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.height(44.dp)
                            ) {
                                val factors = listOf(0.4f, 0.7f, 1.0f, 1.3f, 1.0f, 0.7f, 0.4f)
                                factors.forEachIndexed { index, factor ->
                                    val normalizedRms = if (isRecording) (rmsDb.coerceIn(0f, 10f) / 10f) * factor else 0.1f
                                    val baseHeight = 8.dp
                                    val dynamicAdd = 32.dp
                                    val barHeight by animateDpAsState(
                                        targetValue = baseHeight + (dynamicAdd * normalizedRms.coerceIn(0.1f, 1.0f)),
                                        label = "waveBar_$index"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .width(6.dp)
                                            .height(barHeight)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(
                                                if (isRecording) resolvedTheme.accent else resolvedTheme.specialKeyText.copy(alpha = 0.3f)
                                            )
                                    )
                                }
                            }

                            // Live Partial Transcription Display
                            if (partialText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = resolvedTheme.specialKeyBackground.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = "\"$partialText\"",
                                        color = resolvedTheme.keyText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = FontFamily.Default,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Bottom Action Footer ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel Button
                Surface(
                    modifier = Modifier
                        .height(38.dp)
                        .weight(1f)
                        .padding(end = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onCancelRecording() },
                    shape = RoundedCornerShape(10.dp),
                    color = resolvedTheme.specialKeyBackground
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Cancel",
                            color = resolvedTheme.specialKeyText,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Done / Stop Checkmark Button
                Surface(
                    modifier = Modifier
                        .height(38.dp)
                        .weight(1f)
                        .padding(start = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onStopRecording() },
                    shape = RoundedCornerShape(10.dp),
                    color = resolvedTheme.accent
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Done ✓",
                            color = Color.White,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
