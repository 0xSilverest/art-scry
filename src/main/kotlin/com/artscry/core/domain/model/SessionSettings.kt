package com.artscry.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SessionSettings(
    val timerDuration: Int? = null,
    val blackoutDuration: Int? = null,
    val practiceTimerDuration: Int? = null,
    val randomMode: Boolean = false,
    val imageLimit: Int? = null
)
