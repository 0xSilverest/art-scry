package com.artscry.util

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.prefs.Preferences

object PreferencesManager {
    private const val LAST_FOLDER_KEY = "last_folder"
    private const val RECENT_FOLDERS_KEY = "recent_folders"
    private const val MAX_RECENT_FOLDERS = 5

    private val prefs = Preferences.userNodeForPackage(PreferencesManager::class.java)
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    fun getLastFolder(): String? {
        return prefs.get(LAST_FOLDER_KEY, null)
    }

    fun setLastFolder(path: String) {
        prefs.put(LAST_FOLDER_KEY, path)
        addToRecentFolders(path)
    }

    fun getRecentFolders(): List<String> {
        val foldersJson = prefs.get(RECENT_FOLDERS_KEY, "[]")
        return try {
            json.decodeFromString(foldersJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun addToRecentFolders(path: String) {
        val current = getRecentFolders().toMutableList()

        current.remove(path)

        current.add(0, path)

        val updated = current.take(MAX_RECENT_FOLDERS)

        prefs.put(RECENT_FOLDERS_KEY, json.encodeToString(updated))
    }

    fun clearPreferences() {
        prefs.clear()
    }
}