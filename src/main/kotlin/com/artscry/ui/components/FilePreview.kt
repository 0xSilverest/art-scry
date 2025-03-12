package com.artscry.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Size
import kotlinx.coroutines.Dispatchers
import java.io.File


@Composable
fun FilePreview(file: File, isSelected: Boolean, isVisible: Boolean = true) {
    if (!isVisible) {
        Surface(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f)),
            color = if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else MaterialTheme.colors.surface
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 100f
                            )
                        )
                )
                Text(
                    text = file.name,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(4.dp)
                )
            }
        }
        return
    }

    val context = LocalPlatformContext.current
    val imageLoader = AppImageLoader.getInstance(context)

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(file)
            .size(Size(200, 200))
            .memoryCacheKey(file.absolutePath + "_thumb")
            .precision(Precision.INEXACT)
            .build(),
        filterQuality = FilterQuality.Low,
        imageLoader = imageLoader
    )

    val isLoading = painter.state.value is coil3.compose.AsyncImagePainter.State.Loading

    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f)),
        color = if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else MaterialTheme.colors.surface
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painter,
                contentDescription = file.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 100f
                        )
                    )
            )

            Text(
                text = file.name,
                color = Color.White,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(4.dp)
            )
        }
    }
}

object AppImageLoader {
    private var instance: ImageLoader? = null

    private val USER_HOME = System.getProperty("user.home")
    private val CACHE_DIR = File("$USER_HOME/.artscry/image-cache").apply {
        if (!exists()) mkdirs()
    }

    fun clearMemoryCache() {
        instance?.memoryCache?.clear()
    }

    fun clearAllCaches() {
        instance?.let { imageLoader ->
            imageLoader.memoryCache?.clear()
            imageLoader.diskCache?.clear()
        }
    }

    fun evictImage(path: String) {
        instance?.let { imageLoader ->
            imageLoader.memoryCache?.remove(MemoryCache.Key(path))
            imageLoader.diskCache?.remove(path)
        }
    }

    fun getInstance(context: PlatformContext): ImageLoader {
        if (instance == null) {
            val imageProcessingDispatcher = Dispatchers.IO.limitedParallelism(4)

            instance = ImageLoader.Builder(context)
                .memoryCache {
                    MemoryCache.Builder()
                        .maxSizePercent(context, 0.10)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .maxSizePercent(0.02)
                        .build()
                }
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .fetcherCoroutineContext(imageProcessingDispatcher)
                .decoderCoroutineContext(imageProcessingDispatcher)
                .build()
        }
        return instance!!
    }
}