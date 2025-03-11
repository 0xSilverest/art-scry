package com.artscry.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.artscry.core.domain.model.Tag

@Composable
fun TagReviewDialog(
    detectedTags: List<Tag>,
    onTagsSelected: (List<Tag>) -> Unit,
    onCancel: () -> Unit
) {
    var selectedTags by remember { mutableStateOf(detectedTags.toSet()) }
    var searchQuery by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val tagsByCategory = remember(detectedTags) {
        detectedTags.groupBy { it.category ?: "Uncategorized" }
            .toSortedMap()
    }

    val filteredTagsByCategory = remember(tagsByCategory, searchQuery) {
        if (searchQuery.isBlank()) {
            tagsByCategory
        } else {
            tagsByCategory.mapValues { (_, tags) ->
                tags.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }.filter { it.value.isNotEmpty() }
        }
    }

    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Review and Select Tags",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${selectedTags.size} of ${detectedTags.size} tags selected",
                        style = MaterialTheme.typography.subtitle2
                    )

                    Row {
                        TextButton(onClick = { selectedTags = emptySet() }) {
                            Text("Clear All")
                        }

                        TextButton(onClick = { selectedTags = detectedTags.toSet() }) {
                            Text("Select All")
                        }
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Tags") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    filteredTagsByCategory.forEach { (category, tags) ->
                        Text(
                            text = category,
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )

                        FlowRow(
                            mainAxisSpacing = 8,
                            crossAxisSpacing = 8,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tags.forEach { tag ->
                                val isSelected = tag in selectedTags
                                TagChip(
                                    tag = tag,
                                    selected = isSelected,
                                    onClick = {
                                        selectedTags = if (isSelected) {
                                            selectedTags - tag
                                        } else {
                                            selectedTags + tag
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onCancel,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { onTagsSelected(selectedTags.toList()) },
                        enabled = selectedTags.isNotEmpty()
                    ) {
                        Text("Apply Selected Tags")
                    }
                }
            }
        }
    }
}