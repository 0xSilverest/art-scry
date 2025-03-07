package com.artscry.core.domain.manager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.artscry.core.domain.model.ImageReference
import com.artscry.core.domain.model.Tag
import com.artscry.data.repository.DbRepository
import com.artscry.util.FileUtils
import com.artscry.util.ImageCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

class ImageManager(
    private val repository: DbRepository,
    private val tagManager: TagManager,
    private val scope: CoroutineScope
) {
    val imageCache = ImageCache(scope)

    var images by mutableStateOf<List<ImageReference>>(emptyList())
    var selectedImages by mutableStateOf<List<ImageReference>>(emptyList())
    var currentImageIndex by mutableStateOf(0)
    var imageChangeCounter by mutableStateOf(0)

    val currentImage: ImageReference?
        get() = images.getOrNull(currentImageIndex)

    fun loadImages(folder: String, limit: Int? = null) {
        val allImages = FileUtils.findImageFiles(File(folder))

        selectedImages = if (limit != null && limit < allImages.size) {
            allImages.shuffled().take(limit)
        } else {
            allImages
        }

        images = selectedImages
        currentImageIndex = 0

        tagManager.loadTagsForImage(currentImage?.path)
    }

    fun loadImagesWithTags(folder: String, tags: List<Tag>, limit: Int? = null) {
        loadImages(folder, limit)

        if (tags.isNotEmpty()) {
            scope.launch {
                for (image in images) {
                    try {
                        val existingImage = repository.getImageByPath(image.path)
                        val imageEntity = existingImage ?: repository.createImageEntity(image)

                        for (tag in tags) {
                            val existingTag = try {
                                repository.getTagByName(tag.name)
                            } catch (e: Exception) {
                                null
                            }

                            val tagId = existingTag?.id ?: repository.createTag(
                                tag.name,
                                tag.category,
                            ).id

                            repository.addTagToImage(imageEntity.id, tagId)
                        }
                    } catch (e: Exception) {
                        println("Error processing image ${image.path}: ${e.message}")
                        e.printStackTrace()
                    }
                }

                tagManager.loadTagsForImage(currentImage?.path)
            }
        }
    }

    fun onNextImage() {
        if (currentImageIndex < images.size - 1) {
            currentImageIndex++
            imageChangeCounter++

            tagManager.loadTagsForImage(currentImage?.path)
        }
    }

    fun onPreviousImage() {
        if (currentImageIndex > 0) {
            currentImageIndex--
            imageChangeCounter++

            tagManager.loadTagsForImage(currentImage?.path)
        }
    }

    fun loadImagesByTags(tags: List<Tag>, matchAll: Boolean = true) {
        scope.launch {
            val tagIds = tags.map { it.id }
            repository.getImagesByTags(tagIds, matchAll).collect { imageEntities ->
                images = imageEntities.map { image ->
                    ImageReference(
                        path = image.path,
                        name = image.name,
                        type = File(image.path).extension
                    )
                }.filter { File(it.path).exists() }

                currentImageIndex = 0

                if (images.isNotEmpty()) {
                    tagManager.loadTagsForImage(currentImage?.path)
                }
            }
        }
    }
}