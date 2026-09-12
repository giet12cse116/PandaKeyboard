package com.panda.keyboards.ui.themeeditor

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.East
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.South
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.keyboards.theme.KeyShape
import com.panda.keyboards.ui.keyboards.KeyboardThemePreviewImage
import kotlin.math.roundToInt

private data class WallpaperPreset(
    val title: String,
    val subtitle: String,
    val assetPath: String,
    val badgeText: String? = null
)

private val abstractPresets = listOf(
    WallpaperPreset("Cyber Waves", "Abstract Digital", "themes/abstract/1000299774_Default.jpg", "HOT"),
    WallpaperPreset("Cosmic Flow", "Neon Galaxy", "themes/abstract/1000299775_Default.jpg"),
    WallpaperPreset("Liquid Gold", "Luxury Texture", "themes/abstract/1000299776_Default.jpg", "POPULAR"),
    WallpaperPreset("Neon Prism", "Vibrant Ray", "themes/abstract/1000299779_Default.jpg"),
    WallpaperPreset("Dark Velvet", "Deep Crimson", "themes/abstract/1000299780_Default.jpg"),
    WallpaperPreset("Sunset Blur", "Warm Gradient", "themes/abstract/1000299781_Default.jpg"),
    WallpaperPreset("Ocean Vortex", "Deep Emerald", "themes/abstract/1000299782_Default.jpg"),
    WallpaperPreset("Pastel Aura", "Soft Violet", "themes/abstract/1000299783_Default.jpg"),
    WallpaperPreset("Golden Spark", "Gilded Sparkle", "themes/abstract/1000299608_Default.jpg"),
    WallpaperPreset("Cyber Grid", "Tech Matrix", "themes/abstract/1000299609_Default.jpg")
)

private val hdWallpaperPresets = listOf(
    WallpaperPreset("Coral Reef", "Vibrant Oceanic", "themes/abstract/1000299639_Default.jpg", "HD"),
    WallpaperPreset("Sunset Pulse", "Solar Flare", "themes/abstract/1000299967_Default.jpg"),
    WallpaperPreset("Ocean Surge", "Cyan Waves", "themes/abstract/1000299966_Default.jpg", "POPULAR"),
    WallpaperPreset("Neon Glow", "Electric Magenta", "themes/abstract/1000299965_Default.jpg"),
    WallpaperPreset("Cosmic Aura", "Purple Nebula", "themes/abstract/1000299964_Default.jpg"),
    WallpaperPreset("Candy Dream", "Sweet Pastel", "themes/abstract/1000299623_Default.jpg"),
    WallpaperPreset("Neon Pulse", "Synth Cyber", "themes/abstract/1000299605_Default.jpg"),
    WallpaperPreset("Deep Ocean", "Midnight Abyss", "themes/abstract/1000299613_Default.jpg"),
    WallpaperPreset("Purple Nebula", "Starry Night", "themes/abstract/1000299606_Default.jpg"),
    WallpaperPreset("Sunset Flame", "Fiery Horizon", "themes/abstract/1000299612_Default.jpg")
)

private val naturePresets = listOf(
    WallpaperPreset("Amethyst Haze", "Crystal Mist", "themes/abstract/1000299635_Default.jpg", "NATURE"),
    WallpaperPreset("Shadow Realm", "Muted Twilight", "themes/abstract/1000299644_Default.jpg"),
    WallpaperPreset("Frost Crystal", "Ice Shimmer", "themes/abstract/1000299643_Default.jpg"),
    WallpaperPreset("Obsidian Night", "Dark Slate", "themes/abstract/1000299640_Default.jpg"),
    WallpaperPreset("Solar Flare", "Golden Sunrise", "themes/abstract/1000299629_Default.jpg"),
    WallpaperPreset("Sapphire Haze", "Blue Serenity", "themes/abstract/1000299971_Default.jpg"),
    WallpaperPreset("Sapphire Mist", "Oceanic Breeze", "themes/abstract/1000299627_Default.jpg"),
    WallpaperPreset("Mint Breeze", "Fresh Leaf", "themes/abstract/1000299632_Default.jpg", "POPULAR"),
    WallpaperPreset("Aurora Borealis", "Northern Lights", "themes/abstract/1000299625_Default.jpg"),
    WallpaperPreset("Golden Sunset", "Warm Amber", "themes/abstract/1000299636_Default.jpg"),
    WallpaperPreset("Flower Dream", "Blossom Pink", "themes/abstract/1000299970_Default.jpg"),
    WallpaperPreset("Emerald Mist", "Forest Rain", "themes/abstract/1000299969_Default.jpg"),
    WallpaperPreset("Crimson Wave", "Red Sunset", "themes/abstract/1000299633_Default.jpg"),
    WallpaperPreset("Cosmic Dust", "Violet Starlight", "themes/abstract/1000299631_Default.jpg"),
    WallpaperPreset("Aqua Vortex", "Turquoise Flow", "themes/abstract/1000299637_Default.jpg")
)

// Stitch Brand Palette Colors
private val EmeraldPrimary = Color(0xFF10B981)
private val EmeraldDark = Color(0xFF006C49)
private val PlayfulViolet = Color(0xFF8B5CF6)
private val SolarAmber = Color(0xFFF59E0B)
private val SurfaceSlate = Color(0xFFF8FAFC)
private val DeepSlate = Color(0xFF0F172A)
private val SoftSlateBg = Color(0xFFE7EEFF)
private val PrimaryFixedContainer = Color(0xFF6FFBBE)
private val OnPrimaryFixed = Color(0xFF002113)

/**
 * 3-Step Custom Theme Studio Screen matching exact Google Stitch design prototype.
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
    var showCustomColorDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var customHexInput by remember { mutableStateOf("#10B981") }

    BackHandler(enabled = true) {
        showDiscardDialog = true
    }

    val fontColorPresets = listOf(
        "#FFFFFF" to "White",
        "#000000" to "Black",
        "#64748B" to "Gray",
        "#EF4444" to "Red",
        "#F97316" to "Orange",
        "#F59E0B" to "Yellow",
        "#10B981" to "Green",
        "#3B82F6" to "Blue",
        "#8B5CF6" to "Purple",
        "#EC4899" to "Pink"
    )

    val colorPresets = listOf(
        "#10B981", "#8B5CF6", "#F59E0B", "#0F172A", "#1E293B",
        "#3B82F6", "#EC4899", "#EF4444", "#14B8A6", "#6366F1",
        "#FFFFFF", "#F1F5F9", "#FEF3C7", "#E0E7FF", "#FBCFE8"
    )

    val classicKeyBackgroundIcons = listOf(
        "ic_classic_key_base_leather_square" to "Leather",
        "ic_classic_key_blue_gorgonzola" to "Gorgonzola",
        "ic_classic_key_deep_blue_enter" to "Deep Blue",
        "ic_classic_key_pizza_standard" to "Pizza Std",
        "ic_classic_special_origami_backspace_pink" to "Pink",
        "ic_classic_special_origami_enter_blue" to "Blue",
        "ic_classic_special_origami_shift_peach" to "Peach",
        "ic_classic_standard_origami_mint" to "Mint",
        "ic_classic_standard_origami_peach" to "Peach Bg",
        "ic_classic_standard_origami_purple" to "Purple"
    )

    val popularKeyBackgroundIcons = listOf(
        "ic_popular_panda" to "Panda",
        "ic_popular_pig" to "Pig",
        "ic_popular_burger" to "Burger",
        "ic_popular_pizza" to "Pizza",
        "ic_popular_cookie" to "Cookie",
        "ic_popular_donut" to "Donut",
        "ic_popular_cheesecake" to "Cheesecake",
        "ic_popular_mango" to "Mango",
        "ic_popular_heart" to "Heart",
        "ic_popular_star" to "Star",
        "ic_popular_sun" to "Sun",
        "ic_popular_sunflower" to "Sunflower",
        "ic_popular_flower" to "Flower",
        "ic_popular_fire" to "Fire",
        "ic_popular_earth" to "Earth",
        "ic_popular_butterfly" to "Butterfly",
        "ic_popular_foot_ball" to "Football",
        "ic_popular_basketball" to "Basketball",
        "ic_popular_gift_box" to "Gift",
        "ic_popular_pizza_kb_numberpad" to "Pad",
        "ic_popular_pizza_kb_p" to "Pizza P"
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onImagePicked(context, uri)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (!viewModel.showCropScreen) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🐼", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Create Custom Theme",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            showDiscardDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },

        bottomBar = {
            if (!viewModel.showCropScreen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    val hasSelectedBackgroundInStep1 = when (viewModel.backgroundMode) {
                        0 -> viewModel.croppedImagePath != null
                        1 -> true
                        2 -> viewModel.selectedBuiltinImage != null
                        else -> false
                    }
                    val showPreviewAndNextButton = (viewModel.currentStep > 1 || hasSelectedBackgroundInStep1)

                    // Fixed Live Preview Board directly above the Next button
                    if (showPreviewAndNextButton && viewModel.showPreviewBoard) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
                                .aspectRatio(1.6f),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                KeyboardThemePreviewImage(theme = draftTheme)
                            }
                        }
                    }

                    // ── Persistent Bottom Action Dock ("Next: Keys Style") ──────────────────
                    Surface(
                        tonalElevation = 8.dp,
                        shadowElevation = 12.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Back Button (Step 2 and Step 3 only)
                            if (viewModel.currentStep > 1) {
                                androidx.compose.material3.OutlinedButton(
                                    onClick = { viewModel.previousStep() },
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.height(48.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Back",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Next / Apply Action Button
                            if (viewModel.currentStep < 3) {
                                if (showPreviewAndNextButton) {
                                    Button(
                                        onClick = { viewModel.nextStep() },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = if (viewModel.currentStep == 1) "Next: Keys Style" else "Next: Font & Text",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.saveTheme(context, onThemeSaved)
                                    },
                                    enabled = !viewModel.isSaving,
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    if (viewModel.isSaving) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Apply Theme",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // Toggle Preview Keyboard Button on the RIGHT side of Next Button
                            if (showPreviewAndNextButton) {
                                androidx.compose.material3.OutlinedButton(
                                    onClick = { viewModel.showPreviewBoard = !viewModel.showPreviewBoard },
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.height(48.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.5.dp,
                                        if (viewModel.showPreviewBoard) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (viewModel.showPreviewBoard) EmeraldPrimary.copy(alpha = 0.12f) else Color.Transparent
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Keyboard,
                                            contentDescription = if (viewModel.showPreviewBoard) "Hide Preview" else "Show Preview",
                                            tint = if (viewModel.showPreviewBoard) EmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (viewModel.showPreviewBoard) "Hide" else "Preview",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (viewModel.showPreviewBoard) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (showDiscardDialog) {
            AlertDialog(
                onDismissRequest = { showDiscardDialog = false },
                title = {
                    Text(
                        text = "Discard design?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to discard your custom keyboard design? All unsaved changes will be lost.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDiscardDialog = false
                            onBackClick()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Discard", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDiscardDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Keep Editing", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        if (viewModel.showCropScreen && viewModel.uncroppedSourceBitmap != null) {
            ImageCropScreen(
                sourceBitmap = viewModel.uncroppedSourceBitmap!!,
                onCancel = { viewModel.onCropCancelled() },
                onConfirmCrop = { cropped -> viewModel.onCropConfirmed(context, cropped) }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Progress Stepper Wizard Card ─────────────────────────────
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Step 1 Pill
                        StepChipStitch(
                            stepNumber = 1,
                            label = "Background",
                            isActive = (viewModel.currentStep == 1),
                            isCompleted = (viewModel.currentStep > 1)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .padding(horizontal = 6.dp)
                                .background(if (viewModel.currentStep > 1) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant)
                        )
                        // Step 2 Pill
                        StepChipStitch(
                            stepNumber = 2,
                            label = "Keys",
                            isActive = (viewModel.currentStep == 2),
                            isCompleted = (viewModel.currentStep > 2)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .padding(horizontal = 6.dp)
                                .background(if (viewModel.currentStep > 2) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant)
                        )
                        // Step 3 Pill
                        StepChipStitch(
                            stepNumber = 3,
                            label = "Font",
                            isActive = (viewModel.currentStep == 3),
                            isCompleted = false
                        )
                    }
                }

                // ── Screen Title & Subtitle Header ──────────────────────────
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (viewModel.currentStep) {
                                1 -> "Keyboard Background"
                                2 -> "Key Style"
                                else -> "Font & Text"
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = PrimaryFixedContainer,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = OnPrimaryFixed,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (viewModel.currentStep) {
                            1 -> "Choose an eye-catching canvas or craft your custom visual style."
                            2 -> "Customize key shapes, decorative panda icons, and opacity."
                            else -> "Customize font color, typography style, size, and shadow."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }


                when (viewModel.currentStep) {
                    // ── STEP 1: KEYBOARD BACKGROUND ─────────────────────────
                    1 -> {
                        // ── 2 Banners Side-by-Side: Upload Image & Classic Keyboards ──────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Banner 1: Upload Image (Acts as direct gallery launcher button)
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        viewModel.backgroundMode = 0
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = EmeraldPrimary.copy(alpha = 0.15f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AddPhotoAlternate,
                                                contentDescription = null,
                                                tint = EmeraldPrimary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Upload Image",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (viewModel.croppedImagePath != null) "Image Selected" else "Tap to open gallery",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Banner 2: Classic Keyboards (Renamed from Classic Color)
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        viewModel.backgroundMode = 1
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = PlayfulViolet.copy(alpha = 0.15f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Gradient,
                                                contentDescription = null,
                                                tint = PlayfulViolet,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Classic Keyboards",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Solid or gradient",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // When Classic Keyboards is active (backgroundMode == 1):
                        if (viewModel.backgroundMode == 1) {
                            // All Classic Color Customization Options
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Color Type",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        FilterChip(selected = (viewModel.colorType == 0), onClick = { viewModel.colorType = 0 }, label = { Text("Solid") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary, selectedLabelColor = Color.White), modifier = Modifier.weight(1f))
                                        FilterChip(selected = (viewModel.colorType == 1), onClick = { viewModel.colorType = 1 }, label = { Text("2-Gradient") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary, selectedLabelColor = Color.White), modifier = Modifier.weight(1f))
                                        FilterChip(selected = (viewModel.colorType == 2), onClick = { viewModel.colorType = 2 }, label = { Text("3-Gradient") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary, selectedLabelColor = Color.White), modifier = Modifier.weight(1f))
                                    }

                                    Text(
                                        text = "Gradient Direction",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        FilterChip(selected = (viewModel.gradientDirection == 0), onClick = { viewModel.gradientDirection = 0 }, label = { Text("Horizontal") }, leadingIcon = { Icon(Icons.Default.East, contentDescription = null, modifier = Modifier.size(14.dp)) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary, selectedLabelColor = Color.White), modifier = Modifier.weight(1f))
                                        FilterChip(selected = (viewModel.gradientDirection == 1), onClick = { viewModel.gradientDirection = 1 }, label = { Text("Vertical") }, leadingIcon = { Icon(Icons.Default.South, contentDescription = null, modifier = Modifier.size(14.dp)) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary, selectedLabelColor = Color.White), modifier = Modifier.weight(1f))
                                        FilterChip(selected = (viewModel.gradientDirection == 2), onClick = { viewModel.gradientDirection = 2 }, label = { Text("Diagonal") }, leadingIcon = { Icon(Icons.Default.SouthEast, contentDescription = null, modifier = Modifier.size(14.dp)) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary, selectedLabelColor = Color.White), modifier = Modifier.weight(1f))
                                    }

                                    Text(text = "Color 1", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    ColorSwatchRow(presets = colorPresets, selectedColorHex = viewModel.colorStop1Hex, onColorSelected = { viewModel.colorStop1Hex = it })

                                    if (viewModel.colorType >= 1) {
                                        Text(text = "Color 2", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        ColorSwatchRow(presets = colorPresets, selectedColorHex = viewModel.colorStop2Hex, onColorSelected = { viewModel.colorStop2Hex = it })
                                    }

                                    if (viewModel.colorType == 2) {
                                        Text(text = "Color 3", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        ColorSwatchRow(presets = colorPresets, selectedColorHex = viewModel.colorStop3Hex, onColorSelected = { viewModel.colorStop3Hex = it })
                                    }
                                }
                            }
                        }



                        // Option 3: Choose Background Card (Featured - Selected by Default)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { viewModel.backgroundMode = 2 },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = PrimaryFixedContainer,
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Wallpaper,
                                                    contentDescription = null,
                                                    tint = OnPrimaryFixed,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Choose Background",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = EmeraldPrimary
                                                ) {
                                                    Text(
                                                        text = "Featured",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Curated HD textures, bamboo gradients",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                if (viewModel.backgroundMode == 2) {
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // ── Section 1: Abstract Category ───────────────────────
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Palette,
                                                contentDescription = null,
                                                tint = EmeraldPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Abstract",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = "${abstractPresets.size} Presets",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Abstract Grid (Grid of 2)
                                    abstractPresets.chunked(2).forEach { rowPresets ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            rowPresets.forEach { item ->
                                                StitchWallpaperCard(
                                                    title = item.title,
                                                    subtitle = item.subtitle,
                                                    assetPath = item.assetPath,
                                                    isSelected = (viewModel.selectedBuiltinImage == item.assetPath),
                                                    badgeText = item.badgeText,
                                                    modifier = Modifier.weight(1f),
                                                    onClick = { viewModel.selectedBuiltinImage = item.assetPath }
                                                )
                                            }
                                            if (rowPresets.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // ── Section 2: HD Wallpapers Category ───────────────────
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.PhotoLibrary,
                                                contentDescription = null,
                                                tint = EmeraldPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "HD Wallpapers",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = "${hdWallpaperPresets.size} Presets",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // HD Wallpapers Grid (Grid of 2)
                                    hdWallpaperPresets.chunked(2).forEach { rowPresets ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            rowPresets.forEach { item ->
                                                StitchWallpaperCard(
                                                    title = item.title,
                                                    subtitle = item.subtitle,
                                                    assetPath = item.assetPath,
                                                    isSelected = (viewModel.selectedBuiltinImage == item.assetPath),
                                                    badgeText = item.badgeText,
                                                    modifier = Modifier.weight(1f),
                                                    onClick = { viewModel.selectedBuiltinImage = item.assetPath }
                                                )
                                            }
                                            if (rowPresets.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // ── Section 3: Nature Category ─────────────────────────
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Forest,
                                                contentDescription = null,
                                                tint = EmeraldPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Nature",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = "${naturePresets.size} Presets",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Nature Grid (Grid of 2)
                                    naturePresets.chunked(2).forEach { rowPresets ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            rowPresets.forEach { item ->
                                                StitchWallpaperCard(
                                                    title = item.title,
                                                    subtitle = item.subtitle,
                                                    assetPath = item.assetPath,
                                                    isSelected = (viewModel.selectedBuiltinImage == item.assetPath),
                                                    badgeText = item.badgeText,
                                                    modifier = Modifier.weight(1f),
                                                    onClick = { viewModel.selectedBuiltinImage = item.assetPath }
                                                )
                                            }
                                            if (rowPresets.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }
                                }
                            }
                        }

                        if (viewModel.backgroundMode == 0 && viewModel.croppedImagePath != null) {
                            androidx.compose.material3.OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Re-crop or Change Image",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // ── STEP 2: KEY STYLE ───────────────────────────────────
                    2 -> {
                        EditorSectionHeader(title = "Key Background Type")
                        val shapesList = listOf(
                            KeyShape.NONE to "None",
                            KeyShape.SQUARE to "Square",
                            KeyShape.SQUARE_ROUNDED to "Rounded",
                            KeyShape.OVAL to "Oval",
                            KeyShape.OVAL_ROUNDED to "Oval Round"
                        )

                        val currentBgColor = remember(viewModel.keyBackgroundColor) {
                            parseHexColor(viewModel.keyBackgroundColor)
                        }
                        val currentTextColor = remember(viewModel.keyTextColor) {
                            parseHexColor(viewModel.keyTextColor)
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(shapesList) { (shape, name) ->
                                val isSelected = (viewModel.keyShape == shape && viewModel.decorativeIcon == null) || (shape == KeyShape.NONE && !viewModel.decorativeIcon.isNullOrEmpty())
                                val cornerRadius = when (shape) {
                                    KeyShape.NONE -> 0.dp
                                    KeyShape.SQUARE -> 3.dp
                                    KeyShape.SQUARE_ROUNDED -> 8.dp
                                    KeyShape.OVAL -> 22.dp
                                    KeyShape.OVAL_ROUNDED -> 14.dp
                                }
                                val shapeObj = RoundedCornerShape(cornerRadius)

                                Card(
                                    modifier = Modifier
                                        .width(72.dp)
                                        .height(84.dp)
                                        .border(
                                            width = if (isSelected && viewModel.decorativeIcon == null) 2.5.dp else 1.dp,
                                            color = if (isSelected && viewModel.decorativeIcon == null) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            viewModel.keyShape = shape
                                            if (shape != KeyShape.NONE) {
                                                viewModel.decorativeIcon = null
                                            }
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Transparent
                                    )
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 6.dp, horizontal = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 42.dp, height = 44.dp)
                                                .clip(shapeObj)
                                                .background(if (shape == KeyShape.NONE) Color.Transparent else currentBgColor)
                                                .border(
                                                    width = if (shape == KeyShape.NONE) 1.5.dp else 1.dp,
                                                    color = if (shape == KeyShape.NONE) MaterialTheme.colorScheme.outline.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.7f),
                                                    shape = shapeObj
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (shape == KeyShape.NONE) {
                                                Text(text = "∅", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            } else {
                                                Text(text = "A", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = currentTextColor)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected && viewModel.decorativeIcon == null) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isSelected && viewModel.decorativeIcon == null) EmeraldPrimary else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Section 1: Classic Key background
                        EditorSectionHeader(title = "Classic Key Background")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                            item {
                                val isNoneSelected = viewModel.decorativeIcon.isNullOrEmpty()
                                Card(
                                    modifier = Modifier
                                        .size(width = 72.dp, height = 84.dp)
                                        .border(
                                            width = if (isNoneSelected) 2.5.dp else 1.dp,
                                            color = if (isNoneSelected) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            viewModel.decorativeIcon = null
                                            if (viewModel.keyShape == KeyShape.NONE) {
                                                viewModel.keyShape = KeyShape.SQUARE_ROUNDED
                                            }
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Transparent
                                    )
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(text = "🚫", fontSize = 22.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = "None", style = MaterialTheme.typography.labelSmall, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                            items(classicKeyBackgroundIcons) { (resName, label) ->
                                val isSelected = viewModel.decorativeIcon == resName
                                val resId = remember(resName) {
                                    context.resources.getIdentifier(resName, "drawable", context.packageName)
                                }
                                Card(
                                    modifier = Modifier
                                        .size(width = 72.dp, height = 84.dp)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            viewModel.decorativeIcon = resName
                                            viewModel.keyShape = KeyShape.NONE
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Transparent
                                    )
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            if (resId != 0) {
                                                Image(
                                                    painter = androidx.compose.ui.res.painterResource(id = resId),
                                                    contentDescription = label,
                                                    modifier = Modifier.size(46.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                            } else {
                                                Text(text = resName, fontSize = 26.sp)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Section 2: Popular Key background
                        EditorSectionHeader(title = "Popular Key Background")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                            items(popularKeyBackgroundIcons) { (resName, label) ->
                                val isSelected = viewModel.decorativeIcon == resName
                                val resId = remember(resName) {
                                    context.resources.getIdentifier(resName, "drawable", context.packageName)
                                }
                                Card(
                                    modifier = Modifier
                                        .size(width = 72.dp, height = 84.dp)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            viewModel.decorativeIcon = resName
                                            viewModel.keyShape = KeyShape.NONE
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Transparent
                                    )
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            if (resId != 0) {
                                                Image(
                                                    painter = androidx.compose.ui.res.painterResource(id = resId),
                                                    contentDescription = label,
                                                    modifier = Modifier.size(46.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                            } else {
                                                Text(text = resName, fontSize = 26.sp)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        EditorSectionHeader(title = "Key Opacity (${(viewModel.keyOpacity * 100).roundToInt()}%)")
                        Slider(
                            value = viewModel.keyOpacity,
                            onValueChange = { viewModel.keyOpacity = it },
                            valueRange = 0.0f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // ── STEP 3: FONT & TEXT ─────────────────────────────────
                    3 -> {
                        EditorSectionHeader(title = "Font Color")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                            items(fontColorPresets) { (hex, label) ->
                                val color = parseHexColor(hex)
                                val isSelected = hex.equals(viewModel.keyTextColor, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(width = if (isSelected) 3.dp else 1.dp, color = if (isSelected) EmeraldPrimary else Color.Gray, shape = CircleShape)
                                        .clickable { viewModel.keyTextColor = hex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = "Selected", tint = if (isLightColor(color)) Color.Black else Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, EmeraldPrimary, CircleShape)
                                        .clickable { showCustomColorDialog = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Palette, contentDescription = "Custom Color", tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }



                        Spacer(modifier = Modifier.height(12.dp))

                        EditorSectionHeader(title = "Font Size")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            FilterChip(selected = (viewModel.fontSizeSp == 14), onClick = { viewModel.fontSizeSp = 14 }, label = { Text("Small") }, modifier = Modifier.weight(1f))
                            FilterChip(selected = (viewModel.fontSizeSp == 16), onClick = { viewModel.fontSizeSp = 16 }, label = { Text("Medium") }, modifier = Modifier.weight(1f))
                            FilterChip(selected = (viewModel.fontSizeSp == 18), onClick = { viewModel.fontSizeSp = 18 }, label = { Text("Large") }, modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Text Shadow", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = "Apply subtle drop shadow to keyboard text", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = viewModel.hasTextShadow,
                                onCheckedChange = { viewModel.hasTextShadow = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = EmeraldPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        EditorSectionHeader(title = "Theme Name")
                        OutlinedTextField(
                            value = viewModel.themeName,
                            onValueChange = { viewModel.themeName = it },
                            label = { Text("Theme Name") },
                            placeholder = { Text("e.g. My Custom Theme") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }

    // ── Live Interactive Keyboard Preview Modal Dialog ────────────────────────
    if (viewModel.showPreviewModal) {
        AlertDialog(
            onDismissRequest = { viewModel.showPreviewModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Interactive Canvas",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    KeyboardThemePreviewImage(theme = draftTheme)
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.showPreviewModal = false }) {
                    Text("Close", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                }
            }
        )
    }

    // ── Custom Hex Color Picker Dialog ──────────────────────
    if (showCustomColorDialog) {
        AlertDialog(
            onDismissRequest = { showCustomColorDialog = false },
            title = { Text("Custom Font Color") },
            text = {
                Column {
                    Text("Enter hex color code:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customHexInput,
                        onValueChange = { customHexInput = it },
                        singleLine = true,
                        placeholder = { Text("#10B981") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (customHexInput.isNotBlank()) {
                        val formatted = if (customHexInput.startsWith("#")) customHexInput else "#$customHexInput"
                        viewModel.keyTextColor = formatted
                    }
                    showCustomColorDialog = false
                }) {
                    Text("Select", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomColorDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Stitch Progress Stepper Chip Component.
 */
@Composable
private fun StepChipStitch(
    stepNumber: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isActive) EmeraldPrimary else Color.Transparent)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isActive) Color.White.copy(alpha = 0.25f) else if (isCompleted) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isCompleted && !isActive) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                } else {
                    Text(
                        text = stepNumber.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Stitch Wallpaper Grid Card.
 */
@Composable
private fun StitchWallpaperCard(
    title: String,
    subtitle: String,
    assetPath: String,
    isSelected: Boolean,
    badgeText: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val imageBitmap = remember(assetPath) {
        try {
            if (assetPath.startsWith("themes/") || assetPath.endsWith(".jpg") || assetPath.endsWith(".png")) {
                context.assets.open(assetPath).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                }
            } else {
                val resId = context.resources.getIdentifier(assetPath, "drawable", context.packageName)
                if (resId != 0) {
                    BitmapFactory.decodeResource(context.resources, resId)?.asImageBitmap()
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    Card(
        modifier = modifier
            .height(125.dp)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldPrimary
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RadioDot(isSelected: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant) else null,
        modifier = Modifier.size(22.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun ColorSwatchRow(
    presets: List<String>,
    selectedColorHex: String,
    onColorSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(presets) { hex ->
            val color = parseHexColor(hex)
            val isSelected = hex.equals(selectedColorHex, ignoreCase = true)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color = if (isSelected) EmeraldPrimary else Color.Gray.copy(alpha = 0.5f),
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
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

private fun parseHexColor(hex: String): Color {
    return try {
        val cleaned = hex.removePrefix("#")
        val colorInt = android.graphics.Color.parseColor("#$cleaned")
        Color(colorInt)
    } catch (e: Exception) {
        Color(0xFF10B981)
    }
}

private fun isLightColor(color: Color): Boolean {
    val luminance = (0.299f * color.red) + (0.587f * color.green) + (0.114f * color.blue)
    return luminance > 0.6f
}
