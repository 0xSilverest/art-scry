package com.artscry.model

data class TimedSketchingConfig(
    val duration: Int = 60,
    val hideArrows: Boolean = false,
    override val randomMode: Boolean = false,
    override val imageLimit: Int? = null
) : RandomModeConfig {
    fun updateRandomMode(
        newRandomMode: Boolean,
        newImageLimit: Int?
    ) = copy(randomMode = newRandomMode, imageLimit = newImageLimit)

}

