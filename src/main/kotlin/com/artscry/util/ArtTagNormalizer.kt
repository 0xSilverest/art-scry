package com.artscry.util

import java.util.*

object ArtTagNormalizer {
    private val config by lazy { TagConfigLoader.getConfig() }

    fun normalizeWord(word: String): String {
        if (config.shouldIgnoreWord(word)) {
            return ""
        }

        val lemma = Lemmatizer.lemmatize(word.lowercase())

        val transformed = config.transformTag(lemma)

        if (config.shouldIgnoreWord(transformed)) {
            return ""
        }

        return transformed.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault())
            else it.toString()
        }
    }

    fun getCategoryForTag(tagName: String): String? {
        return config.getCategoryForTag(tagName)
    }
}