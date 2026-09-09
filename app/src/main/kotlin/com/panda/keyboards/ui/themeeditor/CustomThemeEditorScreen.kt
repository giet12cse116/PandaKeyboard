package com.panda.keyboards.ui.themeeditor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.panda.keyboards.theme.KeyShape
import com.panda.keyboards.ui.keyboards.KeyboardThemePreviewImage

/**
 * Screen providing interactive color pickers, key shape selection, and live
 * real-time [KeyboardThemePreviewImage] static image preview for building custom themes.
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

    val keyBgPresets = listOf(
        "#2D2D44", "#121212", "#1B4D3E", "#0F172A", "#4A0E17", "#3B2D54",
        "#2C3E50", "#1E272C", "#FFFFFF", "#F3F4F6", "#FEF3C7", "#E0E7FF"
    )
    val keyTextPresets = listOf(
        "#FFFFFF", "#000000", "#1E293B", "#312E81", "#A7F3D0", "#FDE047",
        "#38BDF8", "#FB7185", "#E2E8F0", "#FED7AA", "#C7D2FE", "#FBCFE8"
    )
    val bgStartPresets = listOf(
        "#1A1A2E", "#0B0C10", "#0B2B22", "#0F172A", "#2E0810", "#1E1B4B",
        "#0F2027", "#141E30", "#FFF0F5", "#E0F2F1", "#F5F7FA", "#F3E8FF"
    )
    val bgEndPresets = listOf(
        "#16213E", "#1F2833", "#1B4D3E", "#1E293B", "#581825", "#312E81",
        "#203A43", "#243B55", "#FFD1DC", "#B2DFDB", "#E4E7EB", "#E9D5FF"
    )
    val accentPresets = listOf(
        "#7C4DFF", "#10B981", "#F97316", "#EC4899", "#06B6D4", "#F59E0B",
        "#3B82F6", "#8B5CF6", "#EF4444", "#14B8A6", "#6366F1", "#D946EF"
    )

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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            viewModel.saveTheme(context, onThemeSaved)
                        },
                        enabled = !viewModel.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (viewModel.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            val buttonText = if (viewModel.themeName.isBlank()) {
                                "Save Theme"
                            } else {
                                "Save as \"${viewModel.themeName}\""
                            }
                            Text(
                                text = buttonText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You can find the theme in Keyboard section",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
                            Text("Upload Image")
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
                // ── Upload Image Tab ─────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Real-Time Live Preview Card (Static Image Canvas) ────
                    EditorSectionHeader(title = "Live Preview")

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.5f),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            KeyboardThemePreviewImage(
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
                        placeholder = { Text("e.g. My Custom Theme") },
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
                    EditorSectionHeader(title = "Shift & Enter")
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
                    // ── Real-Time Live Preview Card (Static Image Canvas) ────
                    Text(
                        text = "Live Preview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.5f),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            KeyboardThemePreviewImage(
                                theme = draftTheme
                            )
                        }
                    }

                    // ── Theme Name Input ─────────────────────────────────────
                    OutlinedTextField(
                        value = viewModel.themeName,
                        onValueChange = { viewModel.themeName = it },
                        label = { Text("Theme Name") },
                        placeholder = { Text("e.g. My Custom Theme") },
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

                    // ── Keyboard Background Swatches ─────────────────
                    EditorSectionHeader(title = "Keyboard Background Color")
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
                    EditorSectionHeader(title = "Shift & Enter")
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
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
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
