package com.artscry.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState

@Composable
fun AddFavoriteDialog(
    currentPath: String,
    onAdd: (name: String, path: String) -> Unit,
    onDismiss: () -> Unit
) {
    var favoriteNameInput by remember { mutableStateOf("") }

    DialogWindow(
        onCloseRequest = onDismiss,
        state = rememberDialogState(
            position = WindowPosition(Alignment.Center),
            width = 500.dp,
            height = 300.dp
        ),
        title = "Add to Favorites",
        resizable = true,
        undecorated = false
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 16.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                ) {
                    Text(
                        "Add to Favorites",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = favoriteNameInput,
                        onValueChange = { favoriteNameInput = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = currentPath,
                        onValueChange = { },
                        label = { Text("Path") },
                        readOnly = true,
                        minLines = 1,
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(72.dp))
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { /* ... */ },
                        enabled = favoriteNameInput.isNotBlank()
                    ) { Text("Add") }
                }
            }
        }
    }
}