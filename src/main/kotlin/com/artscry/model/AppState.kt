package com.artscry.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.artscry.ui.components.TimerConfig
import com.artscry.util.FileUtils
import com.artscry.util.ImageCache
import kotlinx.coroutines.*
import java.io.File

class AppState {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val imageCache = ImageCache(scope)

    var isAlwaysOnTop by mutableStateOf(false)

    var images by mutableStateOf<List<ImageReference>>(emptyList())
    var selectedImages by mutableStateOf<List<ImageReference>>(emptyList())
    var imageChangeCounter by mutableStateOf(0)

    var isBlackedOut by mutableStateOf(false)
    var blackoutTimer: Job? = null

    fun loadImages(folder: String, limit: Int? = null) {
        val allImages = FileUtils.findImageFiles(File(folder))

        selectedImages = if (limit != null && limit < allImages.size) {
            allImages.shuffled().take(limit)
        } else {
            allImages
        }

        images = selectedImages

        currentImageIndex = 0
    }

    var currentImageIndex by mutableStateOf(0)

    var timerConfig by mutableStateOf<TimerConfig?>(null)
    var isTimerActive by mutableStateOf(false)

    var viewerSettings by mutableStateOf<ViewerSettings?>(null)

    fun startTimer(config: TimerConfig) {
        timerConfig = config
        isTimerActive = true
    }

    fun stopTimer() {
        isTimerActive = false
    }

    fun onNextImage() {
        if (currentImageIndex < images.size - 1) {
            currentImageIndex++
            imageChangeCounter++
            clearBlackout()

            // If in blackout mode, start timer for new image
            viewerSettings?.let { settings ->
                if (settings.mode == PracticeMode.BLACKOUT_PRACTICE) {
                    startBlackout(settings.blackoutDuration)
                }
            }
        }
    }

    fun onPreviousImage() {
        if (!isTimerActive && currentImageIndex > 0) {
            currentImageIndex--
            imageChangeCounter++
        }
    }

    fun startBlackout(duration: Int) {
        scope.launch {
            delay(duration * 1000L)
            isBlackedOut = true
        }
    }

    private fun clearBlackout() {
        isBlackedOut = false
        blackoutTimer?.cancel()
        blackoutTimer = null
    }

}