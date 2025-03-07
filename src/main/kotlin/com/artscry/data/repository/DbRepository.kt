package com.artscry.data.repository

import com.artscry.core.domain.model.*
import com.artscry.data.database.ArtScryDatabase
import com.artscry.data.database.entity.ImageDbEntity
import com.artscry.data.database.entity.ImageEntity
import com.artscry.data.database.entity.TagEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*

class DbRepository {
    suspend fun createTag(name: String, category: String? = null): Tag =
        withContext(Dispatchers.IO) {
            transaction {
                val tagEntity = TagEntity.new {
                    this.name = name
                    this.category = category
                }

                tagEntity.toTag()
            }
        }

    suspend fun getAllTags(): Flow<List<Tag>> = flow {
        val tags = withContext(Dispatchers.IO) {
            transaction {
                TagEntity.all().map { it.toTag() }
            }
        }
        emit(tags)
    }

    suspend fun searchTags(query: String): Flow<List<Tag>> = flow {
        val tags = withContext(Dispatchers.IO) {
            transaction {
                TagEntity.find { ArtScryDatabase.Tags.name like "%$query%" }
                    .map { it.toTag() }
            }
        }
        emit(tags)
    }

    suspend fun deleteTag(tagId: String) = withContext(Dispatchers.IO) {
        transaction {
            TagEntity.findById(UUID.fromString(tagId))?.delete()
        }
    }

    suspend fun createImageEntity(imageRef: ImageReference): ImageEntity = withContext(Dispatchers.IO) {
        transaction {
            val entity = ImageDbEntity.new {
                this.path = imageRef.path
                this.name = imageRef.name
                this.folderName = File(imageRef.path).parentFile?.name ?: ""
                this.type = imageRef.type
                this.lastAccessed = System.currentTimeMillis()
                this.favorite = false
                this.rating = 0
            }

            entity.toImageEntity()
        }
    }

    suspend fun getImageByPath(path: String): ImageEntity? = withContext(Dispatchers.IO) {
        transaction {
            ImageDbEntity.find { ArtScryDatabase.Images.path eq path }
                .firstOrNull()?.toImageEntity()
        }
    }


    suspend fun addTagToImage(imageId: String, tagId: String) = withContext(Dispatchers.IO) {
        transaction {
            val existingRel = ArtScryDatabase.ImageTagCrossRefs.select {
                (ArtScryDatabase.ImageTagCrossRefs.image eq UUID.fromString(imageId)) and
                        (ArtScryDatabase.ImageTagCrossRefs.tag eq UUID.fromString(tagId))
            }.count()

            if (existingRel == 0L) {
                ArtScryDatabase.ImageTagCrossRefs.insert {
                    it[image] = EntityID(UUID.fromString(imageId), ArtScryDatabase.Images)
                    it[tag] = EntityID(UUID.fromString(tagId), ArtScryDatabase.Tags)
                }
            }
        }
    }

    suspend fun removeTagFromImage(imageId: String, tagId: String) = withContext(Dispatchers.IO) {
        transaction {
            ArtScryDatabase.ImageTagCrossRefs.deleteWhere {
                (image eq UUID.fromString(imageId)) and
                        (tag eq UUID.fromString(tagId))
            }
        }
    }

    suspend fun getImageWithTags(imageId: String): Flow<ImageWithTags?> = flow {
        val result = withContext(Dispatchers.IO) {
            transaction {
                val imageEntity = ImageDbEntity.findById(UUID.fromString(imageId))?.toImageEntity()

                if (imageEntity != null) {
                    val tags = (ArtScryDatabase.Tags innerJoin ArtScryDatabase.ImageTagCrossRefs)
                        .select { ArtScryDatabase.ImageTagCrossRefs.image eq UUID.fromString(imageId) }
                        .map {
                            Tag(
                                id = it[ArtScryDatabase.Tags.id].value.toString(),
                                name = it[ArtScryDatabase.Tags.name],
                                category = it[ArtScryDatabase.Tags.category],
                            )
                        }

                    ImageWithTags(imageEntity, tags)
                } else {
                    null
                }
            }
        }

        emit(result)
    }

    fun getImagesByTags(tagIds: List<String>, matchAll: Boolean): Flow<List<ImageEntity>> = flow {
        if (tagIds.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        val images = withContext(Dispatchers.IO) {
            transaction {
                if (matchAll) {
                    val tagUUIDs = tagIds.map { UUID.fromString(it) }

                    val imageIds = ArtScryDatabase.ImageTagCrossRefs
                        .slice(ArtScryDatabase.ImageTagCrossRefs.image)
                        .select { ArtScryDatabase.ImageTagCrossRefs.tag inList tagUUIDs }
                        .groupBy(ArtScryDatabase.ImageTagCrossRefs.image)
                        .having { Count(ArtScryDatabase.ImageTagCrossRefs.tag).eq(tagIds.size.toLong()) }
                        .map { it[ArtScryDatabase.ImageTagCrossRefs.image].value }

                    if (imageIds.isEmpty()) {
                        return@transaction emptyList<ImageEntity>()
                    }

                    ImageDbEntity.find { ArtScryDatabase.Images.id inList imageIds }
                        .map { it.toImageEntity() }
                } else {
                    val tagUUIDs = tagIds.map { UUID.fromString(it) }

                    val imageIds = ArtScryDatabase.ImageTagCrossRefs
                        .slice(ArtScryDatabase.ImageTagCrossRefs.image)
                        .select { ArtScryDatabase.ImageTagCrossRefs.tag inList tagUUIDs }
                        .map { it[ArtScryDatabase.ImageTagCrossRefs.image].value }
                        .distinct()

                    if (imageIds.isEmpty()) {
                        return@transaction emptyList<ImageEntity>()
                    }

                    ImageDbEntity.find { ArtScryDatabase.Images.id inList imageIds }
                        .map { it.toImageEntity() }
                }
            }
        }

        println("Found ${images.size} images matching the selected tags")
        emit(images)
    }

    suspend fun getTagUsageCount(tagId: String): Flow<Int> = flow {
        val count = withContext(Dispatchers.IO) {
            transaction {
                ArtScryDatabase.ImageTagCrossRefs
                    .select { ArtScryDatabase.ImageTagCrossRefs.tag eq UUID.fromString(tagId) }
                    .count().toInt()
            }
        }

        emit(count)
    }

    suspend fun update(tag: Tag) = withContext(Dispatchers.IO) {
        transaction {
            val existingTag = TagEntity.findById(UUID.fromString(tag.id))

            if (existingTag != null) {
                existingTag.name = tag.name
                existingTag.category = tag.category
            } else {
                TagEntity.new(UUID.fromString(tag.id)) {
                    this.name = tag.name
                    this.category = tag.category
                }
            }
        }
    }

    suspend fun getTagByName(name: String): Tag? = withContext(Dispatchers.IO) {
        transaction {
            TagEntity.find { ArtScryDatabase.Tags.name eq name }
                .firstOrNull()?.toTag()
        }
    }
}