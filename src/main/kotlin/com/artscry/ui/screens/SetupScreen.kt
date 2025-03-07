package com.artscry.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artscry.core.domain.model.*
import com.artscry.data.repository.DbRepository
import com.artscry.ui.components.*
import com.artscry.ui.theme.ThemeState
import com.artscry.ui.theme.artScryColors
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

@Composable
fun SetupScreen(
    imageRepository: DbRepository,
    onStart: (ViewerSettings) -> Unit
) {
    val scope = rememberCoroutineScope()

    var showDirectoryScanner by remember { mutableStateOf(false) }
    var showTagBasedLoader by remember { mutableStateOf(false) }

    var selectedMode by remember { mutableStateOf<PracticeMode?>(null) }
    var folderPath by remember { mutableStateOf<String?>(null) }
    var showModeMenu by remember { mutableStateOf(false) }
    var showFolderSelector by remember { mutableStateOf(false) }
    var showTagReviewDialog by remember { mutableStateOf(false) }
    var selectedTags by remember { mutableStateOf<List<Tag>>(emptyList()) }

    val scrollState = rememberScrollState()

    var selectedImages by remember { mutableStateOf<List<ImageReference>>(emptyList()) }
    var timedConfig by remember { mutableStateOf(TimedSketchingConfig()) }
    var blackoutConfig by remember { mutableStateOf(BlackoutConfig()) }
    var freeViewConfig by remember { mutableStateOf(FreeViewConfig()) }

    Surface(color = MaterialTheme.colors.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "ArtScry",
                        style = MaterialTheme.typography.h4,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary
                    )
                }

                IconButton(
                    onClick = { ThemeState.isDarkMode = !ThemeState.isDarkMode },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    Image(
                        painter = painterResource(
                            if (ThemeState.isDarkMode) "icons/crescent.png" else "icons/sun.png"
                        ),
                        contentDescription = "Toggle theme",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(
                            if (ThemeState.isDarkMode) Color.White else Color.Black
                        )
                    )
                }
            }
            Text(
                "Setup",
                style = MaterialTheme.typography.h5,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.8f)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp,
                backgroundColor = MaterialTheme.colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colors.primary
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "1",
                                        color = MaterialTheme.colors.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Text(
                                "Select a folder with reference images",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        if (folderPath == null) {
                            FolderSelector { path ->
                                folderPath = path
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, MaterialTheme.colors.primary.copy(alpha = 0.3f)),
                                backgroundColor = MaterialTheme.colors.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(40.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                "📁",
                                                fontSize = 20.sp
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(16.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = File(folderPath!!).name,
                                            style = MaterialTheme.typography.subtitle1,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState())
                                        ) {
                                            Text(
                                                text = folderPath!!,
                                                style = MaterialTheme.typography.caption,
                                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            showFolderSelector = true
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = MaterialTheme.colors.surface,
                                            contentColor = MaterialTheme.colors.onSurface
                                        ),
                                        elevation = ButtonDefaults.elevation()
                                    ) {
                                        Text("Change")
                                    }
                                }
                            }

                            if (selectedTags.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Auto-detected Tags:",
                                    style = MaterialTheme.typography.subtitle2
                                )
                                Spacer(Modifier.height(4.dp))
                                FlowRow(
                                    mainAxisSpacing = 8,
                                    crossAxisSpacing = 8
                                ) {
                                    selectedTags.forEach { tag ->
                                        TagChip(tag = tag, selected = true)
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                TextButton(onClick = { showTagReviewDialog = true }) {
                                    Text("Edit Tags")
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showDirectoryScanner = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Scan Directory Tree")
                            }

                            OutlinedButton(
                                onClick = { showTagBasedLoader = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Load By Tags")
                            }
                        }

                    }

                    Divider()

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = if (folderPath != null) MaterialTheme.colors.primary
                                else MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "2",
                                        color = if (folderPath != null) MaterialTheme.colors.onPrimary
                                        else MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Text(
                                "Select a practice mode",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Medium,
                                color = if (folderPath != null) MaterialTheme.colors.onSurface
                                else MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { if (folderPath != null) showModeMenu = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = folderPath != null,
                                border = BorderStroke(
                                    1.dp,
                                    if (selectedMode != null) MaterialTheme.colors.primary
                                    else MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        selectedMode?.name?.replace('_', ' ')?.lowercase()
                                            ?.split(' ')
                                            ?.joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                                            ?: "Select Practice Mode",
                                        style = MaterialTheme.typography.button,
                                        color = if (selectedMode != null) MaterialTheme.colors.onSurface else MaterialTheme.colors.onSurface.copy(
                                            alpha = 0.4f
                                        )
                                    )

                                    Text("▼", fontSize = 12.sp)
                                }
                            }

                            DropdownMenu(
                                expanded = showModeMenu,
                                onDismissRequest = { showModeMenu = false },
                                modifier = Modifier.background(MaterialTheme.artScryColors.surfaceElevated)
                            ) {
                                PracticeMode.entries.forEach { mode ->
                                    DropdownMenuItem(
                                        onClick = {
                                            selectedMode = mode
                                            showModeMenu = false
                                        }
                                    ) {
                                        val modeName = mode.name.lowercase(Locale.ROOT).replace('_', ' ')
                                            .split(' ')
                                            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

                                        Text(
                                            modeName,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = selectedMode != null) {
                        Column {
                            Divider()

                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(32.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (selectedMode != null) MaterialTheme.colors.primary
                                    else MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "3",
                                            color = if (selectedMode != null) MaterialTheme.colors.onPrimary
                                            else MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                Text(
                                    "Configure settings",
                                    style = MaterialTheme.typography.h6,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            when (selectedMode) {
                                PracticeMode.TIMED_SKETCHING -> TimedSketchingSettings(
                                    onSettingsChange = { newConfig ->
                                        timedConfig = newConfig
                                    }
                                )

                                PracticeMode.BLACKOUT_PRACTICE -> BlackoutSettings(
                                    onSettingsChange = { newConfig ->
                                        blackoutConfig = newConfig
                                    }
                                )

                                PracticeMode.FREE_VIEWING -> FreeViewSettings(
                                    onSettingsChange = { newConfig ->
                                        freeViewConfig = newConfig
                                    }
                                )

                                null -> {}
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    folderPath?.let { path ->
                        val settings = when (selectedMode) {
                            PracticeMode.TIMED_SKETCHING -> {
                                ViewerSettings(
                                    folderPath = path,
                                    mode = PracticeMode.TIMED_SKETCHING,
                                    tags = selectedTags,
                                    preloadedImages = if (path == "Tagged Images") selectedImages else null,
                                    timerEnabled = true,
                                    timerDuration = timedConfig.duration,
                                    hideArrowsInTimer = timedConfig.hideArrows,
                                    randomMode = timedConfig.randomMode,
                                    imageLimit = timedConfig.imageLimit
                                )
                            }

                            PracticeMode.BLACKOUT_PRACTICE -> {
                                ViewerSettings(
                                    folderPath = path,
                                    mode = PracticeMode.BLACKOUT_PRACTICE,
                                    tags = selectedTags,
                                    preloadedImages = if (path == "Tagged Images") selectedImages else null,
                                    blackoutEnabled = true,
                                    blackoutDuration = blackoutConfig.duration,
                                    practiceTimerEnabled = blackoutConfig.practiceTimerEnabled,
                                    practiceTimerDuration = blackoutConfig.practiceTimerDuration,
                                    randomMode = blackoutConfig.randomMode,
                                    imageLimit = blackoutConfig.imageLimit
                                )
                            }

                            PracticeMode.FREE_VIEWING -> {
                                ViewerSettings(
                                    folderPath = path,
                                    mode = PracticeMode.FREE_VIEWING,
                                    tags = selectedTags,
                                    preloadedImages = if (path == "Tagged Images") selectedImages else null,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Start Viewing",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }


        if (showDirectoryScanner) {
            DirectoryScannerDialog(
                repository = imageRepository,
                onScanComplete = {
                    showDirectoryScanner = false
                },
                onDismiss = { showDirectoryScanner = false }
            )
        }

        if (showTagBasedLoader) {
            TagBasedImageLoader(
                repository = imageRepository,
                onImagesLoaded = { images ->
                    folderPath = "Tagged Images"
                    selectedImages = images
                    selectedMode = PracticeMode.FREE_VIEWING
                },
                onDismiss = { showTagBasedLoader = false }
            )
        }

        if (showFolderSelector) {
            FolderSelector { path ->
                folderPath = path
            }
        }
    }
}