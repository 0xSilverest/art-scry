package com.artscry.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.artscry.core.domain.model.ImageReference
import com.artscry.ui.components.ProgressNumber
import com.artscry.util.ImageCache
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ImageViewer(
    currentImage: ImageReference?,
    imageCache: ImageCache,
    allImages: List<ImageReference>,
    onNextImage: () -> Unit,
    onPreviousImage: () -> Unit,
    hideNavigation: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var loadingError by remember { mutableStateOf<String?>(null) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var isTransitioning by remember { mutableStateOf(false) }

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var contentScale by remember { mutableStateOf(ContentScale.Fit) }
    var isHovered by remember { mutableStateOf(false) }

    var showLoading by remember { mutableStateOf(false) }
    var currentLoadingJob by remember { mutableStateOf<Job?>(null) }
    var retryTrigger by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        onDispose {
            currentLoadingJob?.cancel()
        }
    }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(150)
            showLoading = true
        } else {
            showLoading = false
        }
    }

    LaunchedEffect(currentImage) {
        if (currentImage == null) return@LaunchedEffect

        currentLoadingJob?.cancel()

        isLoading = true
        loadingError = null
        isTransitioning = true

        currentLoadingJob = scope.launch {
            try {
                supervisorScope {
                    val newImage = imageCache.getImage(currentImage.path)
                    if (newImage != null) {
                        scale = 1f
                        offset = Offset.Zero
                        contentScale = ContentScale.Fit
                        imageBitmap = newImage
                    } else {
                        loadingError = "Failed to load image"
                    }
                }
            } catch (e: CancellationException) {
                // Ignore cancellation exceptions
            } catch (e: Exception) {
                loadingError = "Error: ${e.message}"
            } finally {
                isLoading = false
                isTransitioning = false
            }
        }

        scope.launch {
            try {
                imageCache.preloadAround(
                    currentPath = currentImage.path,
                    paths = allImages.map { it.path }
                )
            } catch (e: CancellationException) {
                // Ignore cancellation
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val position = event.changes.first().position
                        isHovered = position.y > size.height - 150
                    }
                }
            }
    ) {
        when {
            showLoading || isTransitioning -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    imageBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(0.5f),
                            contentScale = ContentScale.Fit
                        )
                    }

                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp)
                    )
                }
            }
            loadingError != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = loadingError!!,
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.body1
                        )
                        Button(onClick = {
                            loadingError = null
                            currentLoadingJob?.cancel()
                            currentLoadingJob = null
                            isLoading = true
                            retryTrigger++
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }
            imageBitmap != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { imageSize = it }
                ) {
                    Image(
                        bitmap = imageBitmap!!,
                        contentDescription = "Reference image",
                        contentScale = contentScale,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(0.5f, 5f)

                                    val panSensitivity = scale * 0.7f
                                    val adjustedPan = Offset(
                                        x = pan.x * panSensitivity,
                                        y = pan.y * panSensitivity
                                    )

                                    val scaledWidth = imageSize.width * scale
                                    val scaledHeight = imageSize.height * scale

                                    val newX = if (scaledWidth <= size.width) {
                                        0f
                                    } else {
                                        val maxX = (scaledWidth - size.width) / 2
                                        (offset.x + adjustedPan.x).coerceIn(-maxX, maxX)
                                    }

                                    val newY = if (scaledHeight <= size.height) {
                                        0f
                                    } else {
                                        val maxY = (scaledHeight - size.height) / 2
                                        (offset.y + adjustedPan.y).coerceIn(-maxY, maxY)
                                    }

                                    offset = Offset(newX, newY)
                                }
                            }
                            .onPointerEvent(PointerEventType.Scroll) { event ->
                                val delta = event.changes.first().scrollDelta.y
                                val zoomFactor = if (delta < 0) 1.1f else 0.9f
                                val newScale = (scale * zoomFactor).coerceIn(0.5f, 5f)
                                val pointerOffset = event.changes.first().position

                                val centerX = pointerOffset.x - (size.width / 2)
                                val centerY = pointerOffset.y - (size.height / 2)

                                if (newScale != scale) {
                                    val scaleFactor = newScale / scale

                                    val scaledWidth = imageSize.width * newScale
                                    val scaledHeight = imageSize.height * newScale

                                    val newOffset = if (scaledWidth <= size.width && scaledHeight <= size.height) {
                                        Offset.Zero
                                    } else {
                                        val rawX = centerX - (centerX - offset.x) * scaleFactor
                                        val rawY = centerY - (centerY - offset.y) * scaleFactor

                                        Offset(
                                            x = if (scaledWidth <= size.width) 0f
                                            else rawX.coerceIn(
                                                -(scaledWidth - size.width) / 2,
                                                (scaledWidth - size.width) / 2
                                            ),
                                            y = if (scaledHeight <= size.height) 0f
                                            else rawY.coerceIn(
                                                -(scaledHeight - size.height) / 2,
                                                (scaledHeight - size.height) / 2
                                            )
                                        )
                                    }

                                    offset = newOffset
                                    scale = newScale
                                }
                            }
                    )


                    androidx.compose.animation.AnimatedVisibility(
                        visible = isHovered,
                        enter = androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.75f),
                            modifier = Modifier.wrapContentSize()
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.75f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        scale = (scale - 0.5f).coerceIn(0.5f, 5f)
                                        if (scale == 1f) offset = Offset.Zero
                                    }
                                ) {
                                    Image(
                                        painter = painterResource("icons/zoomOut.png"),
                                        contentDescription = "Zoom out",
                                        modifier = Modifier.size(24.dp),
                                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        scale = (scale + 0.5f).coerceIn(0.5f, 5f)
                                    }
                                ) {
                                    Image(
                                        painter = painterResource("icons/zoomIn.png"),
                                        contentDescription = "Zoom in",
                                        modifier = Modifier.size(24.dp),
                                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                                    )
                                }
                            }
                        }
                    }


                    ProgressNumber(
                        current = allImages.indexOf(currentImage) + 1,
                        total = allImages.size,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 16.dp, start = 16.dp)
                    )


                    if (!hideNavigation) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = onPreviousImage,
                                enabled = !isTransitioning
                            ) {
                                Text("←")
                            }
                            IconButton(
                                onClick = onNextImage,
                                enabled = !isTransitioning
                            ) {
                                Text("→")
                            }
                        }
                    }
                }
            }
        }
    }
}