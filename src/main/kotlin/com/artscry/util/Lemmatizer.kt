package com.artscry.util

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.pipeline.Annotation
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import java.util.*

object Lemmatizer {
    private val pipeline = StanfordCoreNLP(
        Properties().apply {
            setProperty("annotators", "tokenize, ssplit, pos, lemma")
            setProperty("coref.algorithm", "neural")
        }
    )

    fun lemmatize(word: String): String {
        val document = Annotation(word)
        pipeline.annotate(document)

        return document.get(CoreAnnotations.SentencesAnnotation::class.java)
            .flatMap { it.get(CoreAnnotations.TokensAnnotation::class.java) }
            .firstOrNull()
            ?.get(CoreAnnotations.LemmaAnnotation::class.java)
            ?: word
    }
}