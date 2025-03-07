package com.artscry.core.domain.model

data class BlackoutConfig(
    val duration: Int = 60,
    val practiceTimerEnabled: Boolean = false,
    val practiceTimerDuration: Int = 300,
    override val randomMode: Boolean = false,
    override val imageLimit: Int? = null
) : com.artscry.core.domain.model.RandomModeConfig {
    fun updateRandomMode(
        newRandomMode: Boolean,
        newImageLimit: Int?
    ) = copy(randomMode = newRandomMode, imageLimit = newImageLimit)

}