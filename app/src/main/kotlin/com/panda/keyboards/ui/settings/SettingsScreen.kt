package com.panda.keyboards.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panda.keyboards.theme.KeyboardHeight
import com.panda.keyboards.theme.QwertyOrder

/**
 * Settings Screen providing grouped preference controls, selection dialogs,
 * toggles, and support actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit = {},
    onOpenSetupDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showQwertyDialog by remember { mutableStateOf(false) }
    var stubTitle by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                .padding(top = innerPadding.calculateTopPadding())
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Section 1: General ──────────────────────────────────────────
            SettingsGroupHeader(title = "General")
            SettingsCard {
                SettingsItem(
                    title = "Widget Guide",
                    subtitle = "Learn how to add quick widgets to your home screen",
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    onClick = { stubTitle = "Widget Guide" }
                )
                SettingsDivider()
                SettingsItem(
                    title = "Language",
                    subtitle = "English (US) — Primary language",
                    icon = Icons.Default.Language,
                    onClick = { stubTitle = "Language Selection" }
                )
                SettingsDivider()
                SettingsItem(
                    title = "Keyboard Layout",
                    subtitle = settings.qwertyOrder.displayName,
                    icon = Icons.Default.Keyboard,
                    onClick = { showQwertyDialog = true }
                )
                SettingsDivider()
                SettingsSwitchItem(
                    title = "Number row",
                    subtitle = "Display a dedicated row of numbers above letter keys",
                    icon = Icons.Default.Numbers,
                    checked = settings.numberRowEnabled,
                    onCheckedChange = { viewModel.setNumberRowEnabled(it) }
                )
                SettingsDivider()
                SettingsItem(
                    title = "Keyboard Activation",
                    subtitle = "Enable and switch to Panda Keyboards",
                    icon = Icons.Default.CheckCircle,
                    onClick = onOpenSetupDialog
                )
            }


            // ── Section 2: Smart Typing ─────────────────────────────────────
            SettingsGroupHeader(title = "Smart Typing")
            SettingsCard {
                SettingsSwitchItem(
                    title = "Auto-Correct",
                    subtitle = "Automatically show word completion suggestions while typing",
                    icon = Icons.Default.Spellcheck,
                    checked = settings.autoCorrectionEnabled,
                    onCheckedChange = { viewModel.setAutoCorrectionEnabled(it) }
                )
                SettingsDivider()
                SettingsSwitchItem(
                    title = "Auto-Capitalization",
                    subtitle = "Capitalize the first letter after sentence punctuation (. ! ?)",
                    icon = Icons.Default.Spellcheck,
                    checked = settings.autoCapitalizationEnabled,
                    onCheckedChange = { viewModel.setAutoCapitalizationEnabled(it) }
                )
            }

            // ── Section 3: Privacy & Network ────────────────────────────────
            SettingsGroupHeader(title = "Privacy & Security")
            SettingsCard {
                SettingsSwitchItem(
                    title = "Offline Mode",
                    subtitle = "Strictly disable network access and force local bundled assets",
                    icon = Icons.Default.SignalCellularOff,
                    checked = settings.offlineModeEnabled,
                    onCheckedChange = { viewModel.setOfflineModeEnabled(it) }
                )
            }

            // ── Section 4: About & Support ──────────────────────────────────
            SettingsGroupHeader(title = "About & Support")
            SettingsCard {
                SettingsItem(
                    title = "Share with Friends",
                    subtitle = "Recommend Panda Keyboards to others",
                    icon = Icons.Default.Share,
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Check out Panda Keyboards — custom fonts & themes for Android!")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Panda Keyboards"))
                    }
                )
                SettingsDivider()
                SettingsItem(
                    title = "Privacy Policy",
                    subtitle = "Read our privacy guidelines",
                    icon = Icons.Default.PrivacyTip,
                    onClick = { stubTitle = "Privacy Policy" }
                )
                SettingsDivider()
                SettingsItem(
                    title = "Feedback",
                    subtitle = "Send us feedback or bug reports",
                    icon = Icons.Default.Feedback,
                    onClick = {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@pandakeyboards.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Panda Keyboards Feedback")
                        }
                        try {
                            context.startActivity(emailIntent)
                        } catch (e: Exception) {
                            stubTitle = "Feedback Email: support@pandakeyboards.com"
                        }
                    }
                )
                SettingsDivider()
                SettingsItem(
                    title = "Rate Us",
                    subtitle = "Rate 5 stars on Google Play",
                    icon = Icons.Default.Star,
                    onClick = {
                        val playStoreIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.panda.keyboards")
                        )
                        try {
                            context.startActivity(playStoreIntent)
                        } catch (e: Exception) {
                            stubTitle = "Play Store Storefront"
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Version Footer ──────────────────────────────────────────────
            Text(
                text = "Panda Keyboards v1.0.0 (Build 1)\nBuilt with Kotlin & Jetpack Compose",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ── QWERTZ / QWERTY Dialog ──────────────────────────────────────────────
    if (showQwertyDialog) {
        AlertDialog(
            onDismissRequest = { showQwertyDialog = false },
            title = { Text("Select Keyboard Layout") },
            text = {
                Column {
                    QwertyOrder.entries.forEach { order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setQwertyOrder(order)
                                    showQwertyDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (order == settings.qwertyOrder),
                                onClick = {
                                    viewModel.setQwertyOrder(order)
                                    showQwertyDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = order.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQwertyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }



    // ── Generic Stub Dialog ─────────────────────────────────────────────────
    stubTitle?.let { title ->
        AlertDialog(
            onDismissRequest = { stubTitle = null },
            title = { Text(title) },
            text = { Text("This feature or link is coming soon in Sprint 5!") },
            confirmButton = {
                TextButton(onClick = { stubTitle = null }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun SettingsGroupHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsDivider() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    ) {}
}
