package com.artscry.ui.filechooser.models

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import com.artscry.core.domain.model.FavoriteLocation
import com.artscry.core.domain.model.ViewMode
import com.artscry.util.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class FileChooserState(
    initialDirectory: String? = null,
    private val coroutineScope: CoroutineScope
) {
    val currentPath = mutableStateOf(initialDirectory?.let { Paths.get(it) }
        ?: Paths.get(System.getProperty("user.home")))
    val selectedFolder = mutableStateOf<Path?>(null)

    val searchQuery = mutableStateOf("")
    val viewMode = mutableStateOf(ViewMode.GRID)
    val selectedIndex = mutableStateOf(0)
    val columnsCount = mutableStateOf(4)
    val isSearchFocused = mutableStateOf(false)
    val showAddFavoriteDialog = mutableStateOf(false)

    val recentFolders = mutableStateOf(PreferencesManager.getRecentFolders())
    val favorites = mutableStateOf(loadFavorites())

    private val lastSelectedIndices = mutableMapOf<String, Int>()

    val entries = derivedStateOf {
        currentPath.value.toFile().listFiles()
            ?.filter { file ->
                (file.isDirectory || file.extension.lowercase() in setOf(
                    "jpg", "jpeg", "png", "gif", "bmp"
                )) &&
                        (searchQuery.value.isEmpty() || file.name.contains(searchQuery.value, ignoreCase = true))
            }
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            ?: emptyList()
    }

    fun navigateToFolder(path: Path) {
        lastSelectedIndices[currentPath.value.toString()] = selectedIndex.value

        searchQuery.value = ""
        currentPath.value = path
        selectedFolder.value = null

        selectedIndex.value = lastSelectedIndices[path.toString()] ?: 0
        isSearchFocused.value = false
    }

    fun navigateUp(): Boolean {
        val parentDir = currentPath.value.parent ?: return false

        lastSelectedIndices[currentPath.value.toString()] = selectedIndex.value

        val currentDirName = currentPath.value.fileName.toString()
        val parentEntries = parentDir.toFile().listFiles()
            ?.filter { file ->
                file.isDirectory || file.extension.lowercase() in setOf(
                    "jpg", "jpeg", "png", "gif", "bmp"
                )
            }
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            ?: emptyList()

        val indexInParent = parentEntries.indexOfFirst { it.name == currentDirName }

        currentPath.value = parentDir

        if (indexInParent >= 0) {
            coroutineScope.launch {
                kotlinx.coroutines.delay(50)
                selectedIndex.value = indexInParent
            }
        } else {
            selectedIndex.value = 0
        }

        return true
    }

    fun selectFolder(folder: File? = null): String {
        val pathToUse = folder?.toPath() ?: selectedFolder.value ?: currentPath.value
        return pathToUse.toString()
    }

    fun updateColumnCount(count: Int) {
        columnsCount.value = count
    }

    fun selectItem(index: Int) {
        if (index >= 0 && index < entries.value.size) {
            selectedIndex.value = index

            val entry = entries.value[index]
            if (entry.isDirectory) {
                selectedFolder.value = entry.toPath()
            }
        }
    }

    fun openFolder(index: Int): Boolean {
        if (index >= 0 && index < entries.value.size) {
            val entry = entries.value[index]
            if (entry.isDirectory) {
                navigateToFolder(entry.toPath())
                return true
            }
        }
        return false
    }

    fun addCurrentToFavorites(name: String) {
        val newFavorite = FavoriteLocation(
            path = currentPath.value.toString(),
            name = name
        )
        val updatedFavorites = favorites.value + newFavorite
        favorites.value = updatedFavorites
        PreferencesManager.saveFavorites(updatedFavorites)
    }

    fun removeFavorite(favorite: FavoriteLocation) {
        val updatedFavorites = favorites.value.filter { it.path != favorite.path }
        favorites.value = updatedFavorites
        PreferencesManager.saveFavorites(updatedFavorites)
    }

    fun clearRecentFolders() {
        PreferencesManager.clearRecentFolders()
        recentFolders.value = emptyList()
    }

    fun clearSearch() {
        searchQuery.value = ""
    }

    private fun loadFavorites(): List<FavoriteLocation> {
        return PreferencesManager.getFavorites()
    }
}
