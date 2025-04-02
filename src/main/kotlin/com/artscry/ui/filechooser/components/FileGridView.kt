package com.artscry.ui.filechooser.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import com.artscry.ui.components.FilePreview
import com.artscry.ui.components.FolderItem
import com.artscry.ui.filechooser.utils.FileChooserKeyHandler
import java.io.File

@Composable
fun FileGridView(
    entries: List<File>,
    selectedIndex: Int,
    columnsCount: Int,
    onSelect: (Int) -> Unit,
    onDoubleClick: (Int) -> Unit,
    keyHandler: FileChooserKeyHandler,
    focusRequester: FocusRequester,
    gridState: LazyGridState = rememberLazyGridState(),
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(selectedIndex) {
        if (entries.isEmpty()) return@LaunchedEffect

        val targetIndex = selectedIndex.coerceIn(0, entries.size - 1)
        val scrollIndex = (targetIndex / columnsCount) * columnsCount
        gridState.scrollToItem(scrollIndex)
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        state = gridState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable(true)
            .onPreviewKeyEvent { keyHandler.handleGridKeyEvent(it) }
    ) {
        items(entries.size) { index ->
            val entry = entries[index]
            val isSelected = index == selectedIndex

            if (entry.isDirectory) {
                FolderItem(
                    folder = entry,
                    isSelected = isSelected,
                    onClick = { onSelect(index) },
                    onDoubleClick = { onDoubleClick(index) }
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
