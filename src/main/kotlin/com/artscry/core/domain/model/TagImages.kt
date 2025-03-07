package com.artscry.core.domain.model

import com.artscry.data.database.entity.ImageEntity

data class TagWithImages(
    val tag: com.artscry.core.domain.model.Tag,
    val images: List<ImageEntity>
)