package com.artscry.core.domain.model

import java.util.UUID

data class Tag(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String? = null,
    val color: String? = null
)