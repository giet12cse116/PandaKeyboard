package com.panda.keyboards.ui.themeeditor

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

/** Aspect ratio matching keyboard layout and gallery cards (width:height = 1.5:1). */
const val CROP_ASPECT_RATIO = 1.5f

/**
 * Interactive Compose Crop UI for custom image themes.
 * Allows pinch-to-zoom and pan within a fixed 1.5:1 keyboard aspect ratio crop window.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCropScreen(
    sourceBitmap: Bitmap,
    onCancel: () -> Unit,
    onConfirmCrop: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val imageBitmap = remember(sourceBitmap) { sourceBitmap.asImageBitmap() }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var minScale by remember { mutableFloatStateOf(1.0f) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Crop Image",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancel"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.Black,
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(text = "Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            if (containerSize.width > 0 && containerSize.height > 0) {
                                // Compute crop box on screen
                                val frameMarginHorizontal = 24f * containerSize.width / 360f
                                val frameWidth = (containerSize.width - (frameMarginHorizontal * 2)).coerceAtLeast(100f)
                                val frameHeight = frameWidth / CROP_ASPECT_RATIO

                                val frameLeft = (containerSize.width - frameWidth) / 2f
                                val frameTop = (containerSize.height - frameHeight) / 2f
                                val frameRight = frameLeft + frameWidth
                                val frameBottom = frameTop + frameHeight

                                val imgCenterX = containerSize.width / 2f + offsetX
                                val imgCenterY = containerSize.height / 2f + offsetY

                                val cropLeftInBitmap = (frameLeft - imgCenterX) / scale + sourceBitmap.width / 2f
                                val cropTopInBitmap = (frameTop - imgCenterY) / scale + sourceBitmap.height / 2f
                                val cropRightInBitmap = (frameRight - imgCenterX) / scale + sourceBitmap.width / 2f
                                val cropBottomInBitmap = (frameBottom - imgCenterY) / scale + sourceBitmap.height / 2f

                                val cropRect = RectF(
                                    cropLeftInBitmap,
                                    cropTopInBitmap,
                                    cropRightInBitmap,
                                    cropBottomInBitmap
                                )

                                val cropped = ImageUtils.cropAndDownscaleBitmap(
                                    source = sourceBitmap,
                                    cropRect = cropRect,
                                    targetMaxWidth = 1080,
                                    targetMaxHeight = 720
                                )
                                onConfirmCrop(cropped)
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.padding(end = 4.dp), tint = Color.White)
                        Text(text = "Crop Image", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
                .onGloballyPositioned { coordinates ->
                    if (containerSize != coordinates.size) {
                        containerSize = coordinates.size

                        val frameMarginHorizontal = 24f * containerSize.width / 360f
                        val frameWidth = (containerSize.width - (frameMarginHorizontal * 2)).coerceAtLeast(100f)
                        val frameHeight = frameWidth / CROP_ASPECT_RATIO

                        val scaleW = frameWidth / sourceBitmap.width.toFloat()
                        val scaleH = frameHeight / sourceBitmap.height.toFloat()
                        minScale = max(scaleW, scaleH)
                        scale = minScale
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(minScale, minScale * 5.0f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height
                if (canvasW <= 0 || canvasH <= 0) return@Canvas

                val frameMarginHorizontal = 24.dp.toPx()
                val frameWidth = (canvasW - (frameMarginHorizontal * 2)).coerceAtLeast(100f)
                val frameHeight = frameWidth / CROP_ASPECT_RATIO

                val frameLeft = (canvasW - frameWidth) / 2f
                val frameTop = (canvasH - frameHeight) / 2f
                val cropFrame = Rect(frameLeft, frameTop, frameLeft + frameWidth, frameTop + frameHeight)

                // 1. Draw transformed source bitmap centered
                val drawW = sourceBitmap.width * scale
                val drawH = sourceBitmap.height * scale
                val drawLeft = (canvasW - drawW) / 2f + offsetX
                val drawTop = (canvasH - drawH) / 2f + offsetY

                drawImage(
                    image = imageBitmap,
                    dstOffset = androidx.compose.ui.unit.IntOffset(drawLeft.toInt(), drawTop.toInt()),
                    dstSize = IntSize(drawW.toInt(), drawH.toInt())
                )

                // 2. Translucent overlay outside crop frame
                val overlayPath = Path().apply {
                    addRect(Rect(0f, 0f, canvasW, canvasH))
                }
                val cropFramePath = Path().apply {
                    addRoundRect(androidx.compose.ui.geometry.RoundRect(cropFrame, CornerRadius(12.dp.toPx())))
                }

                clipPath(cropFramePath, clipOp = ClipOp.Difference) {
                    drawPath(overlayPath, color = Color.Black.copy(alpha = 0.65f))
                }

                // 3. Draw crop frame border
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(frameLeft, frameTop),
                    size = Size(frameWidth, frameHeight),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
            }

            Text(
                text = "Pinch to zoom • Drag to position photo",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
        }
    }
}
