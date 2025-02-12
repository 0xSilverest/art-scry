package com.artscry.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artscry.model.*
import java.util.*

@Composable
fun SetupScreen(onStart: (ViewerSettings) -> Unit) {
    var selectedMode by remember { mutableStateOf<PracticeMode?>(null) }
    var folderPath by remember { mutableStateOf<String?>(null) }
    var showModeMenu by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    var timedConfig by remember { mutableStateOf(TimedSketchingConfig()) }
    var blackoutConfig by remember { mutableStateOf(BlackoutConfig()) }
    var freeViewConfig by remember { mutableStateOf(FreeViewConfig()) }

    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "ArtScry Setup",
                style = MaterialTheme.typography.h4
            )

            FolderSelector { path ->
                folderPath = path
            }

            Box {
                Button(
                    onClick = { showModeMenu = true },
                    modifier = Modifier
                        .width(250.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.surface,
                        contentColor = MaterialTheme.colors.onSurface
                    ),
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            selectedMode?.name?.replace('_', ' ') ?: "Select Practice Mode",
                            style = MaterialTheme.typography.button
                        )

                        Text(
                            "▼",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showModeMenu,
                    onDismissRequest = { showModeMenu = false },
                    modifier = Modifier.width(250.dp)
                ) {
                    PracticeMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            onClick = {
                                selectedMode = mode
                                showModeMenu = false
                            }
                        ) {
                            Text(
                                mode.name.lowercase(Locale.ROOT).replace('_', ' '),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            when (selectedMode) {
                PracticeMode.TIMED_SKETCHING -> TimedSketchingSettings(
                    onSettingsChange = {
                            newConfig -> timedConfig = newConfig
                    }
                )
                PracticeMode.BLACKOUT_PRACTICE -> BlackoutSettings(
                    onSettingsChange = {
                        newConfig -> blackoutConfig = newConfig
                    }
                )
                PracticeMode.FREE_VIEWING -> FreeViewSettings(
                    onSettingsChange = {
                        newConfig -> freeViewConfig = newConfig
                    }
                )
                null -> {
                    Text(
                        "Select a practice mode to continue",
                        style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Button(
                onClick = {
                    folderPath?.let { path ->
                        val settings = when (selectedMode) {
                            PracticeMode.TIMED_SKETCHING -> {

                                println("$timedConfig")
                                ViewerSettings(
                                    folderPath = path,
                                    mode = PracticeMode.TIMED_SKETCHING,
                                    timerEnabled = true,
                                    timerDuration = timedConfig.duration,
                                    hideArrowsInTimer = timedConfig.hideArrows,
                                    randomMode = timedConfig.randomMode,
                                    imageLimit = timedConfig.imageLimit
                                )
                            }

                            PracticeMode.BLACKOUT_PRACTICE -> {

                                println("$blackoutConfig")
                            ViewerSettings(
                                folderPath = path,
                                mode = PracticeMode.BLACKOUT_PRACTICE,
                                blackoutEnabled = true,
                                blackoutDuration = blackoutConfig.duration,
                                randomMode = blackoutConfig.randomMode,
                                imageLimit = blackoutConfig.imageLimit)
                            }


                            PracticeMode.FREE_VIEWING -> {
                                println("$freeViewConfig")
                                ViewerSettings(
                                    folderPath = path,
                                    mode = PracticeMode.FREE_VIEWING,
                                    randomMode = freeViewConfig.randomMode,
                                    imageLimit = freeViewConfig.imageLimit
                                )
                            }

                            null -> return@let
                        }
                        onStart(settings)
                    }
                },
                enabled = folderPath != null && selectedMode != null,
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text("Start Viewing")
            }
        }
    }
}