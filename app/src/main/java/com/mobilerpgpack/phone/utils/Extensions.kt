package com.mobilerpgpack.phone.utils

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.obsez.android.lib.filechooser.ChooserDialog
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

val Context.isTelevision get() = this.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)

inline fun <reified T> Context.startActivity(finishParentActivity : Boolean = true) where T : Activity {
    val i = Intent(this, T::class.java)

    if (this is Application) i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    startActivity(Intent(this, T::class.java))

    if (finishParentActivity && this is Activity) this.finish();
}

fun Context.isInternetAvailable(): Boolean {
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    @Suppress("DEPRECATION")
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } else {
        val networkInfo = cm.activeNetworkInfo
        networkInfo != null && networkInfo.isConnected
    }
}

fun Context.isWifiConnected(): Boolean {
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    @Suppress("DEPRECATION")
    val networkInfo = cm.activeNetworkInfo
    @Suppress("DEPRECATION")
    return networkInfo != null &&
            networkInfo.isConnected &&
            networkInfo.type == ConnectivityManager.TYPE_WIFI
}

fun Context.isExternalStoragePermissionGranted () : Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        return Environment.isExternalStorageManager()
    }

    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
}

fun Activity.displayInSafeArea() {
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars()
                    or WindowInsetsCompat.Type.displayCutout()
        )
        v.updatePadding(
            left = bars.left,
            top = bars.top,
            right = bars.right,
            bottom = bars.bottom,
        )

        val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
        if (cutout.top > 0 || cutout.left > 0 || cutout.right > 0) {
            v.setBackgroundColor(Color.BLACK)
        }

        WindowInsetsCompat.CONSUMED
    }
}

fun Context.requestResourceFile (onFileSelected : (String) -> Unit ){
    this.requestResourceFileByAlternateFilePicker ( dirOnly = false, onFileSelected)
}

fun Context.requestDirectory (onDirectorySelected : (String) -> Unit ){
    this.requestResourceFileByAlternateFilePicker ( dirOnly = true, onDirectorySelected)
}

private fun Context.requestResourceFileByAlternateFilePicker ( dirOnly : Boolean, onFileSelected : (String) -> Unit ){
    ChooserDialog(this)
        .withFilter(dirOnly, false, "ipa", "zip")
        .withStartFile(Environment.getExternalStorageDirectory().absolutePath)
        .withChosenListener { path, _ ->
            onFileSelected(path)
        }
        .build()
        .show()
}

private fun buildRequestResourceFileIntent () : Intent {
    return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/zip", "application/octet-stream"))
    }
}