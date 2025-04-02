package com.artscry.ui.filechooser

import androidx.compose.runtime.Composable
import com.artscry.util.PreferencesManager

object FileChooserModule {
    @Composable
    fun showFolderChooser(
        initialDirectory: String? = PreferencesManager.getLastFolder(),
        onFolderSelected: (String) -> Unit,
        onDismiss: () -> Unit
    ) {
        CustomFileChooser(
            initialDirectory = initialDirectory,
            onFolderSelected = { path ->
                PreferencesManager.setLastFolder(path)
                onFolderSelected(path)
            },
            onDismiss = onDismiss
        )
    }
}
