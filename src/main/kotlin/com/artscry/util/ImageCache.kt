package com.artscry.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.skia.Image
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class ImageCache(
    private val scope: CoroutineScope,
    private val maxCacheSize: Int = 10
) {
    private val cache = ConcurrentHashMap<String, ImageBitmap>()
    private val loadingJobs = ConcurrentHashMap<String, Job>()
    private val mutex = Mutex()

    private suspend fun preloadImage(path: String) {
        if (!cache.containsKey(path) && !loadingJobs.containsKey(path)) {
            mutex.withLock {
                if (!cache.containsKey(path) && !loadingJobs.containsKey(path)) {
                    loadingJobs[path] = scope.launch(Dispatchers.IO + SupervisorJob()) {
                        try {
                            val bytes = File(path).readBytes()
                            withContext(Dispatchers.Default) {
                                val bitmap = Image.makeFromEncoded(bytes).toComposeImageBitmap()
                                cache[path] = bitmap

                                if (cache.size > maxCacheSize) {
                                    val removeCount = cache.size - maxCacheSize
                                    cache.keys.take(removeCount).forEach { cache.remove(it) }
                                }
                            }
                        } catch (e: Exception) {
                            if (e !is CancellationException) {
                                println("Failed to preload image $path: ${e.message}")
                            }
                        } finally {
                            loadingJobs.remove(path)
                        }
                    }
                }
            }
        }
    }

    suspend fun getImage(path: String): ImageBitmap? = supervisorScope {
        try {
            cache[path]?.let { return@supervisorScope it }

            loadingJobs[path]?.let { job ->
                try {
                    job.join()
                    cache[path]?.let { return@supervisorScope it }
                } catch (e: CancellationException) {
                    // If the job was cancelled, we'll load it ourselves
                }
            }

            withContext(Dispatchers.IO) {
                val bytes = File(path).readBytes()
                val fileSize = bytes.size / 1024  // Size in KB
                withContext(Dispatchers.Default) {
                    val bitmap = Image.makeFromEncoded(bytes).toComposeImageBitmap()

                    println("Image loaded: $path | Dimensions: ${bitmap.width}x${bitmap.height} | File size: $fileSize KB")

                    cache[path] = bitmap
                    bitmap
                }
            }
        } catch (e: Exception) {
            if (e !is CancellationException) {
                println("Failed to load image: ${e.message}")
            }
            null
        }
    }

    fun preloadAround(currentPath: String, paths: List<String>, preloadCount: Int = 2) {
        scope.launch(Dispatchers.Default) {
            try {
                mutex.withLock {
                    loadingJobs.forEach { (path, job) ->
                        val pathIndex = paths.indexOf(path)
                        val currentIndex = paths.indexOf(currentPath)
                        if (pathIndex == -1 ||
                            currentIndex == -1 ||
                            kotlin.math.abs(pathIndex - currentIndex) > preloadCount) {
                            job.cancel()
                            loadingJobs.remove(path)
                        }
                    }
                }

                coroutineScope {
                    (-preloadCount..preloadCount).forEach { offset ->
                        val currentIndex = paths.indexOf(currentPath)
                        if (currentIndex != -1) {
                            paths.getOrNull(currentIndex + offset)?.let { path ->
                                launch(Dispatchers.Default) { preloadImage(path) }
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                // Ignore cancellation
            }
        }
    }

    fun clearCache() {
        scope.launch(Dispatchers.Default) {
            mutex.withLock {
                println("Clearing cache")
                loadingJobs.values.forEach { it.cancel() }
                loadingJobs.clear()
                cache.clear()
            }
        }
    }
}