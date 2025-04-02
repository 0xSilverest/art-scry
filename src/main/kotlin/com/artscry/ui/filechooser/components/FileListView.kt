package com.artscry.ui.filechooser.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import com.artscry.ui.components.FileListItem
import com.artscry.ui.filechooser.utils.FileChooserKeyHandler
import java.io.File

@Composable
fun FileListView(
    entries: List<File>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDoubleClick: (Int) -> Unit,
    keyHandler: FileChooserKeyHandler,
    focusRequester: FocusRequester,
    listState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(selectedIndex) {
        if (entries.isEmpty()) return@LaunchedEffect

        val targetIndex = selectedIndex.coerceIn(0, entries.size - 1)
        listState.scrollToItem(targetIndex)
    }

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable(true)
            .onPreviewKeyEvent { keyHandler.handleListKeyEvent(it) }
    ) {
        items(entries.size) { index ->
            val entry = entries[index]
            val isSelected = index == selectedIndex

            FileListItem(
                file = entry,
                isSelected = isSelected,
                onClick = { onSelect(index) },
                onDoubleClick = { onDoubleClick(index) }
            )
        }
    }
}
