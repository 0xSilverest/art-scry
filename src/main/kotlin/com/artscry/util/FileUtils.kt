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
}