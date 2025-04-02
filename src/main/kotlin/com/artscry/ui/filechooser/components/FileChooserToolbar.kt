package com.artscry.ui.filechooser.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import com.artscry.core.domain.model.ViewMode
import com.artscry.ui.filechooser.utils.FileChooserKeyHandler

@Composable
fun FileChooserToolbar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    keyHandler: FileChooserKeyHandler,
    searchFocusRequester: FocusRequester,
    mainFocusRequester: FocusRequester
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { onViewModeChange(ViewMode.GRID) },
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
                onClick = { onViewModeChange(ViewMode.LIST) },
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

        Spacer(modifier = Modifier.weight(1f))

        SearchField(
            searchQuery = searchQuery,
            onSearchChange = onSearchChange,
            onClearSearch = onClearSearch,
            keyHandler = keyHandler,
            searchFocusRequester = searchFocusRequester,
            mainFocusRequester = mainFocusRequester
        )
    }
}

@Composable
private fun SearchField(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    keyHandler: FileChooserKeyHandler,
    searchFocusRequester: FocusRequester,
    mainFocusRequester: FocusRequester
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        modifier = Modifier
            .width(250.dp)
            .focusRequester(searchFocusRequester)
            .onPreviewKeyEvent { keyHandler.handleSearchKeyEvent(it) }
            .focusProperties {
                next = mainFocusRequester
                previous = mainFocusRequester
            },
        placeholder = { Text("Search") },
        singleLine = true,
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                TextButton(onClick = onClearSearch) {
                    Text("×")
                }
            }
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = MaterialTheme.colors.surface
        )
    )
}
