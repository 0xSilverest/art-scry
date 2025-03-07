package com.artscry.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.artscry.core.domain.model.Tag

@Composable
fun TagFilterBar(
    selectedTags: List<com.artscry.core.domain.model.Tag>,
    availableTags: List<com.artscry.core.domain.model.Tag>,
    onTagSelected: (com.artscry.core.domain.model.Tag) -> Unit,
    onTagRemoved: (com.artscry.core.domain.model.Tag) -> Unit,
    onClearAll: () -> Unit,
    matchAllTags: Boolean,
    onMatchAllChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var isTagSelectorOpen by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val filteredTags = remember(searchText, availableTags, selectedTags) {
        if (searchText.isEmpty()) {
            availableTags.filter { it.id !in selectedTags.map { tag -> tag.id } }
                .sortedByDescending { tag -> tag.category != null }
        } else {
            availableTags.filter {
                it.id !in selectedTags.map { tag -> tag.id } &&
                        (it.name.contains(searchText, ignoreCase = true) ||
                                it.category?.contains(searchText, ignoreCase = true) == true)
            }
        }
    }

    Surface(
        elevation = 4.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            if (selectedTags.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Filters:",
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(selectedTags) { tag ->
                            TagChip(
                                tag = tag,
                                selected = true,
                                onRemove = { onTagRemoved(tag) }
                            )
                        }
                    }

                    IconButton(
                        onClick = onClearAll,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear all filters",
                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = matchAllTags,
                        onCheckedChange = onMatchAllChanged
                    )

                    Text(
                        text = if (matchAllTags) "Match all tags" else "Match any tag",
                        style = MaterialTheme.typography.body2
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search tags...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )

                IconButton(onClick = { isTagSelectorOpen = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add tag")
                }
            }

            if (searchText.isNotEmpty() && filteredTags.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredTags.take(5)) { tag ->
                        TagChip(
                            tag = tag,
                            onClick = {
                                onTagSelected(tag)
                                searchText = ""
                            }
                        )
                    }

                    if (filteredTags.size > 5) {
                        item {
                            TextButton(onClick = { isTagSelectorOpen = true }) {
                                Text("More...")
                            }
                        }
                    }
                }
            }
        }
    }

    if (isTagSelectorOpen) {
        TagSelectorDialog(
            allTags = availableTags,
            selectedTags = selectedTags,
            onTagSelected = {
                onTagSelected(it)
                searchText = ""
            },
            onDismiss = { isTagSelectorOpen = false }
        )
    }
}
