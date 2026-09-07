package com.panda.keyboards.ui.themeeditor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.keyboards.ime.keyboard.PandaKeyboardLayout
import com.panda.keyboards.theme.KeyShape

/**
 * Screen providing interactive color pickers, key shape selection, and live
 * real-time [PandaKeyboardLayout] preview for building custom themes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomThemeEditorScreen(
    viewModel: CustomThemeEditorViewModel,
    onBackClick: () -> Unit,
    onThemeSaved: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val draftTheme = viewModel.currentDraftTheme

    val keyBgPresets = listOf("#2D2D44", "#121212", "#1B4D3E", "#0F172A", "#4A0E17", "#3B2D54")
    val keyTextPresets = listOf("#FFFFFF", "#A7F3D0", "#FDE047", "#38BDF8", "#FB7185", "#E2E8F0")
    val bgStartPresets = listOf("#1A1A2E", "#0B0C10", "#0B2B22", "#0F172A", "#2E0810", "#1E1B4B")
    val bgEndPresets = listOf("#16213E", "#1F2833", "#1B4D3E", "#1E293B", "#581825", "#312E81")
    val accentPresets = listOf("#7C4DFF", "#10B981", "#F97316", "#EC4899", "#06B6D4", "#F59E0B")

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Custom Theme Editor",
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
                actions = {
                    Button(
                        onClick = {
                            viewModel.saveTheme(context, onThemeSaved)
                        },
                        enabled = !viewModel.isSaving,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (viewModel.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(text = "Save", fontWeight = FontWeight.Bold)
                        }
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
        ) {
            // ── Tab Switcher: Choose Colors vs Upload Image ────────────────
            TabRow(selectedTabIndex = viewModel.selectedTab) {
                Tab(
                    selected = viewModel.selectedTab == 0,
                    onClick = { viewModel.selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ColorLens, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Choose Colors")
                        }
                    }
                )
                Tab(
                    selected = viewModel.selectedTab == 1,
                    onClick = { viewModel.selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Image (Sprint 8b)")
                        }
                    }
                )
            }

            // ── ActivityResultLauncher for Photo Picker (Android Recommended PickVisualMedia) ──
            val photoPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri != null) {
                    viewModel.onImagePicked(context, uri)
                }
            }

            // ── Crop Screen Overlay Mode ─────────────────────────────────────
            val sourceBitmapForCrop = viewModel.imageCropSourceBitmap
            if (sourceBitmapForCrop != null) {
                ImageCropScreen(
                    sourceBitmap = sourceBitmapForCrop,
                    onCancel = { viewModel.onCropCancelled() },
                    onConfirmCrop = { cropped -> viewModel.onCropConfirmed(context, cropped) }
                )
            } else if (viewModel.selectedTab == 1) {
                // ── Upload Image Tab (Sprint 8b) ─────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Real-Time Live Preview Card ──────────────────────────
                    EditorSectionHeader(title = "Live Preview")

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            PandaKeyboardLayout(
                                theme = draftTheme
                            )
                        }
                    }

                    // ── Photo Picker Trigger Card ────────────────────────────
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (viewModel.croppedImagePath != null) "Custom Image Loaded" else "Select Background Image",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (viewModel.croppedImagePath != null) "Photo cropped to keyboard 1.5:1 ratio" else "Pick a photo from your gallery and crop it to fit the keyboard.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (viewModel.croppedImagePath != null) "Change Photo" else "Pick Photo")
                            }
                        }
                    }

                    // ── Theme Name Input ─────────────────────────────────────
                    OutlinedTextField(
                        value = viewModel.themeName,
                        onValueChange = { viewModel.themeName = it },
                        label = { Text("Theme Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // ── Key Shape Selector ───────────────────────────────────
                    EditorSectionHeader(title = "Key Shape")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KeyShape.entries.forEach { shape ->
                            FilterChip(
                                selected = (viewModel.keyShape == shape),
                                onClick = { viewModel.keyShape = shape },
                                label = { Text(shape.name) }
                            )
                        }
                    }

                    // ── Key Background Color Swatches ────────────────────────
                    EditorSectionHeader(title = "Key Background Color")
                    ColorSwatchRow(
                        presets = keyBgPresets,
                        selectedColorHex = viewModel.keyBackgroundColor,
                        onColorSelected = { viewModel.keyBackgroundColor = it }
                    )

                    // ── Key Text Color Swatches ──────────────────────────────
                    EditorSectionHeader(title = "Key Text Color")
                    ColorSwatchRow(
                        presets = keyTextPresets,
                        selectedColorHex = viewModel.keyTextColor,
                        onColorSelected = { viewModel.keyTextColor = it }
                    )

                    // ── Accent Color Swatches ────────────────────────────────
                    EditorSectionHeader(title = "Accent Color (Shift & Enter)")
                    ColorSwatchRow(
                        presets = accentPresets,
                        selectedColorHex = viewModel.accentColor,
                        onColorSelected = { viewModel.accentColor = it }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                // ── Color Customization Path ─────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Real-Time Live Preview Card ──────────────────────────
                    Text(
                        text = "Live Preview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            PandaKeyboardLayout(
                                theme = draftTheme
                            )
                        }
                    }

                    // ── Theme Name Input ─────────────────────────────────────
                    OutlinedTextField(
                        value = viewModel.themeName,
                        onValueChange = { viewModel.themeName = it },
                        label = { Text("Theme Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // ── Key Shape Selector ───────────────────────────────────
                    EditorSectionHeader(title = "Key Shape")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KeyShape.entries.forEach { shape ->
                            FilterChip(
                                selected = (viewModel.keyShape == shape),
                                onClick = { viewModel.keyShape = shape },
                                label = { Text(shape.name) }
                            )
                        }
                    }

                    // ── Key Background Color Swatches ────────────────────────
                    EditorSectionHeader(title = "Key Background Color")
                    ColorSwatchRow(
                        presets = keyBgPresets,
                        selectedColorHex = viewModel.keyBackgroundColor,
                        onColorSelected = { viewModel.keyBackgroundColor = it }
                    )

                    // ── Key Text Color Swatches ──────────────────────────────
                    EditorSectionHeader(title = "Key Text Color")
                    ColorSwatchRow(
                        presets = keyTextPresets,
                        selectedColorHex = viewModel.keyTextColor,
                        onColorSelected = { viewModel.keyTextColor = it }
                    )

                    // ── Keyboard Surface Background Swatches ─────────────────
                    EditorSectionHeader(title = "Keyboard Surface Background")
                    ColorSwatchRow(
                        presets = bgStartPresets,
                        selectedColorHex = viewModel.keyboardBackgroundStartHex,
                        onColorSelected = { selected ->
                            viewModel.keyboardBackgroundStartHex = selected
                            val idx = bgStartPresets.indexOf(selected)
                            if (idx >= 0 && idx < bgEndPresets.size) {
                                viewModel.keyboardBackgroundEndHex = bgEndPresets[idx]
                            }
                        }
                    )

                    // ── Accent Color Swatches ────────────────────────────────
                    EditorSectionHeader(title = "Accent Color (Shift & Enter)")
                    ColorSwatchRow(
                        presets = accentPresets,
                        selectedColorHex = viewModel.accentColor,
                        onColorSelected = { viewModel.accentColor = it }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun EditorSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun ColorSwatchRow(
    presets: List<String>,
    selectedColorHex: String,
    onColorSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        presets.forEach { hex ->
            val color = parseHexColor(hex)
            val isSelected = hex.equals(selectedColorHex, ignoreCase = true)

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(hex) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = if (isLightColor(color)) Color.Black else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun parseHexColor(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = cleanHex.toLong(16)
        if (cleanHex.length == 6) {
            Color((0xFF000000 or colorInt).toInt())
        } else {
            Color(colorInt.toInt())
        }
    } catch (e: Exception) {
        Color.Gray
    }
}

private fun isLightColor(color: Color): Boolean {
    val luminance = (0.299f * color.red) + (0.587f * color.green) + (0.114f * color.blue)
    return luminance > 0.5f
}
