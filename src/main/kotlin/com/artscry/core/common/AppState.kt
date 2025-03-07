package com.artscry.core.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.artscry.core.domain.manager.BlackoutManager
import com.artscry.core.domain.manager.ImageManager
import com.artscry.core.domain.manager.TagManager
import com.artscry.core.domain.manager.TimerManager
import com.artscry.core.domain.model.PracticeMode
import com.artscry.core.domain.model.Tag
import com.artscry.core.domain.model.TimerConfig
import com.artscry.core.domain.model.ViewerSettings
import com.artscry.data.repository.DbRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppState {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val repository = DbRepository()

    var isAlwaysOnTop by mutableStateOf(false)
    var viewerSettings by mutableStateOf<ViewerSettings?>(null)

    val tagManager = TagManager(repository, scope)

    val blackoutManager = BlackoutManager(scope)

    val timerManager = TimerManager(
        scope = scope
    )

    val imageManager = ImageManager(
        repository = repository,
        tagManager = tagManager,
        scope = scope
    )

    val currentImage get() = imageManager.currentImage
    val currentImageTags get() = tagManager.currentImageTags
    val imageCache get() = imageManager.imageCache

    val isBlackedOut get() = blackoutManager.isBlackedOut
    val onBlackout get() = blackoutManager.onBlackout
    val blackoutTimeRemaining get() = blackoutManager.blackoutTimeRemaining
    val blackoutPracticeTimeRemaining get() = blackoutManager.blackoutPracticeTimeRemaining

    val isTimerActive get() = timerManager.isTimerActive
    val timerConfig get() = timerManager.timerConfig

    val images get() = imageManager.images
    val currentImageIndex get() = imageManager.currentImageIndex
    val imageChangeCounter get() = imageManager.imageChangeCounter

    fun onNextImage() {
        if (currentImageIndex < images.size - 1) {
            blackoutManager.clearBlackout()
            imageManager.onNextImage()

            viewerSettings?.let { settings ->
                if (settings.mode == PracticeMode.BLACKOUT_PRACTICE) {
                    timerManager.isTimerActive = true
                    blackoutManager.startBlackout(settings.blackoutDuration)

                    if (settings.practiceTimerEnabled) {
                        blackoutManager.startBlackoutPracticeTimer(
                            settings.practiceTimerDuration,
                            onComplete = { onNextImage() }
                        )
                    }
                }
            }
        }
    }

    fun onPreviousImage() {
        if (!timerManager.isTimerActive && currentImageIndex > 0) {
            imageManager.onPreviousImage()
        }
    }

    fun loadImages(folder: String, limit: Int? = null) {
        imageManager.loadImages(folder, limit)
    }

    fun loadImagesWithTags(folder: String, tags: List<Tag>, limit: Int? = null) {
        imageManager.loadImagesWithTags(folder, tags, limit)
    }

    fun loadImagesByTags(tags: List<Tag>, matchAll: Boolean = true) {
        imageManager.loadImagesByTags(tags, matchAll)
    }

    fun startTimer(config: TimerConfig) {
        timerManager.startTimer(config)
    }

    fun stopTimer() {
        timerManager.stopTimer()
    }

    fun startBlackout(duration: Int) {
        blackoutManager.startBlackout(duration)

        viewerSettings?.let { settings ->
            if (settings.practiceTimerEnabled) {
                blackoutManager.startBlackoutPracticeTimer(
                    settings.practiceTimerDuration,
                    onComplete = { onNextImage() }
                )
            }
        }
    }
}