package com.artscry.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artscry.util.PreferencesManager
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderSelector(onFolderSelected: (String) -> Unit) {
    var showChooser by remember { mutableStateOf(false) }
    var showRecentMenu by remember { mutableStateOf(false) }
    val recentFolders = remember { mutableStateOf(PreferencesManager.getRecentFolders()) }

    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Button(
                onClick = { showChooser = true }
            ) {
                Text("Select Folder")
            }


            Box {
                IconButton(
                    onClick = { showRecentMenu = true },
                    enabled = recentFolders.value.isNotEmpty()
                ) {
                    Text("↓")
                }

                DropdownMenu(
                    expanded = showRecentMenu,
                    onDismissRequest = { showRecentMenu = false },
                    modifier = Modifier.width(300.dp)
                ) {
                    recentFolders.value.forEach { path ->
                        TooltipArea(
                            tooltip = {
                                Surface(
                                    modifier = Modifier.padding(8.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    elevation = 4.dp
                                ) {
                                    Text(
                                        text = path,
                                        modifier = Modifier.padding(8.dp),
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            delayMillis = 600
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    showRecentMenu = false
                                    onFolderSelected(path)
                                }
                            ) {
                                Text(
                                    text = File(path).name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (recentFolders.value.isNotEmpty()) {
                        Divider()
                        DropdownMenuItem(
                            onClick = {
                                PreferencesManager.clearPreferences()
                                recentFolders.value = emptyList()
                                showRecentMenu = false
                            }
                        ) {
                            Text("Clear History", color = MaterialTheme.colors.error)
                        }
                    }
                }
            }
        }
    }

    if (showChooser) {
        CustomFileChooser(
            initialDirectory = PreferencesManager.getLastFolder(),
            onFolderSelected = { path ->
                PreferencesManager.setLastFolder(path)
                recentFolders.value = PreferencesManager.getRecentFolders()
                onFolderSelected(path)
            },
            onDismiss = { showChooser = false }
        )
    }
}