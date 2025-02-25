package com.artscry.model

data class TimerConfig(
    val duration: Int,
    val blackoutEnabled: Boolean = false,
    val blackoutDuration: Int? = null,
)