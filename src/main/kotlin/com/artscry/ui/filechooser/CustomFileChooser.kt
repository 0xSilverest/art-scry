package com.artscry.ui.filechooser

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.artscry.core.domain.model.ViewMode
import com.artscry.ui.components.AddFavoriteDialog
import com.artscry.ui.components.BreadcrumbNavigationBar
import com.artscry.ui.components.FileChooserSidebar
import com.artscry.ui.filechooser.components.FileChooserFooter
import com.artscry.ui.filechooser.components.FileChooserToolbar
import com.artscry.ui.filechooser.components.FileGridView
import com.artscry.ui.filechooser.components.FileListView
import com.artscry.ui.filechooser.models.FileChooserState
import com.artscry.ui.filechooser.utils.FileChooserKeyHandler
import kotlinx.coroutines.delay

@Composable
fun CustomFileChooser(
    initialDirectory: String? = null,
    onFolderSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val state = remember { FileChooserState(initialDirectory, scope) }

    val mainFocusRequester = remember { FocusRequester() }
    val searchFocusRequester = remember { FocusRequester() }

    val keyHandler = remember {
        FileChooserKeyHandler(state, mainFocusRequester, searchFocusRequester, scope)
    }

    val density = LocalDensity.current

    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        delay(100)
        mainFocusRequester.requestFocus()
    }

    LaunchedEffect(state.isSearchFocused.value) {
        if (state.isSearchFocused.value) {
            searchFocusRequester.requestFocus()
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
                    recentFolders = state.recentFolders.value,
                    favorites = state.favorites.value,
                    onFolderClick = { state.navigateToFolder(java.nio.file.Paths.get(it)) },
                    onFavoriteClick = { state.navigateToFolder(java.nio.file.Paths.get(it)) },
                    onRemoveFavorite = { state.removeFavorite(it) },
                    onAddCurrentLocation = { state.showAddFavoriteDialog.value = true },
                    onClearRecentFolders = { state.clearRecentFolders() },
                    modifier = Modifier
                        .width(250.dp)
                        .fillMaxHeight()
                        .padding(end = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .onSizeChanged { size ->
                            with(density) {
                                val estimatedColumns = (size.width / 150.dp.toPx()).toInt().coerceAtLeast(1)
                                state.updateColumnCount(estimatedColumns)
                            }
                        }
                ) {
                    BreadcrumbNavigationBar(
                        currentPath = state.currentPath.value,
                        onNavigate = { state.navigateToFolder(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    FileChooserToolbar(
                        searchQuery = state.searchQuery.value,
                        onSearchChange = { state.searchQuery.value = it },
                        onClearSearch = { state.clearSearch() },
                        viewMode = state.viewMode.value,
                        onViewModeChange = { state.viewMode.value = it },
                        keyHandler = keyHandler,
                        searchFocusRequester = searchFocusRequester,
                        mainFocusRequester = mainFocusRequester
                    )

                    when (state.viewMode.value) {
                        ViewMode.GRID -> FileGridView(
                            entries = state.entries.value,
                            selectedIndex = state.selectedIndex.value,
                            columnsCount = state.columnsCount.value,
                            onSelect = { state.selectItem(it) },
                            onDoubleClick = { state.openFolder(it) },
                            keyHandler = keyHandler,
                            focusRequester = mainFocusRequester,
                            gridState = gridState,
                            modifier = Modifier.weight(1f)
                        )

                        ViewMode.LIST -> FileListView(
                            entries = state.entries.value,
                            selectedIndex = state.selectedIndex.value,
                            onSelect = { state.selectItem(it) },
                            onDoubleClick = { state.openFolder(it) },
                            keyHandler = keyHandler,
                            focusRequester = mainFocusRequester,
                            listState = listState,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    FileChooserFooter(
                        onCancel = onDismiss,
                        onSelect = {
                            val selectedPath = state.selectFolder()
                            onFolderSelected(selectedPath)
                        },
                        currentPath = state.currentPath.value,
                        selectedFolder = state.selectedFolder.value
                    )

                }
            }
        }
    }

    if (state.showAddFavoriteDialog.value) {
        AddFavoriteDialog(
            currentPath = state.currentPath.value.toString(),
            onAdd = { name, path ->
                state.addCurrentToFavorites(name)
                state.showAddFavoriteDialog.value = false
            },
            onDismiss = {
                state.showAddFavoriteDialog.value = false
            }
        )
    }
}