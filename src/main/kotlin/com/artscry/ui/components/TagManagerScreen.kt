package com.artscry.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.artscry.data.repository.DbRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TagManagerScreen(
    repository: DbRepository,
    modifier: Modifier = Modifier
) {
    var allTags by remember { mutableStateOf<List<com.artscry.core.domain.model.Tag>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var isCreatingTag by remember { mutableStateOf(false) }
    var tagToEdit by remember { mutableStateOf<com.artscry.core.domain.model.Tag?>(null) }
    var tagUsageCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        repository.getAllTags().collect { tags ->
            allTags = tags

            tags.forEach { tag ->
                repository.getTagUsageCount(tag.id).firstOrNull()?.let { count ->
                    tagUsageCounts = tagUsageCounts + (tag.id to count)
                }
            }
        }
    }

    val categories = remember(allTags) {
        allTags.mapNotNull { it.category }.distinct().sorted()
    }

    val filteredTags = remember(allTags, searchQuery, selectedCategory) {
        allTags.filter { tag ->
            (searchQuery.isEmpty() ||
                    tag.name.contains(searchQuery, ignoreCase = true) ||
                    tag.category?.contains(searchQuery, ignoreCase = true) == true) &&
                    (selectedCategory == null || tag.category == selectedCategory)
        }.sortedWith(
            compareBy<com.artscry.core.domain.model.Tag> { it.category }.thenBy { it.name }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            "Tag Manager",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Tags") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { isCreatingTag = true }) {
                Text("Create Tag")
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    content = { Text("All") }
                )
            }

            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = if (selectedCategory == category) null else category },
                    content = { Text(category) }
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredTags.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (searchQuery.isNotEmpty()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No matching tags found")
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { isCreatingTag = true }) {
                                    Text("Create \"$searchQuery\"")
                                }
                            }
                        } else {
                            Text("No tags found")
                        }
                    }
                }
            } else {
                var currentCategory: String? = null

                for (tag in filteredTags) {
                    if (tag.category != currentCategory) {
                        currentCategory = tag.category

                            item {
                                currentCategory?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.subtitle1,
                                        color = MaterialTheme.colors.primary,
                                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                    )
                                }
                            }
                    }

                    item {
                        TagListItem(
                            tag = tag,
                            usageCount = tagUsageCounts[tag.id] ?: 0,
                            onEdit = { tagToEdit = tag },
                            onDelete = {
                                scope.launch {
                                    repository.deleteTag(tag.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (isCreatingTag) {
        CreateTagDialog(
            initialName = searchQuery,
            categories = categories,
            onCreateTag = { name, category ->
                scope.launch {
                    repository.createTag(name, category)
                }
                isCreatingTag = false
            },
            onDismiss = { isCreatingTag = false }
        )
    }

    tagToEdit?.let { tag ->
        EditTagDialog(
            tag = tag,
            categories = categories,
            onSaveTag = { updatedTag ->
                scope.launch {
                    repository.update(updatedTag)
                }
                tagToEdit = null
            },
            onDismiss = { tagToEdit = null }
        )
    }
}