package com.artscry.data.database.entity

import com.artscry.data.database.ArtScryDatabase
import com.artscry.core.domain.model.Tag
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class TagEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TagEntity>(ArtScryDatabase.Tags)

    var name by ArtScryDatabase.Tags.name
    var category by ArtScryDatabase.Tags.category

    fun toTag(): Tag = Tag(
        id = id.value.toString(),
        name = name,
        category = category,
    )
}