package com.artscry.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun ListFileView(
    entries: List<File>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    onFolderOpen: (File) -> Unit,
    onFolderSelect: (File) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedIndex) {
        listState.scrollToItem(selectedIndex)
    }

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(entries.size) { index ->
            val entry = entries[index]
            val isSelected = index == selectedIndex

            FileListItem(
                file = entry,
                isSelected = isSelected,
                onClick = {
                    onSelectionChange(index)
                    if (entry.isDirectory) {
                        onFolderSelect(entry)
                    }
                },
                onDoubleClick = {
                    if (entry.isDirectory) {
                        onFolderOpen(entry)
                    }
                }
            )
        }
    }
}

