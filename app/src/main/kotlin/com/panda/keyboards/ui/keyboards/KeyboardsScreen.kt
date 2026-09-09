package com.panda.keyboards.ui.keyboards

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panda.keyboards.ime.keyboard.ResolvedTheme
import com.panda.keyboards.theme.KeyboardTheme
import com.panda.keyboards.ui.theme.ProGold

import androidx.compose.material.icons.filled.Delete

private data class PreviewKeyData(
    val weight: Float,
    val bgColor: Color,
    val textColor: Color,
    val label: String
)

private data class PreparedKey(
    val rectTopLeft: Offset,
    val rectSize: Size,
    val color: Color,
    val textLayout: TextLayoutResult?,
    val textTopLeft: Offset
)

/**
 * Keyboards gallery screen displaying sample and custom themes in a 2-column grid.
 *
 * Performance & Memory Optimized: Loads visible theme subset initially, incrementally fetching
 * more items as the user scrolls. Uses single-pass [Canvas] previews via [drawWithCache].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardsScreen(
    viewModel: KeyboardsViewModel,
    onOpenSetupDialog: (String) -> Unit = {},
    onThemeApplied: (String) -> Unit = {},
    onOpenCustomEditor: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val themes by viewModel.themes.collectAsStateWithLifecycle()
    val selectedThemeId by viewModel.selectedThemeId.collectAsStateWithLifecycle()
    val imeStatus by viewModel.imeStatus.collectAsStateWithLifecycle()

    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedThemeForSheet by viewModel.selectedThemeForSheet.collectAsStateWithLifecycle()
    val unlockedThemeIds by viewModel.unlockedThemeIds.collectAsStateWithLifecycle()
    val freeApplyCredits by viewModel.freeApplyCredits.collectAsStateWithLifecycle()

    val effectiveIsFullyConfigured = imeStatus.isFullyConfigured
    val gridState = rememberLazyGridState()

    // Re-evaluate IME status whenever the screen is resumed (e.g. user returns from settings or dialog)
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

    var themeToDelete by remember { mutableStateOf<KeyboardTheme?>(null) }
    var showProPurchaseDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🐼",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Panda Keyboards",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "IME Setup & Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Top CTA Card: Custom Theme ──────────────────────────────────
            item(span = { GridItemSpan(2) }) {
                CustomThemeCtaCard(
                    onClick = onOpenCustomEditor
                )
            }

            // ── Section Title ───────────────────────────────────────────────
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Theme Gallery",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // ── Category Filter Bar ─────────────────────────────────────────
            item(span = { GridItemSpan(2) }) {
                ThemeCategoryFilterBar(
                    categories = listOf("All", "Modern UI", "Abstract", "HD Background", "Nature", "Gradient", "Solid", "Custom"),
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category -> viewModel.selectCategory(category) }
                )
            }

            // ── Theme Grid Items (Loaded incrementally on demand) ────────────
            items(
                items = themes,
                key = { it.id }
            ) { theme ->
                val isSelected = selectedThemeId != null && theme.id == selectedThemeId
                val onClick = remember(theme.id, viewModel) {
                    { viewModel.openThemeSheet(theme) }
                }
                KeyboardThemeItem(
                    theme = theme,
                    isSelected = isSelected,
                    onClick = onClick,
                    onDeleteClick = if (theme.isCustom) {
                        { themeToDelete = theme }
                    } else null
                )
            }
        }
    }

    // ── Theme Detail Modal Bottom Sheet ──────────────────────────────────────
    val sheetTheme = selectedThemeForSheet
    if (sheetTheme != null) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { viewModel.dismissThemeSheet() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Theme Title & Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = sheetTheme.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (sheetTheme.isCustom) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "CUSTOM",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else if (sheetTheme.isPro) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "PRO",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "FREE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Large Preview Image (Static Canvas Pass)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    KeyboardThemePreviewImage(theme = sheetTheme)
                }

                Spacer(modifier = Modifier.height(24.dp))

                val onThemeSelectedAction: () -> Unit = {
                    if (effectiveIsFullyConfigured) {
                        onThemeApplied(sheetTheme.id)
                    } else {
                        onOpenSetupDialog(sheetTheme.id)
                    }
                }

                if (sheetTheme.isCustom) {
                    // Custom Theme: Direct Apply Action
                    androidx.compose.material3.Button(
                        onClick = {
                            viewModel.applyTheme(sheetTheme.id)
                            onThemeSelectedAction()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Apply Custom Theme", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    androidx.compose.material3.OutlinedButton(
                        onClick = { themeToDelete = sheetTheme },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Delete Theme", color = MaterialTheme.colorScheme.error)
                    }
                } else if (freeApplyCredits > 0) {
                    // User has 2-time rewarded ad free Apply passes!
                    androidx.compose.material3.Button(
                        onClick = {
                            viewModel.consumeFreeApplyCreditAndApply(sheetTheme.id, onThemeSelectedAction)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Apply", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                text = "$freeApplyCredits free apply pass${if (freeApplyCredits > 1) "es" else ""} remaining",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                } else {
                    // Option 1: Unlock Forever (PRO) (All Feature No Limit)
                    androidx.compose.material3.Button(
                        onClick = { showProPurchaseDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = ProGold,
                            contentColor = Color.Black
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Unlock Forever (PRO)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            Text(
                                text = "All Feature No Limit",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option 2: Unlock For Free (Watch ad to unlock)
                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            viewModel.watchRewardedAdAndApply(sheetTheme.id, onThemeSelectedAction)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Unlock For Free (Watch ad to unlock)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Delete Custom Theme Confirmation Dialog
    themeToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { themeToDelete = null },
            title = {
                Text(text = "Delete Custom Theme?")
            },
            text = {
                Text(text = "Are you sure you want to delete '${target.name}'? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomTheme(target.id)
                        themeToDelete = null
                    }
                ) {
                    Text(text = "Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { themeToDelete = null }) {
                    Text(text = "Cancel")
                }
            }
        )
    }




    // PRO Purchase Stub Dialog
    if (showProPurchaseDialog) {
        AlertDialog(
            onDismissRequest = { showProPurchaseDialog = false },
            title = {
                Text(text = "Unlock Panda PRO")
            },
            text = {
                Text("In-app purchase billing flow is coming in a future sprint! Use 'Unlock For Free' to test this theme now.")
            },
            confirmButton = {
                TextButton(onClick = { showProPurchaseDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

/**
 * Top CTA Banner encouraging users to create custom themes.
 */
@Composable
private fun CustomThemeCtaCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF7C4DFF),
                            Color(0xFFB388FF)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Custom Theme",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Create Custom Theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Design your own custom color theme",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

/**
 * Single cell in the 2-column theme gallery grid.
 *
 * Renders a high-performance single-pass [KeyboardThemePreviewImage] preview.
 */
@Composable
private fun KeyboardThemeItem(
    theme: KeyboardTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column {
            // ── Static Theme Preview Box ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.33f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color.Black.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                KeyboardThemePreviewImage(theme = theme)

                // Delete Button for Custom Themes
                if (onDeleteClick != null) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .size(28.dp)
                            .clickable(onClick = onDeleteClick)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Theme",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Selected Checkmark Badge or PRO Gold Crown Badge
                if (isSelected) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(24.dp),
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else if (theme.isPro) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 8.dp, topEnd = 16.dp, bottomEnd = 0.dp),
                        color = Color(0xFFF5C518),
                        shadowElevation = 2.dp,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "👑",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // ── Theme Title & Label ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (theme.isCustom) {
                    Text(
                        text = "Custom",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                } else if (isSelected) {
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}


/**
 * High-performance single-pass Canvas theme preview.
 *
 * Draws the background, header strip, and key shapes directly using GPU drawing passes
 * via [drawWithCache]. This replaces over 40 layout nodes with a single [Canvas] node per grid cell,
 * eliminating measurement/layout passes during grid scrolling.
 */
@Composable
internal fun KeyboardThemePreviewImage(
    theme: KeyboardTheme,
    modifier: Modifier = Modifier
) {
    val resolvedTheme = remember(theme) { ResolvedTheme.from(theme) }
    val textMeasurer = rememberTextMeasurer()

    val context = androidx.compose.ui.platform.LocalContext.current
    val bgImageBitmap = remember(resolvedTheme.imagePath) {
        resolvedTheme.imagePath?.let { path ->
            try {
                val file = java.io.File(path)
                if (file.exists() && file.isFile) {
                    BitmapFactory.decodeFile(path)?.asImageBitmap()
                } else {
                    context.assets.open(path).use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    Spacer(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val padding = 6.dp.toPx()
                val drawWidth = size.width - (padding * 2)

                val bgBrush = resolvedTheme.keyboardBackgroundBrush
                val bgColor = resolvedTheme.keyboardBackground
                val keyColor = resolvedTheme.keyBackground
                val specialKeyColor = resolvedTheme.specialKeyBackground
                val accentColor = resolvedTheme.accent
                val keyTextColor = resolvedTheme.keyText

                // ── Pre-calculate all key bounds & measure text layouts ONCE in drawWithCache ──
                val headerTop = padding
                val headerHeight = 14.dp.toPx()

                val rowSpacing = 3.dp.toPx()
                val keySpacing = 2.dp.toPx()
                val rowsTop = headerTop + headerHeight + 4.dp.toPx()
                val availableRowsHeight = size.height - padding - rowsTop

                val targetKeyWidth = (drawWidth - (keySpacing * 9)) / 10f
                val rowHeight = (targetKeyWidth * 1.05f).coerceAtMost((availableRowsHeight - (rowSpacing * 3)) / 4f)
                val totalRowsHeight = (rowHeight * 4) + (rowSpacing * 3)
                val startY = rowsTop + ((availableRowsHeight - totalRowsHeight) / 2f).coerceAtLeast(0f)

                val keyCornerRadiusPx = when (theme.keyShape) {
                    com.panda.keyboards.theme.KeyShape.SQUARE -> 2.dp.toPx()
                    com.panda.keyboards.theme.KeyShape.ROUNDED -> 7.dp.toPx()
                    com.panda.keyboards.theme.KeyShape.PILL -> (rowHeight / 2f)
                }
                val keyCornerRadius = CornerRadius(keyCornerRadiusPx, keyCornerRadiusPx)
                val headerCornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())

                val fontSpNormal = (rowHeight * 0.45f).toSp()
                val fontSpSmall = (rowHeight * 0.32f).toSp()

                fun prepareRow(
                    rowY: Float,
                    keys: List<PreviewKeyData>
                ): List<PreparedKey> {
                    val totalWeight = keys.sumOf { it.weight.toDouble() }.toFloat()
                    val totalSpacing = keySpacing * (keys.size - 1)
                    val totalKeyWidth = drawWidth - totalSpacing

                    var currentX = padding
                    val prepared = ArrayList<PreparedKey>(keys.size)
                    for (key in keys) {
                        val kWidth = (key.weight / totalWeight) * totalKeyWidth
                        val rectTopLeft = Offset(currentX, rowY)
                        val rectSize = Size(kWidth, rowHeight)

                        var textLayout: TextLayoutResult? = null
                        var textTopLeft = Offset.Zero

                        if (key.label.isNotEmpty()) {
                            val fontSp = if (key.label.length > 2) fontSpSmall else fontSpNormal
                            val textStyle = TextStyle(
                                color = key.textColor,
                                fontSize = fontSp,
                                fontWeight = FontWeight.Normal
                            )
                            val measured = textMeasurer.measure(
                                text = key.label,
                                style = textStyle
                            )
                            val textX = currentX + (kWidth - measured.size.width) / 2f
                            val textY = rowY + (rowHeight - measured.size.height) / 2f
                            textLayout = measured
                            textTopLeft = Offset(textX, textY)
                        }

                        prepared.add(
                            PreparedKey(
                                rectTopLeft = rectTopLeft,
                                rectSize = rectSize,
                                color = key.bgColor,
                                textLayout = textLayout,
                                textTopLeft = textTopLeft
                            )
                        )

                        currentX += kWidth + keySpacing
                    }
                    return prepared
                }

                // Row 1
                val r1Labels = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
                val r1Keys = r1Labels.map { PreviewKeyData(1f, keyColor, keyTextColor, it) }
                val preparedR1 = prepareRow(startY, r1Keys)

                // Row 2
                val r2Labels = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")
                val r2Keys = r2Labels.map { PreviewKeyData(1f, keyColor, keyTextColor, it) }
                val preparedR2 = prepareRow(startY + rowHeight + rowSpacing, r2Keys)

                // Row 3
                val r3MiddleLabels = listOf("Z", "X", "C", "V", "B", "N", "M")
                val r3Keys = buildList {
                    add(PreviewKeyData(1.5f, specialKeyColor, keyTextColor, "⇧"))
                    r3MiddleLabels.forEach { add(PreviewKeyData(1f, keyColor, keyTextColor, it)) }
                    add(PreviewKeyData(1.5f, specialKeyColor, keyTextColor, "⌫"))
                }
                val preparedR3 = prepareRow(startY + (rowHeight + rowSpacing) * 2, r3Keys)

                // Row 4 (Comma before Space)
                val r4Keys = listOf(
                    PreviewKeyData(1.5f, specialKeyColor, keyTextColor, "?123"),
                    PreviewKeyData(1.0f, keyColor, keyTextColor, ","),
                    PreviewKeyData(4.0f, keyColor, keyTextColor.copy(alpha = 0.6f), "space"),
                    PreviewKeyData(1.0f, keyColor, keyTextColor, "."),
                    PreviewKeyData(1.5f, accentColor, Color.White, "↵")
                )
                val preparedR4 = prepareRow(startY + (rowHeight + rowSpacing) * 3, r4Keys)

                val allPreparedKeys = preparedR1 + preparedR2 + preparedR3 + preparedR4

                onDrawBehind {
                    // 1. Draw keyboard background
                    if (bgImageBitmap != null) {
                        drawImage(
                            image = bgImageBitmap,
                            dstSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt())
                        )
                    } else if (bgBrush != null) {
                        drawRect(brush = bgBrush)
                    } else {
                        drawRect(color = bgColor)
                    }

                    // 1b. Draw glowing underlay for Glassmorphic themes
                    val glassGlowBrush = resolvedTheme.glassmorphicGlowBrush
                    if (glassGlowBrush != null) {
                        drawRect(
                            brush = glassGlowBrush,
                            topLeft = Offset(padding, startY - rowSpacing),
                            size = Size(drawWidth, totalRowsHeight + (rowSpacing * 2))
                        )
                    }

                    // 2. Header Strip
                    drawRoundRect(
                        color = specialKeyColor.copy(alpha = 0.5f),
                        topLeft = Offset(padding, headerTop),
                        size = Size(drawWidth, headerHeight),
                        cornerRadius = headerCornerRadius
                    )
                    drawRoundRect(
                        color = accentColor,
                        topLeft = Offset(padding + 6.dp.toPx(), headerTop + 3.dp.toPx()),
                        size = Size(28.dp.toPx(), 8.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                    drawRoundRect(
                        color = keyTextColor.copy(alpha = 0.3f),
                        topLeft = Offset(padding + 38.dp.toPx(), headerTop + 3.dp.toPx()),
                        size = Size(20.dp.toPx(), 8.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )

                    // 3. Draw pre-calculated keys & text layouts (Zero allocations per draw frame!)
                    val shadowColor = resolvedTheme.keyShadowColor
                    val rawShadowOffsetPx = resolvedTheme.keyShadowOffsetDp.dp.toPx()
                    // Intentional Thumbnail Legibility Enhancement: Ensure shadow offset is at least 1.8dp in small scale previews
                    val previewShadowOffsetPx = if (rawShadowOffsetPx > 0f) rawShadowOffsetPx.coerceAtLeast(1.8.dp.toPx()) else 0f
                    val previewBorderWidthPx = resolvedTheme.keyBorderWidthDp.dp.toPx()

                    for (key in allPreparedKeys) {
                        if (previewShadowOffsetPx > 0f && shadowColor != null) {
                            drawRoundRect(
                                color = shadowColor,
                                topLeft = key.rectTopLeft + Offset(0f, previewShadowOffsetPx),
                                size = key.rectSize,
                                cornerRadius = keyCornerRadius
                            )
                        }
                        drawRoundRect(
                            color = key.color,
                            topLeft = key.rectTopLeft,
                            size = key.rectSize,
                            cornerRadius = keyCornerRadius
                        )
                        if (previewBorderWidthPx > 0f && resolvedTheme.keyBorderColor != null) {
                            drawRoundRect(
                                color = resolvedTheme.keyBorderColor!!,
                                topLeft = key.rectTopLeft,
                                size = key.rectSize,
                                cornerRadius = keyCornerRadius,
                                style = Stroke(width = previewBorderWidthPx)
                            )
                        }
                        if (key.textLayout != null) {
                            drawText(
                                textLayoutResult = key.textLayout,
                                topLeft = key.textTopLeft
                            )
                        }
                    }
                }
            }
    )
}

/**
 * Category filter chips strip for filtering themes in the Theme Gallery.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeCategoryFilterBar(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(categories) { cat ->
            val isSelected = cat.equals(selectedCategory, ignoreCase = true)

            androidx.compose.material3.FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(cat) },
                label = {
                    Text(
                        text = cat,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                },
                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
