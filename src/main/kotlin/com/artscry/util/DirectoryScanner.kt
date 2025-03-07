package com.artscry.util

import com.artscry.core.domain.model.ImageReference
import com.artscry.core.domain.model.Tag
import com.artscry.data.repository.DbRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import kotlin.system.measureTimeMillis

object DirectoryScanner {
    private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "bmp")

    private val STOP_WORDS = setOf(
        // Common words
        "the", "and", "for", "with", "from", "these", "those", "this", "that",
        "set", "sets", "collection", "collections", "pack", "packs",
        "ref", "refs", "reference", "references",
        // Common folder naming patterns
        "part", "parts", "vol", "volume", "volumes", "chapter", "chapters", "series",
        "section", "sections", "page", "pages", "bundle", "bundles", "art", "fully",
        "images",
        // Qualifiers
        "new", "old", "final", "latest", "high", "low", "res", "resolution",
        // Roman numerals
        "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x",
        "xi", "xii", "xiii", "xiv", "xv"
    )

    private val numericPrefixRegex = Regex("\\d+")
    private val separatorsRegex = Regex("[-_/+\\\\()]")
    private val specialCharsRegex = Regex("[^a-zA-Z0-9\\s]")
    private val multiSpaceRegex = Regex("\\s+")

    private val dbMutex = Mutex()

    suspend fun scanDirectoryRecursively(
        rootDir: String,
        repository: DbRepository,
        progressCallback: (String, Int, Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        println("DS: Starting scan of directory: $rootDir")
        val overallTime = measureTimeMillis {
            try {
                val rootDirectory = File(rootDir)
                if (!rootDirectory.isDirectory) {
                    println("DS: Error: Not a directory: $rootDir")
                    return@withContext
                }

                val allImageFiles = mutableListOf<File>()
                val findTime = measureTimeMillis {
                    findAllImagesRecursively(rootDirectory, allImageFiles)
                }

                val totalImages = allImageFiles.size
                println("DS: Found $totalImages image files in ${findTime}ms")

                if (totalImages == 0) {
                    println("DS: No images found")
                    return@withContext
                }

                val directoryToTags = prepareDirectoryTags(allImageFiles)
                val uniqueTags = directoryToTags.values.flatten().distinct()
                println("DS: Found ${directoryToTags.size} unique directories with ${uniqueTags.size} tags")

                val tagCache = preCreateAllTags(uniqueTags, repository)
                println("DS: Pre-created ${tagCache.size} unique tags")

                val imageEntityCache = createAllImageEntries(allImageFiles, repository)
                println("DS: Created ${imageEntityCache.size} image entries")

                val processedImages = createAllTagLinks(
                    allImageFiles, imageEntityCache, tagCache, directoryToTags,
                    repository, totalImages, progressCallback
                )

                println("DS: Successfully processed $processedImages/$totalImages images")
            } catch (e: Exception) {
                println("DS: Error scanning directory: ${e.message}")
                e.printStackTrace()
            }
        }
        println("DS: Total scan completed in ${overallTime}ms")
    }

    private fun prepareDirectoryTags(imageFiles: List<File>): Map<String, List<String>> {
        val directoryTags = HashMap<String, List<String>>()

        imageFiles.mapNotNull { it.parentFile?.absolutePath }
            .distinct()
            .forEach { dirPath ->
                val dirName = File(dirPath).name
                directoryTags[dirPath] = extractSmartTags(dirName)
            }

        return directoryTags
    }

    private suspend fun preCreateAllTags(
        allTagNames: List<String>,
        repository: DbRepository
    ): Map<String, Tag> {
        val tagCache = HashMap<String, Tag>()

        dbMutex.withLock {
            for (tagName in allTagNames) {
                try {
                    val existingTag = try {
                        repository.getTagByName(tagName)
                    } catch (e: Exception) {
                        null
                    }

                    val tag = existingTag ?: repository.createTag(
                        name = tagName,
                        category = guessCategoryForTag(tagName),
                    )

                    tagCache[tagName] = tag
                } catch (e: Exception) {
                    println("DS: Error creating tag $tagName: ${e.message}")
                }
            }
        }

        return tagCache
    }

    private suspend fun createAllImageEntries(
        imageFiles: List<File>,
        repository: DbRepository
    ): Map<String, String> {
        val imageCache = HashMap<String, String>()

        dbMutex.withLock {
            val batchSize = 100
            val batches = imageFiles.chunked(batchSize)

            for (batch in batches) {
                for (imageFile in batch) {
                    try {
                        val imageRef = ImageReference(
                            path = imageFile.absolutePath,
                            name = imageFile.nameWithoutExtension,
                            type = imageFile.extension
                        )

                        val imageEntity = repository.getImageByPath(imageFile.absolutePath)
                            ?: repository.createImageEntity(imageRef)

                        imageCache[imageFile.absolutePath] = imageEntity.id
                    } catch (e: Exception) {
                        println("DS: Error creating image entry for ${imageFile.absolutePath}: ${e.message}")
                    }
                }
            }
        }

        return imageCache
    }

    private suspend fun createAllTagLinks(
        imageFiles: List<File>,
        imageEntityCache: Map<String, String>,
        tagCache: Map<String, Tag>,
        directoryToTags: Map<String, List<String>>,
        repository: DbRepository,
        totalImages: Int,
        progressCallback: (String, Int, Int) -> Unit
    ): Int {
        val tagLinks = mutableListOf<Pair<String, String>>()

        for (imageFile in imageFiles) {
            val imagePath = imageFile.absolutePath
            val imageId = imageEntityCache[imagePath] ?: continue

            val parentDirPath = imageFile.parentFile?.absolutePath ?: continue
            val tagNames = directoryToTags[parentDirPath] ?: continue

            for (tagName in tagNames) {
                val tag = tagCache[tagName] ?: continue
                tagLinks.add(imageId to tag.id)
            }
        }

        val distinctLinks = tagLinks.distinct()
        val batchSize = 100
        val batches = distinctLinks.chunked(batchSize)

        for ((batchIndex, batch) in batches.withIndex()) {
            withContext(Dispatchers.IO) {
                dbMutex.withLock {
                    for ((imageId, tagId) in batch) {
                        try {
                            repository.addTagToImage(imageId, tagId)
                        } catch (e: Exception) {
                            // Probably already linked - ignore
                        }
                    }
                }
            }

            val currentProcessed = minOf((batchIndex + 1) * batchSize, distinctLinks.size)
            val progress = (currentProcessed.toFloat() / distinctLinks.size) * totalImages
            progressCallback(
                "Creating tag links: $currentProcessed/${distinctLinks.size}",
                progress.toInt(),
                totalImages
            )
        }

        return imageFiles.size
    }

    private fun findAllImagesRecursively(directory: File, results: MutableList<File>) {
        val files = directory.listFiles() ?: return

        for (file in files) {
            if (file.isFile && file.extension.lowercase() in IMAGE_EXTENSIONS) {
                results.add(file)
            } else if (file.isDirectory) {
                findAllImagesRecursively(file, results)
            }
        }
    }

    private fun extractSmartTags(dirName: String): List<String> {
        val cleanName = dirName
            .replace(numericPrefixRegex, "")
            .replace(separatorsRegex, " ")
            .replace(specialCharsRegex, "")
            .lowercase()
            .replace(multiSpaceRegex, " ")
            .trim()

        return cleanName
            .split(" ")
            .filter { word ->
                word.isNotBlank() &&
                        word.lowercase() !in STOP_WORDS &&
                        !word.all { it.isDigit() } &&
                        (word.length >= 3 || word in setOf("3d", "2d", "ai"))
            }
            .map { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }

    private fun guessCategoryForTag(tagName: String): String? {
        val lowerName = tagName.lowercase()

        return when (lowerName) {
            in setOf("male", "female", "woman", "man", "girl", "boy", "lady", "guy") -> "gender"
            in setOf("pose", "poses", "standing", "sitting", "laying", "action") -> "pose"
            in setOf("portrait", "figure", "anatomy", "body", "face", "head") -> "type"
            in setOf("hands", "feet", "legs", "arms", "torso", "chest", "back") -> "body_part"
            in setOf("fantasy", "realistic", "cartoon", "anime", "comic", "stylized") -> "style"
            in setOf("nude", "naked", "clothed", "dressed", "underwear", "lingerie") -> "clothing"
            in setOf("color", "grayscale", "bw", "monochrome", "sepia") -> "coloring"
            else -> null
        }
    }
}