package com.artscry.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteLocation(
    val path: String,
    val name: String,
    val icon: String = "📁"
)
