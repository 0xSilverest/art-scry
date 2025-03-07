package com.artscry.core.domain.manager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.artscry.core.domain.model.Tag
import com.artscry.data.repository.DbRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TagManager(
    private val repository: DbRepository,
    private val scope: CoroutineScope
) {
    var currentImageTags by mutableStateOf<List<Tag>>(emptyList())

    fun loadTagsForImage(imagePath: String?) {
        if (imagePath == null) {
            currentImageTags = emptyList()
            return
        }

        scope.launch {
            try {
                val existingImage = repository.getImageByPath(imagePath)
                val imageId = existingImage?.id

                if (imageId != null) {
                    repository.getImageWithTags(imageId).collect { imageWithTags ->
                        currentImageTags = imageWithTags?.tags ?: emptyList()
                    }
                } else {
                    currentImageTags = emptyList()
                }
            } catch (e: Exception) {
                println("Error loading tags for image: ${e.message}")
                currentImageTags = emptyList()
            }
        }
    }
}
