package com.artscry.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.artscry.model.AppState
import com.artscry.model.PracticeMode
import com.artscry.ui.components.ImageViewer
import com.artscry.ui.components.SetupScreen
import com.artscry.ui.components.TimerConfig
import com.artscry.ui.components.TimerDisplay
import com.artscry.ui.theme.ThemeState

@Composable
fun MainScreen(state: AppState) {
    var showMenu by remember { mutableStateOf(false) }
    var isBlackoutConfig by remember { mutableStateOf(false) }

    if (state.viewerSettings == null) {
        SetupScreen { settings ->
            state.viewerSettings = settings

            state.loadImages(
                folder = settings.folderPath,
                limit = settings.imageLimit
            )
            
            if (settings.timerEnabled) {
                state.startTimer(
                    TimerConfig(
                        duration = settings.timerDuration,
                    )
                )
            }

            if (settings.mode == PracticeMode.BLACKOUT_PRACTICE) {
                state.startBlackout(settings.blackoutDuration)
                isBlackoutConfig = true
            } else {
                isBlackoutConfig = false
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
                                            state.isBlackedOut = false
                                            awaitRelease()
                                            state.isBlackedOut = true
                                        }
                                    )
                                }.alpha(if (state.isBlackedOut) 1f else 0f),
                        ) {
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
                                state.images = emptyList()
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
                    visible = state.isTimerActive,
                    enter = slideInHorizontally { it } + fadeIn(),
                    exit = slideOutHorizontally { it } + fadeOut(),
                    modifier = Modifier
                        .padding(end = 4.dp, start = 4.dp)
                ) {
                    TimerDisplay(
                        config = state.timerConfig!!,
                        imageChangeCount = state.imageChangeCounter,
                        onTimerComplete = {
                            state.onNextImage()
                        }
                    )
                }
            }
        }
    }
}