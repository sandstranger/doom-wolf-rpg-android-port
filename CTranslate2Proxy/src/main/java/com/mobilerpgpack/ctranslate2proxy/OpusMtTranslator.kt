package com.mobilerpgpack.ctranslate2proxy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpusMtTranslator(
    private val pathToTranslationModel: String,
    private val pathToSourceProcessor: String,
    private val pathToTargetProcessor: String
) : Translator() {

    private external fun initializeFromJni(
        pathToTranslationModel: String, pathToSourceProcessor: String,
        pathToTargetProcessor: String
    )

    private external fun translateFromJni( sourceText : String, sentences: List<String>): String

    private external fun releaseFromJni()

    override fun initialize() {
        synchronized(lockObject) {
            initializeFromJni(pathToTranslationModel, pathToSourceProcessor, pathToTargetProcessor)
        }
    }

    override suspend fun translate(
        text: String, sourceLocale: String,
        targetLocale: String
    ): String = withContext(Dispatchers.IO) {
        synchronized(lockObject) {
            if (text.isEmpty()){
                return@withContext text
            }
            return@withContext translateFromJni(text, splitTextIntoSentences(text))
        }
    }

    override fun release() {
        synchronized(lockObject) {
            releaseFromJni()
        }
    }
}