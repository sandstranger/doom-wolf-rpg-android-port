package com.mobilerpgpack.phone.translator

import android.util.Log
import com.mobilerpgpack.phone.engine.EngineTypes

class IntervalMarkerTranslator {

    private val specialMarkers = hashSetOf("\n", "|")

    suspend fun translateWithFixedInterval(
        text: String,
        inGame: Boolean,
        engineType : EngineTypes,
        translateFn: suspend (String) -> String
    ): String {
        if (!inGame){
            return translateFn(text)
        }
        var textCopy = text

        val markersIntervals = specialMarkers
            .mapNotNull { marker ->
                val idx = text.indexOf(marker)
                if (idx >= 0) marker to idx else null
            }
            .toMap()

        if (!markersIntervals.isEmpty()) {
            specialMarkers.forEach {
                textCopy = textCopy.replace(it, " ")
            }
        }

        Log.d("TEXT_TO_TRANSLATE", textCopy)

        var translatedText = translateFn(textCopy)

        Log.d("TEXT_TO_TRANSLATE", translatedText)

        if (textCopy == translatedText){
            return text
        }

        val resultBuilder = StringBuilder(translatedText.length * 2 )

        translatedText.forEachIndexed { index, c ->
            resultBuilder.append(c)

            for ((marker, interval) in markersIntervals) {
                if ((index + 1) % interval == 0) {
                    resultBuilder.append(marker)
                }
            }
        }

        return resultBuilder.toString()
    }
}