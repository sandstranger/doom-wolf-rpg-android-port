package com.mobilerpgpack.phone.engine

import android.content.Context
import android.os.Environment
import android.os.Process
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.mobilerpgpack.phone.R
import com.mobilerpgpack.phone.engine.activity.EngineActivity
import com.mobilerpgpack.phone.ui.screen.wolfensteinButtons
import com.mobilerpgpack.phone.utils.PreferencesStorage
import com.mobilerpgpack.phone.utils.startActivity
import kotlinx.coroutines.flow.first
import java.io.File

const val logcatFileName = "wolfenstein_doom_rpg_log.log"

internal val enginesInfo : HashMap<EngineTypes, EngineInfo> = hashMapOf(
    EngineTypes.WolfensteinRpg to EngineInfo("libWolfensteinRPG.so", arrayOf("GL","SDL2","openal","WolfensteinRPG"),
        wolfensteinButtons, pathToResourcesCallback = { context -> PreferencesStorage.getPathToWolfensteinRpgIpaFileValue (context) } ),
    EngineTypes.DoomRpg to EngineInfo("libdoomrpg.so", arrayOf(""),
        wolfensteinButtons, pathToResourcesCallback = { context -> PreferencesStorage.getPathToWolfensteinRpgIpaFileValue (context) }),
    EngineTypes.Doom2Rpg to EngineInfo("", arrayOf(""),
        wolfensteinButtons, pathToResourcesCallback = { context -> PreferencesStorage.getPathToWolfensteinRpgIpaFileValue (context) })
)

internal val defaultPathToLogcatFile: String = "${Environment.getExternalStorageDirectory().absolutePath}" +
        "${File.separator}$logcatFileName"

@Suppress("DEPRECATION")
internal fun setFullscreen(decorView: View) {
    val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    decorView.systemUiVisibility = uiOptions
}

fun killEngine() = Process.killProcess(Process.myPid())

suspend fun startEngine(context: Context) {
    val activeEngineType = PreferencesStorage.getActiveEngineValue(context)

    if (enginesInfo[activeEngineType]!!.pathToResourcesCallback(context).first()!!.isEmpty()){
        MaterialDialog(context).show {
            title(R.string.error)
            message(R.string.can_not_start_engine)
            positiveButton(R.string.ok_text)
        }
        return
    }

    context.startActivity<EngineActivity>()
}