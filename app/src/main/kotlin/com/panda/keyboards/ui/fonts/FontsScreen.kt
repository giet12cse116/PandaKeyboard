package com.panda.keyboards.ui.fonts

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panda.keyboards.fonts.FontStyle
import com.panda.keyboards.fonts.FontTransformer
import com.panda.keyboards.ui.theme.HeartRed
import com.panda.keyboards.ui.theme.ProGold

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

/**
 * Fonts screen — the main gallery view for browsing and previewing
 * Unicode font styles.
 *
 * This composable is stateless: all data comes from [FontsUiState]
 * via the [FontsViewModel], and all user actions delegate back to
 * the ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontsScreen(
    viewModel: FontsViewModel,
    autoFocusInput: Boolean = false,
    onAutoFocusConsumed: () -> Unit = {},
    onOpenSetupDialog: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val imeStatus by viewModel.imeStatus.collectAsStateWithLifecycle()
    val isFullyConfigured = imeStatus.isFullyConfigured

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()

    // Show copied feedback
    LaunchedEffect(uiState.showCopiedFeedback) {
        if (uiState.showCopiedFeedback) {
            snackbarHostState.showSnackbar(
                message = "✨ Copied to clipboard!",
                duration = SnackbarDuration.Short
            )
            viewModel.onCopiedFeedbackDismissed()
        }
    }

    val displayedStyles = remember(uiState.selectedTab, uiState.allStyles, uiState.favoriteIds) {
        when (uiState.selectedTab) {
            FontTab.ALL -> uiState.allStyles
            FontTab.FAVORITES -> uiState.allStyles.filter { it.id in uiState.favoriteIds }
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Fonts",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .imePadding()
                .drawVerticalScrollbar(lazyListState, Color(0xFF383838))
        ) {
            // ── Item 0: Preview Card ────────────────────────────────────────
            item(key = "preview_card") {
                PreviewCard(
                    inputText = uiState.inputText,
                    selectedStyle = uiState.selectedStyle,
                    autoFocusInput = autoFocusInput,
                    isFullyConfigured = isFullyConfigured,
                    onAutoFocusConsumed = onAutoFocusConsumed,
                    onOpenSetupDialog = onOpenSetupDialog,
                    onInputChanged = viewModel::onInputChanged,
                    onCopy = {
                        val transformedText = FontTransformer.transform(
                            uiState.inputText, uiState.selectedStyle
                        )
                        copyToClipboard(context, transformedText)
                        viewModel.onCopied()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // ── Item 1: Tab Row ─────────────────────────────────────────────
            item(key = "tab_row") {
                val tabs = FontTab.entries.toList()
                TabRow(
                    selectedTabIndex = tabs.indexOf(uiState.selectedTab),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(
                                tabPositions[tabs.indexOf(uiState.selectedTab)]
                            ),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    tabs.forEach { tab ->
                        Tab(
                            selected = uiState.selectedTab == tab,
                            onClick = { viewModel.onTabSelected(tab) },
                            text = {
                                Text(
                                    text = tab.label,
                                    fontWeight = if (uiState.selectedTab == tab)
                                        FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Items: Font Style List or Empty State ───────────────────────
            if (displayedStyles.isEmpty() && uiState.selectedTab == FontTab.FAVORITES) {
                item(key = "empty_favorites") {
                    EmptyFavoritesState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    )
                }
            } else {
                items(
                    items = displayedStyles,
                    key = { it.id }
                ) { style ->
                    FontStyleRow(
                        style = style,
                        sampleText = uiState.inputText,
                        isFavorite = style.id in uiState.favoriteIds,
                        isSelected = style == uiState.selectedStyle,
                        onSelect = { viewModel.onStyleSelected(style) },
                        onToggleFavorite = { viewModel.onToggleFavorite(style.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ── Preview Card ────────────────────────────────────────────────────────

@Composable
private fun PreviewCard(
    inputText: String,
    selectedStyle: FontStyle,
    autoFocusInput: Boolean = false,
    isFullyConfigured: Boolean = false,
    onAutoFocusConsumed: () -> Unit = {},
    onOpenSetupDialog: () -> Unit = {},
    onInputChanged: (String) -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transformedText = remember(inputText, selectedStyle) {
        FontTransformer.transform(inputText, selectedStyle)
    }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(autoFocusInput, isFullyConfigured) {
        if (autoFocusInput) {
            if (!isFullyConfigured) {
                focusManager.clearFocus()
                keyboardController?.hide()
                onOpenSetupDialog()
            } else {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
            onAutoFocusConsumed()
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1717)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Input field with strict Panda IME permission checking
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(isFullyConfigured) {
                        detectTapGestures {
                            if (!isFullyConfigured) {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                onOpenSetupDialog()
                            } else {
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            }
                        }
                    }
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChanged,
                    readOnly = !isFullyConfigured,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused && !isFullyConfigured) {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                onOpenSetupDialog()
                            }
                        },
                    label = { Text("Type to preview") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Transformed preview with copy button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedStyle.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = transformedText.ifEmpty { " " },
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (inputText.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy to clipboard",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Font Style Row ──────────────────────────────────────────────────────

@Composable
fun FontStyleRow(
    style: FontStyle,
    sampleText: String,
    isFavorite: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transformedText = remember(sampleText, style) {
        FontTransformer.transform(sampleText, style)
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else
            Color(0xFF1A1717),
        label = "rowBackground"
    )

    val heartScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.0f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "heartScale"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSelect
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Style label and preview
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = style.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = transformedText.ifEmpty { " " },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 15.sp
                )
            }

            // Favorite heart toggle
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.scale(heartScale)
            ) {
                AnimatedVisibility(
                    visible = isFavorite,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Remove from favorites",
                        tint = HeartRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
                AnimatedVisibility(
                    visible = !isFavorite,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "Add to favorites",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// ── Empty State ─────────────────────────────────────────────────────────

@Composable
private fun EmptyFavoritesState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🐼",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No favorites yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the ♡ on any font style to\nadd it to your favorites",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ── Utility ─────────────────────────────────────────────────────────────

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Panda Keyboards", text)
    clipboard.setPrimaryClip(clip)
}

private fun Modifier.drawVerticalScrollbar(
    state: LazyListState,
    color: Color
): Modifier {
    return this.drawWithContent {
        drawContent()
        val layoutInfo = state.layoutInfo
        val totalItems = layoutInfo.totalItemsCount
        val visibleItems = layoutInfo.visibleItemsInfo
        if (totalItems > 0 && visibleItems.isNotEmpty()) {
            val firstVisibleIndex = state.firstVisibleItemIndex
            val visibleCount = visibleItems.size
            val barHeight = (size.height * (visibleCount.toFloat() / totalItems.toFloat())).coerceIn(32.dp.toPx(), size.height)
            val maxScrollIndex = (totalItems - visibleCount).coerceAtLeast(1)
            val scrollRatio = (firstVisibleIndex.toFloat() / maxScrollIndex.toFloat()).coerceIn(0f, 1f)
            val barOffsetY = (size.height - barHeight) * scrollRatio
            drawRoundRect(
                color = color.copy(alpha = 0.9f),
                topLeft = Offset(size.width - 6.dp.toPx(), barOffsetY),
                size = Size(4.dp.toPx(), barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
            )
        }
    }
}
