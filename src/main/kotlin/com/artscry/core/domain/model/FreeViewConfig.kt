package com.artscry.core.domain.model

data class FreeViewConfig(
    override val randomMode: Boolean = false,
    override val imageLimit: Int? = null
) : com.artscry.core.domain.model.RandomModeConfig {
    fun updateRandomMode(
        newRandomMode: Boolean,
        newImageLimit: Int?
    ) = copy(randomMode = newRandomMode, imageLimit = newImageLimit)
}