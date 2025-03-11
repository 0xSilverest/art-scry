package com.artscry.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Codec
import java.io.File


@Composable
fun FilePreview(file: File, isSelected: Boolean) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(file) {
        withContext(Dispatchers.IO) {
            try {
                val imageSource = org.jetbrains.skia.Data.makeFromBytes(file.readBytes())
                val codec = Codec.makeFromData(imageSource)

                val originalWidth = codec.width
                val originalHeight = codec.height

                val targetSize = 300
                val scaleFactor = minOf(
                    targetSize.toFloat() / originalWidth,
                    targetSize.toFloat() / originalHeight
                )

                val scaledWidth = (originalWidth * scaleFactor).toInt()
                val scaledHeight = (originalHeight * scaleFactor).toInt()

                val skiaImage = org.jetbrains.skia.Image.makeFromEncoded(imageSource.bytes)

                val surface = org.jetbrains.skia.Surface.makeRasterN32Premul(scaledWidth, scaledHeight)
                val canvas = surface.canvas

                canvas.drawImageRect(
                    skiaImage,
                    org.jetbrains.skia.Rect.makeXYWH(0f, 0f, originalWidth.toFloat(), originalHeight.toFloat()),
                    org.jetbrains.skia.Rect.makeXYWH(0f, 0f, scaledWidth.toFloat(), scaledHeight.toFloat()),
                    org.jetbrains.skia.Paint()
                )

                imageBitmap = surface.makeImageSnapshot().toComposeImageBitmap()

                codec.close()

            } catch (e: Exception) {
                println("Failed to load image thumbnail: ${e.message}")
            }
        }
    }

    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f)),
        color = if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f) else MaterialTheme.colors.surface
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap!!,
                    contentDescription = file.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
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
