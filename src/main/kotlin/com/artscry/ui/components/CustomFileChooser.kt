package com.artscry.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.artscry.core.domain.model.FavoriteLocation
import com.artscry.core.domain.model.ViewMode
import com.artscry.util.PreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    var viewMode by remember { mutableStateOf(ViewMode.GRID) }
    var selectedIndex by remember { mutableStateOf(0) }
    var columnsCount by remember { mutableStateOf(8) }
    var isSearchFocused by remember { mutableStateOf(false) }

    val lastSelectedIndices = remember { mutableMapOf<String, Int>() }

    val mainFocusRequester = remember { FocusRequester() }
    val searchFieldFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    val recentFolders = remember { mutableStateOf(PreferencesManager.getRecentFolders()) }
    var favorites by remember { mutableStateOf(loadFavorites()) }
    var showAddFavoriteDialog by remember { mutableStateOf(false) }
    var previousSelectedIndex by remember { mutableStateOf(selectedIndex) }

    val density = LocalDensity.current
    val gridSizeModifier = Modifier.onSizeChanged { size ->
        with(density) {
            val estimatedColumns = (size.width / 150.dp.toPx()).toInt().coerceAtLeast(1)
            columnsCount = estimatedColumns
        }
    }

    val entries = remember(currentPath, searchQuery) {
        currentPath.toFile().listFiles()
            ?.filter { file ->
                (file.isDirectory || file.extension.lowercase() in setOf(
                    "jpg", "jpeg", "png", "gif", "bmp"
                )) &&
                        (searchQuery.isEmpty() || file.name.contains(searchQuery, ignoreCase = true))
            }
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            ?: emptyList()
    }

    LaunchedEffect(Unit) {
        println("CUSTOM FILE CHOOSER LAUNCHED")
        mainFocusRequester.requestFocus()
    }

    LaunchedEffect(currentPath) {
        searchQuery = ""
        selectedIndex = lastSelectedIndices[currentPath.toString()] ?: 0
        isSearchFocused = false
        delay(100)
        mainFocusRequester.requestFocus()
    }

    LaunchedEffect(isSearchFocused) {
        if (isSearchFocused) {
            searchFieldFocusRequester.requestFocus()
        } else {
            mainFocusRequester.requestFocus()
        }
    }

    DialogWindow(
        onCloseRequest = onDismiss,
        state = rememberDialogState(
            position = WindowPosition(Alignment.Center),
            width = 1400.dp,
            height = 900.dp
        ),
        title = "Select Folder",
        resizable = true
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.medium,
            elevation = 8.dp
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                FileChooserSidebar(
                    recentFolders = recentFolders.value,
                    favorites = favorites,
                    onFolderClick = { currentPath = Paths.get(it) },
                    onFavoriteClick = { currentPath = Paths.get(it) },
                    onRemoveFavorite = { favorite ->
                        favorites = favorites.filter { it.path != favorite.path }
                        saveFavorites(favorites)
                    },
                    onAddCurrentLocation = { showAddFavoriteDialog = true },
                    onClearRecentFolders = {
                        PreferencesManager.clearRecentFolders()
                        recentFolders.value = emptyList()
                    },
                    modifier = Modifier
                        .width(250.dp)
                        .fillMaxHeight()
                        .padding(end = 8.dp)
                )

                Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                    BreadcrumbNavigationBar(
                        currentPath = currentPath,
                        onNavigate = { path -> currentPath = path },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { viewMode = ViewMode.GRID },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = if (viewMode == ViewMode.GRID)
                                        MaterialTheme.colors.primary
                                    else
                                        MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            ) {
                                Text("Grid")
                            }

                            TextButton(
                                onClick = { viewMode = ViewMode.LIST },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = if (viewMode == ViewMode.LIST)
                                        MaterialTheme.colors.primary
                                    else
                                        MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            ) {
                                Text("List")
                            }
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .width(250.dp)
                                .focusRequester(searchFieldFocusRequester)
                                .onPreviewKeyEvent { event ->
                                    when {
                                        event.type == KeyEventType.KeyDown && event.key == Key.Enter -> {
                                            isSearchFocused = false

                                            if (searchQuery.isNotEmpty() && entries.isNotEmpty()) {
                                                selectedIndex = 0

                                                coroutineScope.launch {
                                                    delay(50)
                                                    mainFocusRequester.requestFocus()
                                                }
                                            } else {
                                                mainFocusRequester.requestFocus()
                                            }
                                            true
                                        }

                                        event.type == KeyEventType.KeyDown && event.key == Key.Escape -> {
                                            if (searchQuery.isNotEmpty()) {
                                                searchQuery = ""
                                                true
                                            } else {
                                                isSearchFocused = false
                                                mainFocusRequester.requestFocus()
                                                true
                                            }
                                        }

                                        event.type == KeyEventType.KeyDown && event.key == Key.Tab && !event.isShiftPressed -> {
                                            isSearchFocused = false
                                            mainFocusRequester.requestFocus()
                                            true
                                        }

                                        else -> false
                                    }
                                }
                                .focusProperties {
                                    next = mainFocusRequester
                                    previous = mainFocusRequester
                                },
                            placeholder = { Text("Search") },
                            singleLine = true,
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Text("×")
                                    }
                                }
                            },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                backgroundColor = MaterialTheme.colors.surface
                            )
                        )
                    }

                    val gridState = rememberLazyGridState()


                    LaunchedEffect(selectedIndex, columnsCount) {
                        if (selectedIndex == previousSelectedIndex) return@LaunchedEffect

                        val layoutInfo = gridState.layoutInfo
                        val visibleItems = layoutInfo.visibleItemsInfo
                        val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset

                        val targetItem = visibleItems.find { it.index == selectedIndex }

                        if (targetItem == null) {
                            gridState.animateScrollToItem(selectedIndex)
                        } else {
                            val itemTop = targetItem.offset.y - layoutInfo.viewportStartOffset
                            val itemBottom = itemTop + targetItem.size.height
                            val margin = with(density) { 8.dp.toPx() }

                            val scrollUpNeeded = itemTop < margin
                            val scrollDownNeeded = itemBottom > viewportHeight - margin

                            when {
                                scrollUpNeeded -> {
                                    val scrollOffset = (targetItem.offset.y - margin).toInt()
                                    gridState.animateScrollBy(scrollOffset.toFloat())
                                }

                                scrollDownNeeded -> {
                                    val missingSpace = itemBottom - (viewportHeight - margin)

                                    val extraPush = with(density) { 24.dp.toPx() }
                                    gridState.animateScrollBy(missingSpace + extraPush)
                                }
                            }
                        }

                        previousSelectedIndex = selectedIndex
                    }

                    when (viewMode) {
                        ViewMode.GRID -> {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 150.dp),
                                state = gridState,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .then(gridSizeModifier)
                                    .focusRequester(mainFocusRequester)
                                    .focusable(true)
                                    .onPreviewKeyEvent { keyEvent ->
                                        when {
                                            keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionDown -> {
                                                val newIndex = selectedIndex + columnsCount
                                                if (newIndex < entries.size) selectedIndex = newIndex
                                                true
                                            }

                                            keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionUp -> {
                                                val newIndex = selectedIndex - columnsCount
                                                if (newIndex >= 0) selectedIndex = newIndex
                                                true
                                            }

                                            keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionRight -> {
                                                if ((selectedIndex + 1) % columnsCount != 0 && selectedIndex < entries.size - 1) {
                                                    selectedIndex++
                                                }
                                                true
                                            }

                                            keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionLeft -> {
                                                if (selectedIndex % columnsCount != 0 && selectedIndex > 0) {
                                                    selectedIndex--
                                                }
                                                true
                                            }

                                            keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.J -> {
                                                val newIndex = selectedIndex + columnsCount
                                                if (newIndex < entries.size) selectedIndex = newIndex
                                                true
                                            }

                                            keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.K -> {
                                                val newIndex = selectedIndex - columnsCount
                                                if (newIndex >= 0) selectedIndex = newIndex
                                                true
                                            }

                                            keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.L -> {
                                                if ((selectedIndex + 1) % columnsCount != 0 && selectedIndex < entries.size - 1) {
                                                    selectedIndex++
                                                    true
                                                } else {
                                                    false
                                                }
                                            }

                                            keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.H -> {
                                                if (selectedIndex % columnsCount != 0 && selectedIndex > 0) {
                                                    selectedIndex--
                                                    true
                                                } else {
                                                    false
                                                }
                                            }

                                            keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Enter -> {
                                                entries.getOrNull(selectedIndex)?.let { file ->
                                                    if (file.isDirectory) {
                                                        currentPath = file.toPath()
                                                        true
                                                    } else false
                                                } ?: false
                                            }

                                            keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Backspace -> {
                                                currentPath.parent?.let { parentDir ->
                                                    val currentFile = entries.getOrNull(selectedIndex)
                                                    if (currentFile != null) {
                                                        val currentDirName = currentPath.fileName.toString()
                                                        lastSelectedIndices[currentPath.toString()] = selectedIndex

                                                        val parentEntries = parentDir.toFile().listFiles()
                                                            ?.filter { file ->
                                                                file.isDirectory || file.extension.lowercase() in setOf(
                                                                    "jpg",
                                                                    "jpeg",
                                                                    "png",
                                                                    "gif",
                                                                    "bmp"
                                                                )
                                                            }
                                                            ?.sortedWith(
                                                                compareBy(
                                                                    { !it.isDirectory },
                                                                    { it.name.lowercase() })
                                                            )
                                                            ?: emptyList()

                                                        val indexInParent =
                                                            parentEntries.indexOfFirst { it.name == currentDirName }

                                                        currentPath = parentDir

                                                        if (indexInParent >= 0) {
                                                            coroutineScope.launch {
                                                                delay(50)
                                                                selectedIndex = indexInParent
                                                            }
                                                        }
                                                    } else {
                                                        currentPath = parentDir
                                                    }
                                                    true
                                                } ?: false
                                            }

                                            keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.F && keyEvent.isCtrlPressed -> {
                                                isSearchFocused = true
                                                searchFieldFocusRequester.requestFocus()
                                                true
                                            }

                                            keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Spacebar -> {
                                                entries.getOrNull(selectedIndex)?.let { file ->
                                                    if (file.isDirectory) {
                                                        selectedFolder = file.toPath()
                                                        onFolderSelected(file.absolutePath)
                                                        true
                                                    } else {
                                                        onFolderSelected(currentPath.toString())
                                                        true
                                                    }
                                                } ?: run {
                                                    onFolderSelected(currentPath.toString())
                                                    true
                                                }
                                            }

                                            else -> false
                                        }
                                    }
                            ) {
                                items(entries.size) { index ->
                                    val entry = entries[index]
                                    val isSelected = index == selectedIndex

                                    if (entry.isDirectory) {
                                        FolderItem(
                                            folder = entry,
                                            isSelected = isSelected || selectedFolder?.toString() == entry.absolutePath,
                                            onClick = {
                                                selectedIndex = index
                                                selectedFolder = entry.toPath()
                                            },
                                            onDoubleClick = {
                                                selectedIndex = 0
                                                currentPath = entry.toPath()
                                                selectedFolder = null
                                            }
                                        )
                                    } else {
                                        FilePreview(
                                            file = entry,
                                            isSelected = isSelected
                                        )
                                    }
                                }
                            }
                        }

                        ViewMode.LIST -> {
                            val listState = rememberLazyListState()

                            LaunchedEffect(selectedIndex) {
                                listState.scrollToItem(selectedIndex)
                            }

                            LazyColumn(
                                state = listState,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(mainFocusRequester)
                                    .focusable(true)
                            ) {
                                items(entries.size) { index ->
                                    val entry = entries[index]
                                    val isSelected = index == selectedIndex

                                    FileListItem(
                                        file = entry,
                                        isSelected = isSelected,
                                        onClick = {
                                            selectedIndex = index
                                            if (entry.isDirectory) {
                                                selectedFolder = entry.toPath()
                                            }
                                        },
                                        onDoubleClick = {
                                            if (entry.isDirectory) {
                                                currentPath = entry.toPath()
                                                selectedFolder = null
                                                selectedIndex = 0
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Shortcuts: Arrows/HJKL to navigate | Enter to open | Space to select | Backspace to go up | Ctrl+F to search",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Row {
                            TextButton(onClick = onDismiss) {
                                Text("Cancel")
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val pathToUse = selectedFolder ?: currentPath
                                    println("SELECTED PATH: $pathToUse")
                                    onFolderSelected(pathToUse.toString())
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
    }

    if (showAddFavoriteDialog) {
        AddFavoriteDialog(
            currentPath = currentPath.toString(),
            onAdd = { name, path ->
                val newFavorite = FavoriteLocation(
                    path = path,
                    name = name
                )
                favorites = favorites + newFavorite
                PreferencesManager.saveFavorites(favorites)
                showAddFavoriteDialog = false
            },
            onDismiss = { showAddFavoriteDialog = false }
        )
    }
}



private fun loadFavorites(): List<FavoriteLocation> {
    return PreferencesManager.getFavorites()
}

private fun saveFavorites(favorites: List<FavoriteLocation>) {
    PreferencesManager.saveFavorites(favorites)
}