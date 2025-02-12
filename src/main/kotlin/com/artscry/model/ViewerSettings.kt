package com.artscry.model


data class ViewerSettings(
    val folderPath: String,
    val mode: PracticeMode,

    val timerEnabled: Boolean = false,
    val timerDuration: Int = 60,

    val blackoutEnabled: Boolean = false,
    val blackoutDuration: Int = 60,

    val randomMode: Boolean = false,
    val hideArrowsInTimer: Boolean = false,
    val imageLimit: Int? = null,
)