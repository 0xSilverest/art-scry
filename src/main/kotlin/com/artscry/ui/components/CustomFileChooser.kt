package com.artscry.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Codec
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

@Composable
fun CustomFileChooser(
    initialDirectory: String? = null,
    onFolderSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentPath by remember {
        mutableStateOf(initialDirectory?.let { Paths.get(it) } ?: Paths.get(System.getProperty("user.home")))
    }
    var selectedFolder by remember { mutableStateOf<Path?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val searchFocusRequester = remember { FocusRequester() }  // Add search focus requester

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(showSearchBar) {
        if (showSearchBar) {
            delay(100) // Small delay to ensure TextField is composed
            try {
                searchFocusRequester.requestFocus()
            } catch (e: Exception) {
                // Handle potential focus request failure
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(1200.dp)
                .height(800.dp)
                .padding(16.dp)
                .focusRequester(focusRequester)
                .focusable(true)
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.isCtrlPressed && event.key == Key.F) {
                        showSearchBar = !showSearchBar
                        if (!showSearchBar) searchQuery = ""
                        true
                    } else if (event.type == KeyEventType.KeyDown && event.key == Key.Escape) {
                        if (showSearchBar) {
                            showSearchBar = false
                            searchQuery = ""
                            true
                        } else {
                            onDismiss()
                            true
                        }
                    } else {
                        false
                    }
                },
            shape = MaterialTheme.shapes.medium,
            elevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            currentPath.parent?.let {
                                currentPath = it
                                searchQuery = ""
                                showSearchBar = false
                            }
                        },
                        enabled = currentPath.parent != null
                    ) {
                        Text("←")
                    }

                    Text(
                        text = currentPath.toString(),
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                        maxLines = 1
                    )

                    IconButton(onClick = {
                        showSearchBar = !showSearchBar
                        if (!showSearchBar) searchQuery = ""
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }

                if (showSearchBar) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search folders... (Ctrl+F to toggle)") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                                .focusRequester(searchFocusRequester),
                            singleLine = true
                        )

                        IconButton(onClick = {
                            showSearchBar = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Search")
                        }
                    }
                }

                val entries = remember(currentPath, searchQuery) {
                    currentPath.toFile().listFiles()
                        ?.filter { file ->
                            (file.isDirectory || file.extension.lowercase() in setOf(
                                "jpg",
                                "jpeg",
                                "png",
                                "gif",
                                "bmp"
                            )) &&
                                    file.name.contains(searchQuery, ignoreCase = true)
                        }
                        ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                        ?: emptyList()
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(entries.size) { index ->
                        val entry = entries[index]
                        if (entry.isDirectory) {
                            FolderItem(
                                folder = entry,
                                isSelected = selectedFolder?.toString() == entry.absolutePath,
                                onClick = { selectedFolder = entry.toPath() },
                                onDoubleClick = {
                                    currentPath = entry.toPath()
                                    selectedFolder = null
                                    showSearchBar = false
                                }
                            )
                        } else {
                            FilePreview(file = entry)
                        }
                    }
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val pathToUse = selectedFolder ?: currentPath
                            onFolderSelected(pathToUse.toString())
                            onDismiss()
                        },
                        enabled = true
                    ) {
                        Text("Select Folder")
                    }
                }
            }
        }
    }
}

@Composable
private fun FilePreview(file: File) {
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
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
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

@Composable
private fun FolderItem(
    folder: File,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit
) {
    var lastClickTime by remember { mutableStateOf(0L) }

    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .clickable {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < 500) {
                    onDoubleClick()
                } else {
                    onClick()
                }
                lastClickTime = currentTime
            },
        color = if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f)
        else MaterialTheme.colors.surface,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                "📁",
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = folder.name,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )
        }
    }
}