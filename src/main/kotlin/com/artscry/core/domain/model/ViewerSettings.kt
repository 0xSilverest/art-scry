package com.artscry.core.domain.model

data class ViewerSettings(
    val folderPath: String,
    val mode: PracticeMode,
    val preloadedImages: List<ImageReference>? = null,

    val tags: List<Tag> = emptyList(),

    val timerEnabled: Boolean = false,
    val timerDuration: Int = 60,

    val blackoutEnabled: Boolean = false,
    val blackoutDuration: Int = 60,

    val practiceTimerEnabled: Boolean = false,
    val practiceTimerDuration: Int = 300,

    val randomMode: Boolean = false,
    val hideArrowsInTimer: Boolean = false,
    val imageLimit: Int? = null,
)