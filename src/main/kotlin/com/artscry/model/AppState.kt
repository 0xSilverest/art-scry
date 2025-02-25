package com.artscry.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    var onBlackout by mutableStateOf(false)
    var blackoutTimer: Job? = null
    var blackoutTimeRemaining by mutableStateOf<Int?>(null)

    var blackoutPracticeTimer: Job? = null
    var blackoutPracticeTimeRemaining by mutableStateOf<Int?>(null)

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

            viewerSettings?.let { settings ->
                if (settings.mode == PracticeMode.BLACKOUT_PRACTICE) {
                    isTimerActive = true
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
        blackoutTimeRemaining = duration
        blackoutTimer?.cancel()
        blackoutTimer = scope.launch {
            while (blackoutTimeRemaining!! > 0) {
                delay(1000L)
                blackoutTimeRemaining = blackoutTimeRemaining!! - 1
            }
            isBlackedOut = true

            delay(1000L)
            viewerSettings?.let { settings ->
                if (settings.practiceTimerEnabled) {
                    onBlackout = true
                    startBlackoutPracticeTimer(settings.practiceTimerDuration)
                }
            }
        }
    }

    private fun startBlackoutPracticeTimer(duration: Int) {
        blackoutPracticeTimeRemaining = duration
        blackoutPracticeTimer?.cancel()
        blackoutPracticeTimer = scope.launch {
            while (blackoutPracticeTimeRemaining!! > 0) {
                delay(1000L)
                blackoutPracticeTimeRemaining = blackoutPracticeTimeRemaining!! - 1

                if (blackoutPracticeTimeRemaining == 0) {
                    onNextImage()
                    break
                }
            }
        }
    }

    private fun clearBlackout() {
        isBlackedOut = false
        onBlackout = false
        blackoutTimer?.cancel()
        blackoutTimer = null
        blackoutPracticeTimer?.cancel()
        blackoutPracticeTimer = null
        blackoutPracticeTimeRemaining = null
    }
}