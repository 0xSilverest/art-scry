package com.artscry.util

import com.artscry.util.models.TagConfig
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

object TagConfigLoader {
    private val DEFAULT_CONFIG_PATH = "config/tag_config.yaml"

    private var instance: TagConfig? = null

    private fun loadConfig(configPath: String = DEFAULT_CONFIG_PATH): TagConfig {
        instance?.let { return it }

        val inputStream: InputStream = try {
            FileInputStream(File(configPath))
        } catch (e: Exception) {
            val classLoader = TagConfigLoader::class.java.classLoader
            classLoader.getResourceAsStream(configPath)
                ?: throw IllegalStateException("Could not find tag configuration at $configPath")
        }

        val yaml = Yaml()
        val rawConfig = inputStream.use { stream ->
            yaml.load(stream) as Map<String, Any>
        }

        val ignoreWords = (rawConfig["ignore_words"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()

        val transformations = (rawConfig["tag_transformations"] as? Map<*, *>)?.entries?.associate {
            (it.key?.toString() ?: "") to (it.value?.toString() ?: "")
        } ?: emptyMap()

        val categoryMappings = (rawConfig["category_mappings"] as? Map<*, *>)?.entries?.associate { entry ->
            val key = entry.key?.toString() ?: ""
            val value = (entry.value as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            key to value
        } ?: emptyMap()

        val config = TagConfig(
            ignoreWords = ignoreWords,
            tagTransformations = transformations,
            categoryMappings = categoryMappings
        )

        instance = config
        return config
    }

    fun getConfig(): TagConfig {
        return instance ?: loadConfig()
    }
}
