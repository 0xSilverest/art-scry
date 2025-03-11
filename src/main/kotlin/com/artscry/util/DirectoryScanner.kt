package com.artscry.util

import com.artscry.core.domain.model.ImageReference
import com.artscry.core.domain.model.Tag
import com.artscry.data.repository.DbRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.system.measureTimeMillis

object DirectoryScanner {
    private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "bmp")

    private val numericPrefixRegex = Regex("\\d+")
    private val separatorsRegex = Regex("[-_/+\\\\()]")
    private val specialCharsRegex = Regex("[^a-zA-Z0-9\\s]")
    private val multiSpaceRegex = Regex("\\s+")

    private val dbMutex = Mutex()

    private val config by lazy { TagConfigLoader.getConfig() }

    suspend fun scanDirectoryRecursively(
        rootDir: String,
        repository: DbRepository,
        progressCallback: (String, Int, Int) -> Unit,
        basePathToIgnore: String? = null,
        selectedTags: List<Tag>? = null
    )  = withContext(Dispatchers.IO) {
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

                val imageToTagsMap = prepareHierarchicalTags(rootDirectory, allImageFiles, basePathToIgnore)

                val filteredImageToTagsMap = if (selectedTags != null) {
                    val selectedTagNames = selectedTags.map { it.name }.toSet()
                    println("DS: Filtering to ${selectedTagNames.size} selected tags")

                    imageToTagsMap.mapValues { (_, tags) ->
                        tags.filter { it in selectedTagNames }
                    }
                } else {
                    imageToTagsMap
                }

                val allTagNames = filteredImageToTagsMap.values.flatten().distinct()
                println("DS: Generated ${allTagNames.size} unique hierarchical tags")

                val tagCache = preCreateAllTags(allTagNames, repository)
                println("DS: Pre-created ${tagCache.size} unique tags")

                val imageEntityCache = createAllImageEntries(allImageFiles, repository)
                println("DS: Created ${imageEntityCache.size} image entries")

                val processedImages = createAllTagLinks(
                    allImageFiles, imageEntityCache, tagCache, filteredImageToTagsMap,
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

    private fun prepareHierarchicalTags(rootDir: File, imageFiles: List<File>, basePathToIgnore: String? = null): Map<String, List<String>> {
        val imageToTagsMap = HashMap<String, List<String>>()
        val rootPath = rootDir.absolutePath

        val basePath = basePathToIgnore?.let { File(it).absolutePath }
        val useBasePath = basePath != null

        println("DS: Using base path filter: $basePath")

        for (imageFile in imageFiles) {
            val tagsList = mutableListOf<String>()

            var currentDir = imageFile.parentFile
            var reachedBasePath = false

            while (currentDir != null && currentDir.absolutePath.startsWith(rootPath)) {
                if (useBasePath && !reachedBasePath) {
                    if (currentDir.absolutePath.equals(basePath, ignoreCase = true) ||
                        currentDir.absolutePath.startsWith("$basePath${File.separator}")) {
                        reachedBasePath = true
                    }
                }

                if (!useBasePath || reachedBasePath) {
                    val dirTags = extractSmartTags(currentDir.name)
                    tagsList.addAll(dirTags)
                }

                currentDir = currentDir.parentFile
            }

            val uniqueTags = tagsList.distinct()
            imageToTagsMap[imageFile.absolutePath] = uniqueTags
        }

        imageToTagsMap.values.flatten().distinct().forEach { tag -> println("DS: Found tag: $tag") }

        return imageToTagsMap
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
                        category = ArtTagNormalizer.getCategoryForTag(tagName) ?: guessCategoryForTag(tagName),
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
        imageToTagsMap: Map<String, List<String>>,
        repository: DbRepository,
        totalImages: Int,
        progressCallback: (String, Int, Int) -> Unit
    ): Int {
        val tagLinks = mutableListOf<Pair<String, String>>()

        for (imageFile in imageFiles) {
            val imagePath = imageFile.absolutePath
            val imageId = imageEntityCache[imagePath] ?: continue

            val tagNames = imageToTagsMap[imagePath] ?: continue

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
                        !config.shouldIgnoreWord(word) &&
                        !word.all { it.isDigit() } &&
                        (word.length >= 3 || word in setOf("3d", "2d", "ai"))
            }
            .mapNotNull { word ->
                val normalized = ArtTagNormalizer.normalizeWord(word)
                if (normalized.isBlank()) null else normalized
            }
    }

    suspend fun detectTagsInDirectory(
        rootDir: String,
        basePathToIgnore: String? = null,
        progressCallback: (String, Int, Int) -> Unit
    ): List<Tag> = withContext(Dispatchers.IO) {
        println("DS: Analyzing directory for tags: $rootDir")
        try {
            val rootDirectory = File(rootDir)
            if (!rootDirectory.isDirectory) {
                println("DS: Error: Not a directory: $rootDir")
                return@withContext emptyList()
            }

            val allImageFiles = mutableListOf<File>()
            findAllImagesRecursively(rootDirectory, allImageFiles)

            val totalImages = allImageFiles.size
            println("DS: Found $totalImages image files")

            if (totalImages == 0) {
                return@withContext emptyList()
            }

            progressCallback("Analyzing directory structure...", 0, 100)

            if (basePathToIgnore != null) {
                println("DS: Using base path filter: $basePathToIgnore")
            }

            val sampleSize = minOf(allImageFiles.size, 1000)
            val sampleFiles = if (allImageFiles.size > sampleSize) {
                allImageFiles.shuffled().take(sampleSize)
            } else {
                allImageFiles
            }

            val imageToTagsMap = prepareHierarchicalTags(rootDirectory, sampleFiles, basePathToIgnore)
            val allTagNames = imageToTagsMap.values.flatten().distinct().sorted()

            progressCallback("Found ${allTagNames.size} unique tags", 50, 100)
            println("DS: Found ${allTagNames.size} unique tags")

            val tags = allTagNames.map { tagName ->
                Tag(
                    name = tagName,
                    category = ArtTagNormalizer.getCategoryForTag(tagName) ?: guessCategoryForTag(tagName)
                )
            }

            progressCallback("Analysis complete", 100, 100)
            return@withContext tags

        } catch (e: Exception) {
            println("DS: Error analyzing directory: ${e.message}")
            e.printStackTrace()
            return@withContext emptyList()
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
            else -> null
        }
    }
}