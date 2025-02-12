package com.artscry.model

data class BlackoutConfig(
    val duration: Int = 60,
    val practiceTimerEnabled: Boolean = false,
    val practiceTimerDuration: Int = 300,
    override val randomMode: Boolean = false,
    override val imageLimit: Int? = null
) : RandomModeConfig {
    fun updateRandomMode(
        newRandomMode: Boolean,
        newImageLimit: Int?
    ) = copy(randomMode = newRandomMode, imageLimit = newImageLimit)

}