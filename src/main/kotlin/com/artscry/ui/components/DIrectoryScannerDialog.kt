package com.artscry.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.artscry.core.domain.model.DialogState
import com.artscry.core.domain.model.Tag
import com.artscry.data.repository.DbRepository
import com.artscry.ui.filechooser.FileChooserModule
import com.artscry.util.DirectoryScanner
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun DirectoryScannerStep1(
    onFolderSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    println("STEP 1: Showing folder chooser")
    FileChooserModule.showFolderChooser(
        onFolderSelected = { path ->
            println("FOLDER SELECTED: $path")
            onFolderSelected(path)
        },
        onDismiss = onDismiss
    )
}


@Composable
fun DirectoryScannerStep2(
    directory: String,
    onStartScan: (String?) -> Unit,
    onChangeDirectory: () -> Unit,
    onDismiss: () -> Unit
) {
    println("STEP 2: Showing confirmation dialog for: $directory")

    var useBasePathFilter by remember { mutableStateOf(false) }
    var basePathToIgnore by remember { mutableStateOf(directory) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(max = 500.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Directory Scanner - Confirm",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    "Selected Directory: ${File(directory).name}",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    directory,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = useBasePathFilter,
                            onCheckedChange = { useBasePathFilter = it }
                        )

                        Text(
                            "Only extract tags from the selected directory and below",
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    if (useBasePathFilter) {
                        Text(
                            "This will ignore parent directories like '/home/user/downloads/' and only use tags from '${File(directory).name}' and its subdirectories.",
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.padding(start = 32.dp, top = 4.dp, bottom = 4.dp)
                        )
                    }
                }

                Text(
                    "This will scan all subdirectories and tag images with their parent directory names. You'll be able to review the detected tags before they're applied.",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onChangeDirectory,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.surface
                        )
                    ) {
                        Text("Change Directory")
                    }

                    Button(
                        onClick = {
                            onStartScan(if (useBasePathFilter) directory else null)
                        }
                    ) {
                        Text("Start Scanning")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun DirectoryScannerAnalyzingStep(
    directory: String,
    basePathToIgnore: String?,
    repository: DbRepository,
    onTagsDetected: (List<Tag>) -> Unit,
    onDismiss: () -> Unit
) {
    println("ANALYZING STEP: Detecting tags in: $directory")
    val scope = rememberCoroutineScope()
    var progress by remember { mutableStateOf(0f) }
    var currentOperation by remember { mutableStateOf("Initializing...") }

    LaunchedEffect(Unit) {
        println("STARTING TAG DETECTION")
        scope.launch {
            try {
                DirectoryScanner.detectTagsInDirectory(
                    directory,
                    basePathToIgnore,
                    progressCallback = { message, processed, total ->
                        progress = processed.toFloat() / total
                        currentOperation = message
                    }
                ).also { tags ->
                    println("DETECTED ${tags.size} TAGS")
                    onTagsDetected(tags)
                }
            } catch (e: Exception) {
                println("TAG DETECTION ERROR: ${e.message}")
                e.printStackTrace()
                onDismiss()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(max = 300.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Analyzing Directory",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(60.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    currentOperation,
                    style = MaterialTheme.typography.body2
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Progress: ${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.body1
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun DirectoryScannerStep3(
    directory: String,
    repository: DbRepository,
    basePathToIgnore: String?,
    selectedTags: List<Tag>,
    onScanComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    println("STEP 3: Starting scan of: $directory with ${selectedTags.size} selected tags")
    val scope = rememberCoroutineScope()
    var progress by remember { mutableStateOf(0f) }
    var currentOperation by remember { mutableStateOf("Initializing...") }

    var scanMessages by remember { mutableStateOf(listOf("Starting scan...")) }
    var isComplete by remember { mutableStateOf(false) }

    var lastUpdateTimeMs by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        println("STARTING SCAN COROUTINE")
        scope.launch {
            try {
                DirectoryScanner.scanDirectoryRecursively(
                    directory,
                    repository,
                    progressCallback = { dirName, processed, total ->
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastUpdateTimeMs > 200 || processed == total) {
                            progress = processed.toFloat() / total
                            currentOperation = "Processing: $dirName"

                            val message = "Processed $processed/$total: $dirName"
                            scanMessages = (scanMessages + message).takeLast(15)

                            lastUpdateTimeMs = currentTime
                        }
                    },
                    basePathToIgnore = basePathToIgnore,
                    selectedTags = selectedTags
                )
                scanMessages = (scanMessages + "Scan completed successfully!").takeLast(15)
                isComplete = true
                println("SCAN COMPLETED")
            } catch (e: Exception) {
                scanMessages = (scanMessages + "Error: ${e.message}").takeLast(15)
                isComplete = true
                println("SCAN ERROR: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Directory Scanner - Scanning",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        progress = progress,
                        modifier = Modifier.size(60.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        currentOperation,
                        style = MaterialTheme.typography.body2
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Progress: ${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.body1
                    )
                }

                if (scanMessages.isNotEmpty()) {
                    Text(
                        "Operation Log:",
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 8.dp)
                    ) {
                        items(scanMessages.reversed()) { message ->
                            Text(
                                message,
                                style = MaterialTheme.typography.caption,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (isComplete) {
                        Button(onClick = onScanComplete) {
                            Text("Done")
                        }
                    } else {
                        Button(onClick = onDismiss) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DirectoryScannerFlow(
    repository: DbRepository,
    onScanComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(DialogState.SELECTING_DIRECTORY) }
    var selectedDirectory by remember { mutableStateOf<String?>(null) }
    var basePathToIgnore by remember { mutableStateOf<String?>(null) }
    var detectedTags by remember { mutableStateOf<List<Tag>>(emptyList()) }
    var selectedTags by remember { mutableStateOf<List<Tag>>(emptyList()) }

    println("SCANNER FLOW - Current Step: $step")

    when (step) {
        DialogState.SELECTING_DIRECTORY -> DirectoryScannerStep1(
            onFolderSelected = { path ->
                selectedDirectory = path
                step = DialogState.CONFIRMING
            },
            onDismiss = onDismiss
        )

        DialogState.CONFIRMING -> selectedDirectory?.let { dir ->
            DirectoryScannerStep2(
                directory = dir,
                onStartScan = { basePath ->
                    basePathToIgnore = basePath
                    step = DialogState.ANALYZING
                },
                onChangeDirectory = { step = DialogState.SELECTING_DIRECTORY },
                onDismiss = onDismiss
            )
        }

        DialogState.ANALYZING -> selectedDirectory?.let { dir ->
            DirectoryScannerAnalyzingStep(
                directory = dir,
                basePathToIgnore = basePathToIgnore,
                repository = repository,
                onTagsDetected = { tags ->
                    detectedTags = tags
                    selectedTags = tags
                    step = DialogState.REVIEWING_TAGS
                },
                onDismiss = onDismiss
            )
        }

        DialogState.REVIEWING_TAGS -> {
            TagReviewDialog(
                detectedTags = detectedTags,
                onTagsSelected = { tags ->
                    selectedTags = tags
                    step = DialogState.SCANNING
                },
                onCancel = onDismiss
            )
        }

        DialogState.SCANNING -> selectedDirectory?.let { dir ->
            DirectoryScannerStep3(
                directory = dir,
                repository = repository,
                basePathToIgnore = basePathToIgnore,
                selectedTags = selectedTags,
                onScanComplete = onScanComplete,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
fun DirectoryScannerDialog(
    repository: DbRepository,
    onScanComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    DirectoryScannerFlow(
        repository = repository,
        onScanComplete = onScanComplete,
        onDismiss = onDismiss
    )
}