package com.mobilerpgpack.ctranslate2proxy

abstract class Translator {

    protected val lockObject = Any()
    
    abstract fun initialize()

    abstract suspend fun translate (text: String, sourceLocale: String, targetLocale : String) : String

    abstract fun release()

    private companion object{
        init {
            System.loadLibrary("icudata")
            System.loadLibrary("icuuc")
            System.loadLibrary("icui18n")
            System.loadLibrary("OpenNMTTokenizer")
            System.loadLibrary("omp")
            System.loadLibrary("spdlogd")
            System.loadLibrary("ctranslate2")
            System.loadLibrary("sentencepiece_train")
            System.loadLibrary("sentencepiece")
            System.loadLibrary("CTranslate2Proxy")
        }
    }
}