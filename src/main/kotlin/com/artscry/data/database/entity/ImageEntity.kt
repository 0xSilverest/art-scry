package com.artscry.data.database.entity

import java.util.*

data class ImageEntity(
    val id: String = UUID.randomUUID().toString(),
    val path: String,
    val name: String,
    val folderName: String,
    val type: String,
    val lastAccessed: Long = System.currentTimeMillis(),
    val favorite: Boolean = false,
    val rating: Int = 0
)