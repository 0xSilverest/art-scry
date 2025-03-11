package com.artscry.util.models

data class TagConfig(
    val ignoreWords: List<String>,
    val tagTransformations: Map<String, String>,
    val categoryMappings: Map<String, List<String>>
) {
    fun getCategoryForTag(tagName: String): String? {
        val normalizedTag = tagName.lowercase()

        return categoryMappings.entries.firstOrNull { (_, tags) ->
            tags.any { it.equals(normalizedTag, ignoreCase = true) }
        }?.key
    }

    fun shouldIgnoreWord(word: String): Boolean {
        return ignoreWords.any { it.equals(word, ignoreCase = true) }
    }

    fun transformTag(tag: String): String {
        val lowerTag = tag.lowercase()
        return tagTransformations[lowerTag] ?: tag
    }
}
