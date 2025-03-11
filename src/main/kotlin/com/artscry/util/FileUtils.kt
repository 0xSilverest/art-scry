package com.artscry.util

import com.artscry.core.domain.model.ImageReference
import java.io.File

object FileUtils {
    private val imageExtensions = setOf("png", "jpg", "jpeg", "gif", "bmp")

    fun findImageFiles(folder: File): List<ImageReference> {
        return folder.walkTopDown()
            .filter { file ->
                file.isFile &&
                        file.extension.lowercase() in imageExtensions
            }
            .map { file ->
                ImageReference(
                    path = file.absolutePath,
                    name = file.nameWithoutExtension,
                    type = file.extension
                )
            }
            .toList()
    }

    fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }

    fun formatLastModified(timestamp: Long): String {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
        return formatter.format(java.util.Date(timestamp))
    }
}