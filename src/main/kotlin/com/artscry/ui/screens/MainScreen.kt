package com.artscry.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.artscry.core.common.AppState
import com.artscry.core.domain.model.PracticeMode
import com.artscry.core.domain.model.TimerConfig
import com.artscry.ui.components.TagChip
import com.artscry.ui.components.TagSelectorDialog
import com.artscry.ui.components.TimerDisplay
import com.artscry.ui.theme.ThemeState
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(state: AppState) {
    var showMenu by remember { mutableStateOf(false) }
    var isBlackoutConfig by remember { mutableStateOf(false) }
    var showTagSelector by remember { mutableStateOf(false) }
    var showTagManager by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (state.viewerSettings == null) {
        SetupScreen(imageRepository = state.repository) { settings ->
            state.viewerSettings = settings

            if (settings.preloadedImages != null && settings.preloadedImages.isNotEmpty()) {
                println("Using ${settings.preloadedImages.size} preloaded images")
                val imagesToUse = if (settings.randomMode && settings.imageLimit != null &&
                    settings.imageLimit < settings.preloadedImages.size) {
                    settings.preloadedImages.shuffled().take(settings.imageLimit)
                } else {
                    settings.preloadedImages
                }

                state.imageManager.images = imagesToUse
                state.imageManager.selectedImages = imagesToUse
                state.imageManager.currentImageIndex = 0
                state.tagManager.loadTagsForImage(state.imageManager.currentImage?.path)
            } else if (settings.tags.isNotEmpty()) {
                state.loadImagesWithTags(
                    folder = settings.folderPath,
                    tags = settings.tags,
                    limit = settings.imageLimit
                )
            } else {
                state.loadImages(
                    folder = settings.folderPath,
                    limit = settings.imageLimit
                )
            }

            if (settings.timerEnabled) {
                state.startTimer(
                    TimerConfig(
                        duration = settings.timerDuration,
                    )
                )
            }

            if (settings.mode == PracticeMode.BLACKOUT_PRACTICE) {
                state.startBlackout(settings.blackoutDuration)
                state.startTimer(
                    TimerConfig(
                        duration = settings.blackoutDuration,
                    )
                )
            }
        }
    } else {

        Surface {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {

                    ImageViewer(
                        currentImage = state.images.getOrNull(state.currentImageIndex),
                        imageCache = state.imageCache,
                        allImages = state.images,
                        onNextImage = state::onNextImage,
                        onPreviousImage = state::onPreviousImage,
                        hideNavigation = state.viewerSettings?.hideArrowsInTimer == true,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isBlackoutConfig) {
                        Surface(
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            if (state.blackoutManager.isBlackedOut) {
                                                state.blackoutManager.isBlackedOut = false
                                                awaitRelease()
                                                state.blackoutManager.isBlackedOut = true
                                            }
                                        }
                                    )
                                }.alpha(if (state.isBlackedOut) 1f else 0f),
                        ) {
                            state.blackoutPracticeTimeRemaining?.let { timeLeft ->
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$timeLeft",
                                            color = if (state.isBlackedOut) Color.White else Color.Black,
                                            fontSize = 72.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "seconds remaining",
                                            color = if (state.isBlackedOut) Color.White else Color.Black,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }

                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 64.dp, end = 16.dp)
                    ) {
                        IconButton(
                            onClick = { showTagManager = true },
                            modifier = Modifier.background(
                                color = MaterialTheme.colors.surface.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(8.dp)
                            )
                                .padding(4.dp)
                        ) {
                            Text("🏷️")
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        IconButton(onClick = { showMenu = true }) {
                            Text("⋮")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(onClick = {
                                state.isAlwaysOnTop = !state.isAlwaysOnTop
                            }) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Always on Top")
                                    if (state.isAlwaysOnTop) {
                                        Text("✓")
                                    }
                                }
                            }

                            DropdownMenuItem(onClick = {
                                ThemeState.isDarkMode = !ThemeState.isDarkMode
                            }) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Dark Mode")
                                    if (ThemeState.isDarkMode) {
                                        Text("✓")
                                    }
                                }
                            }


                            Divider()

                            DropdownMenuItem(onClick = {

                                showMenu = false
                                state.viewerSettings = null
                                state.imageManager.images = emptyList()
                                state.imageCache.clearCache()
                                state.stopTimer()
                            }) {
                                Text("Change Settings")
                            }

                            DropdownMenuItem(onClick = { showMenu = false }) {
                                Text("${state.images.size} images")
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = state.isTimerActive && !state.onBlackout,
                    enter = slideInHorizontally { it } + fadeIn(),
                    exit = slideOutHorizontally { it } + fadeOut(),
                    modifier = Modifier
                        .padding(end = 4.dp, start = 4.dp)
                ) {
                    TimerDisplay(
                        config = state.timerConfig!!,
                        imageChangeCount = state.imageChangeCounter,
                        onTimerComplete = {
                            if (!isBlackoutConfig) {
                                state.onNextImage()
                            } else {
                                state.stopTimer()
                            }
                        }
                    )
                }
            }
        }
    }

    if (showTagManager) {
        Dialog(onDismissRequest = { showTagManager = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                elevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(max = 500.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Manage Tags",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val currentImage = state.images.getOrNull(state.currentImageIndex)

                    if (currentImage != null) {
                        Text(
                            "Image: ${File(currentImage.path).name}",
                            style = MaterialTheme.typography.subtitle1,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            "Current Tags:",
                            style = MaterialTheme.typography.subtitle2,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (state.currentImageTags.isEmpty()) {
                            Text(
                                "No tags applied to this image",
                                style = MaterialTheme.typography.body2,
                                modifier = Modifier.padding(bottom = 16.dp),
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                state.currentImageTags.forEach { tag ->
                                    TagChip(
                                        tag = tag,
                                        selected = true,
                                        onRemove = {
                                            scope.launch {
                                                val imageEntity = state.repository.getImageByPath(currentImage.path)
                                                if (imageEntity != null) {
                                                    state.repository.removeTagFromImage(imageEntity.id, tag.id)
                                                    state.tagManager.loadTagsForImage(currentImage.path)
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { showTagSelector = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Tags")
                        }
                    } else {
                        Text(
                            "No image selected",
                            style = MaterialTheme.typography.body1
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { showTagManager = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    if (showTagSelector) {
        var allTags by remember { mutableStateOf<List<com.artscry.core.domain.model.Tag>>(emptyList()) }

        LaunchedEffect(Unit) {
            state.repository.getAllTags().collect { tags ->
                allTags = tags
            }
        }

        TagSelectorDialog(
            allTags = allTags,
            selectedTags = state.currentImageTags,
            onTagSelected = { tag ->
                val currentImage = state.images.getOrNull(state.currentImageIndex)
                if (currentImage != null) {
                    scope.launch {
                        val imageEntity = state.repository.getImageByPath(currentImage.path)
                            ?: state.repository.createImageEntity(currentImage)

                        state.repository.addTagToImage(imageEntity.id, tag.id)

                        state.tagManager.loadTagsForImage(currentImage.path)
                    }
                }
            },
            onDismiss = { showTagSelector = false }
        )
    }
}