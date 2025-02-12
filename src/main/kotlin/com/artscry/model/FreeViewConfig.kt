package com.artscry.model

data class FreeViewConfig(
    override val randomMode: Boolean = false,
    override val imageLimit: Int? = null
) : RandomModeConfig {
    fun updateRandomMode(
        newRandomMode: Boolean,
        newImageLimit: Int?
    ) = copy(randomMode = newRandomMode, imageLimit = newImageLimit)
}