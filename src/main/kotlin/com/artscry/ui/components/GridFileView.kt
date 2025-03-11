package com.artscry.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun GridFileView(
    entries: List<File>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    onFolderOpen: (File) -> Unit,
    onFolderSelect: (File) -> Unit
) {
    val gridState = rememberLazyGridState()

    LaunchedEffect(selectedIndex) {
        val columnsCount = 4
        val row = selectedIndex / columnsCount
        gridState.scrollToItem(row * columnsCount)
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        state = gridState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(entries.size) { index ->
            val entry = entries[index]
            val isSelected = index == selectedIndex

            if (entry.isDirectory) {
                FolderItem(
                    folder = entry,
                    isSelected = isSelected,
                    onClick = {
                        onSelectionChange(index)
                        onFolderSelect(entry)
                    },
                    onDoubleClick = { onFolderOpen(entry) }
                )
            } else {
                FilePreview(
                    file = entry,
                    isSelected = isSelected,
                )
            }
        }
    }
}
