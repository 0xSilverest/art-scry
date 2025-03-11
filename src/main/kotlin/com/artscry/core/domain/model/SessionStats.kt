package com.artscry.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SessionStats(
    val id: String = java.util.UUID.randomUUID().toString(),
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val mode: PracticeMode,
    val folderPath: String,
    val imagesViewed: Int = 0,
    val totalImages: Int,
    val settings: SessionSettings,
    val completed: Boolean = false
)
