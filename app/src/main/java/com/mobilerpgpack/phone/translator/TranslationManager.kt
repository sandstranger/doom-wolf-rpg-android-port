package com.mobilerpgpack.phone.translator

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.Log
import com.google.mlkit.nl.translate.TranslateLanguage
import com.mobilerpgpack.phone.engine.EngineTypes
import com.mobilerpgpack.phone.translator.models.GoogleTranslateV2
import com.mobilerpgpack.phone.translator.models.ITranslationModel
import com.mobilerpgpack.phone.translator.models.M2M100TranslationModel
import com.mobilerpgpack.phone.translator.models.MLKitTranslationModel
import com.mobilerpgpack.phone.translator.models.OpusMtTranslationModel
import com.mobilerpgpack.phone.translator.models.Small100TranslationModel
import com.mobilerpgpack.phone.translator.models.TranslationType
import com.mobilerpgpack.phone.translator.sql.TranslationDatabase
import com.mobilerpgpack.phone.translator.sql.TranslationEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import java.io.File
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

object TranslationManager {
    const val sourceLocale = TranslateLanguage.ENGLISH
    val targetLocale : String = getSystemLocale()

    private var wasInit = false
    private var _activeEngine: EngineTypes = EngineTypes.DefaultActiveEngine

    private lateinit var db: TranslationDatabase

    @SuppressLint("StaticFieldLeak")
    @Volatile
    private lateinit var translationModel : ITranslationModel

    private val scope = TranslatorApp.globalScope
    private val intervalsTranslator = IntervalMarkerTranslator()
    private val translationModels = HashMap<TranslationType, ITranslationModel>()
    private val loadedTranslations = ConcurrentHashMap<String, TranslationEntry>()
    private val activeTranslations: MutableSet<String> = Collections.newSetFromMap(ConcurrentHashMap())
    private val activeTranslationsAwaitable = ConcurrentHashMap<String, Job>()

    var inGame = false

    var activeEngine: EngineTypes = EngineTypes.DefaultActiveEngine
        set(value) {
            if (_activeEngine == value) {
                return
            }
            _activeEngine = value

            scope.launch {
                reloadSavedTranslations()
            }
        }

    var allowDownloadingOveMobile: Boolean = false
        set(value) {
            translationModels.values.forEach {
                it.allowDownloadingOveMobile = value
            }
        }

    var activeTranslationType : TranslationType
        get() {
            return translationModel.translationType
        }set(value) {
            changeTranslationModel(value)
        }

    fun init( context: Context, activeTranslationType: TranslationType = TranslationType.DefaultTranslationType,
        allowDownloadingOveMobile: Boolean = false
    ) {
        if (wasInit) {
            return
        }

        translationModels [TranslationType.MLKit] =
            MLKitTranslationModel(context,sourceLocale, targetLocale, allowDownloadingOveMobile)

        val filesRootDir = context.getExternalFilesDir("")!!

        val pathToOptModel = "${filesRootDir.absolutePath}${File.separator}opus-ct2-en-ru"
        val optModelSourceProcessor = "${pathToOptModel}${File.separator}source.spm"
        val optModelTargetProcessor = "${pathToOptModel}${File.separator}target.spm"

        translationModels[TranslationType.OpusMt] =
            OpusMtTranslationModel (pathToOptModel, optModelSourceProcessor, optModelTargetProcessor)

        val pathToM2M100Model = "${filesRootDir.absolutePath}${File.separator}m2m100_ct2"
        val m2m100smpFile = "${pathToM2M100Model}${File.separator}sentencepiece.model"

        translationModels[TranslationType.M2M100] =
            M2M100TranslationModel (context, pathToM2M100Model, m2m100smpFile, allowDownloadingOveMobile)

        val pathToSmall100Model = "${filesRootDir.absolutePath}${File.separator}small100_ct2"
        val small100SmpFile = "${pathToSmall100Model}${File.separator}sentencepiece.model"

        translationModels[TranslationType.Small100] =
            Small100TranslationModel (context, pathToSmall100Model, small100SmpFile, allowDownloadingOveMobile)

        translationModels[TranslationType.GoogleTranslate] = GoogleTranslateV2(context)

        translationModel = translationModels[activeTranslationType]!!

        wasInit = true
        db = TranslationDatabase.getInstance(context)

        scope.launch {
            reloadSavedTranslations()
        }
    }

    fun terminate() {
        db.close()
        activeTranslations.clear()
        loadedTranslations.clear()
        translationModels.values.forEach {
            it.release()
        }
        translationModels.clear()
    }

    suspend fun downloadModelIfNeeded(onProgress: (String) -> Unit = { }) {
        if (isTargetLocaleSupported()){
            translationModel.downloadModelIfNeeded(onProgress)
        }
    }

    fun isTranslationSupportedAsFlow(): Flow<Boolean> = flow {
        while (currentCoroutineContext().isActive) {
            emit(isTranslationSupported())
            delay(500)
        }
    }.distinctUntilChanged()

    fun isTargetLocaleSupported () : Boolean = translationModel.isLocaleSupported(targetLocale)

    suspend fun isTranslationSupported() : Boolean =
        isModelDownloaded() && isTargetLocaleSupported() && targetLocale != sourceLocale

    suspend fun isModelDownloaded () = !translationModel.needToDownloadModel()

    fun cancelDownloadModel() = translationModel.cancelDownloadingModel()

    @JvmStatic
    fun getTranslation(text: String) = if (isTranslated(text)) loadedTranslations[text]!!.value else text

    @JvmStatic
    fun isTranslated(text: String) = loadedTranslations.containsKey(text)

    @JvmStatic
    fun translate(text: String): String {
        if (sourceLocale == targetLocale) {
            return text
        }

        if (isTranslated(text)) {
            return getTranslation(text)
        }

        if (activeTranslations.contains(text) || activeTranslationsAwaitable.containsKey(text)) {
            return text
        }

        scope.launch {
            translateAsync(text)
        }
        return text
    }

    fun translate(text: String, onTextTranslated: (String) -> Unit) {
        if (sourceLocale == targetLocale){
            onTextTranslated(text)
            return
        }

        if (isTranslated(text)) {
            onTextTranslated(getTranslation(text))
            return
        }

        if (activeTranslations.contains(text) || activeTranslationsAwaitable.containsKey(text)) {
            return onTextTranslated(text)
            return
        }

        scope.launch {
            onTextTranslated(translateAsync(text))
        }
    }

    suspend fun translateAsync(text: String): String = coroutineScope  {
        if (sourceLocale == targetLocale){
            return@coroutineScope text
        }

        val activeTranslationType = this@TranslationManager.activeTranslationType

        suspend fun saveTranslatedText(translatedText: String) {
            val translationEntry = TranslationEntry(
                key = text,
                lang = targetLocale,
                value = translatedText,
                engine = _activeEngine,
                translationModelType = activeTranslationType
            )
            db.translationDao().insertTranslation(translationEntry)
            loadedTranslations[text] = translationEntry
        }

        if (isTranslated(text)) {
            return@coroutineScope getTranslation(text)
        }

        if (!isTranslationSupported()){
            return@coroutineScope text
        }

        if (inGame) {
            if (activeTranslations.contains(text)) {
                return@coroutineScope text
            }
            activeTranslations.add(text)
        }
        else{
            val job = coroutineContext.job
            val existing = activeTranslationsAwaitable.putIfAbsent(text, job)
            existing?.join()
            if (isTranslated(text)) {
                return@coroutineScope getTranslation(text)
            }
        }

        try {
            val translatedValue = intervalsTranslator.translateWithFixedInterval (text, inGame,_activeEngine) {
                cleanText -> translationModel.translate(cleanText, sourceLocale, targetLocale)
            }
            if (translatedValue!=text && activeTranslationType==this@TranslationManager.activeTranslationType) {
                saveTranslatedText(translatedValue)
                return@coroutineScope translatedValue
            }
            return@coroutineScope text
        }
        catch (ce: CancellationException) {
            throw ce
        }
        catch (_: Exception) {
            return@coroutineScope text
        } finally {
            activeTranslations.remove(text)
            activeTranslationsAwaitable.remove(text)
        }
    }

    private suspend fun loadSavedTranslations() {
        loadedTranslations.clear()
        val entries = db.translationDao().getAllTranslations();

        entries.forEach {
            if (it.lang == targetLocale && it.engine == activeEngine &&
                activeTranslationType == it.translationModelType) {
                loadedTranslations[it.key] = it
            }
        }
    }

    private fun getSystemLocale(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales.get(0).language
        } else {
            Resources.getSystem().configuration.locale.language
        }
    }

    private suspend fun reloadSavedTranslations() {
        return
        activeTranslations.clear()
        activeTranslationsAwaitable.clear()
        loadSavedTranslations()
    }

    private fun changeTranslationModel (targetTranslationType : TranslationType){
        if (activeTranslationType != targetTranslationType) {
            translationModel.release()
            translationModel = translationModels[targetTranslationType]!!
            scope.launch {
                reloadSavedTranslations()
            }
        }
    }
}