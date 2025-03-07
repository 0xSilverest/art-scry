package com.artscry.data.database

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.sqlite.SQLiteDataSource
import java.io.File


object ArtScryDatabase {
    object Tags : UUIDTable("tags") {
        val name = varchar("name", 255)
        val category = varchar("category", 100).nullable()
    }

    object Images : UUIDTable("images") {
        val path = varchar("path", 1024).uniqueIndex()
        val name = varchar("name", 255)
        val folderName = varchar("folder_name", 255)
        val type = varchar("type", 50)
        val lastAccessed = long("last_accessed")
        val favorite = bool("favorite").default(false)
        val rating = integer("rating").default(0)
    }

    object ImageTagCrossRefs : Table("image_tag_cross_refs") {
        val image = reference("image_id", Images)
        val tag = reference("tag_id", Tags)

        override val primaryKey = PrimaryKey(image, tag)
    }

    fun initialize() {
        val dbDir = File(System.getProperty("user.home") + File.separator + ".artscry")
        if (!dbDir.exists()) {
            dbDir.mkdirs()
        }

        val dbPath = dbDir.absolutePath + File.separator + "artscry_database.db"

        val dataSource = SQLiteDataSource()
        dataSource.url = "jdbc:sqlite:$dbPath"

        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(Tags, Images, ImageTagCrossRefs)
        }
    }
}