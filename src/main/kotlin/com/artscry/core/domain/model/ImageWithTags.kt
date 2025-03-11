package com.artscry.core.domain.model

import com.artscry.data.database.entity.ImageEntity

data class ImageWithTags(
    val image: ImageEntity,
    val tags: List<Tag>
)
