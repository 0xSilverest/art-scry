package com.artscry.data.database.entity

import com.artscry.data.database.ArtScryDatabase
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class ImageDbEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ImageDbEntity>(ArtScryDatabase.Images)

    var path by ArtScryDatabase.Images.path
    var name by ArtScryDatabase.Images.name
    var folderName by ArtScryDatabase.Images.folderName
    var type by ArtScryDatabase.Images.type
    var lastAccessed by ArtScryDatabase.Images.lastAccessed
    var favorite by ArtScryDatabase.Images.favorite
    var rating by ArtScryDatabase.Images.rating

    fun toImageEntity(): ImageEntity = ImageEntity(
        id = id.value.toString(),
        path = path,
        name = name,
        folderName = folderName,
        type = type,
        lastAccessed = lastAccessed,
        favorite = favorite,
        rating = rating
    )
}