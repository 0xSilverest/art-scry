package com.artscry.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.artscry.core.domain.model.ImageReference
import com.artscry.data.repository.DbRepository
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun TagBasedImageLoader(
    repository: DbRepository,
    onImagesLoaded: (List<ImageReference>) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var allTags by remember { mutableStateOf<List<com.artscry.core.domain.model.Tag>>(emptyList()) }
    var selectedTags by remember { mutableStateOf<List<com.artscry.core.domain.model.Tag>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var matchAllTags by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repository.getAllTags().collect { tags ->
            allTags = tags
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Load Images by Tags",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Match: ")

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = matchAllTags,
                            onClick = { matchAllTags = true }
                        )
                        Text("All selected tags")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = !matchAllTags,
                            onClick = { matchAllTags = false }
                        )
                        Text("Any selected tag")
                    }
                }

                if (selectedTags.isNotEmpty()) {
                    Text(
                        "Selected Tags:",
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    FlowRow(
                        mainAxisSpacing = 8,
                        crossAxisSpacing = 8,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        selectedTags.forEach { tag ->
                            TagChip(
                                tag = tag,
                                selected = true,
                                onRemove = {
                                    selectedTags = selectedTags - tag
                                }
                            )
                        }
                    }
                }

                Text(
                    "Available Tags:",
                    style = MaterialTheme.typography.subtitle2,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    items(allTags.filter { it !in selectedTags }) { tag ->
                        TagChip(
                            tag = tag,
                            onClick = {
                                selectedTags = selectedTags + tag
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (selectedTags.isNotEmpty()) {
                                isLoading = true
                                scope.launch {
                                    val tagIds = selectedTags.map { it.id }

                                    println("Loading images with tags: ${selectedTags.map { it.name }}")

                                    repository.getImagesByTags(tagIds, matchAllTags)
                                        .collect { images ->
                                            val imageRefs = images.map { image ->
                                                ImageReference(
                                                    path = image.path,
                                                    name = image.name,
                                                    type = File(image.path).extension
                                                )
                                            }

                                            val existingImages = imageRefs.filter { File(it.path).exists() }

                                            println("Found ${existingImages.size} existing images")

                                            onImagesLoaded(existingImages)
                                            isLoading = false
                                            onDismiss()
                                        }
                                }
                            }
                        },
                        enabled = selectedTags.isNotEmpty() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Load Images")
                    }
                }
            }
        }
    }

}