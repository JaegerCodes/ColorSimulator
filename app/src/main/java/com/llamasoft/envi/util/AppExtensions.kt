package com.tektonlabs.americancolors.app.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Build
import android.text.SpannedString
import android.util.Patterns
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.llamasoft.envi.BuildConfig
import com.llamasoft.envi.R
import com.llamasoft.envi.databinding.SnackbarMessageBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI


/** VIEW **/

fun<A : Activity> Activity.startNewActivity(activity: Class<A>) {
    Intent(this, activity).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }
}

fun<A : Activity> Fragment.startNewActivity(activityClass: Class<A>) {
    requireActivity().startNewActivity(activityClass)
}


fun View.snackbar(message: String, position: Int? = null, action: (() -> Unit)? = null) {
    val snackbar = Snackbar.make(this, message, 4000)

    position?.let {
        val snacView = snackbar.view
        val params = snacView.layoutParams as CoordinatorLayout.LayoutParams
        params.gravity = position
        snacView.layoutParams = params
    }

    snackbar.setAction("OK!") { action?.invoke() }
    snackbar.show()
}

fun View.materialSnackBar(message: SpannedString, gravity: Int = Gravity.TOP) {
    val snackView       = View.inflate(this.context, R.layout.snackbar_message, null)
    val binding         = SnackbarMessageBinding.bind(snackView)
    val snackbar        = Snackbar.make(this, "", Snackbar.LENGTH_LONG)
    val layoutParams    = CoordinatorLayout.LayoutParams(snackbar.view.layoutParams)

    (snackbar.view as ViewGroup).removeAllViews()
    (snackbar.view as ViewGroup).addView(binding.root)

    layoutParams.gravity        = gravity
    snackbar.view.layoutParams  = layoutParams
    snackbar.view.elevation     = 0f
    snackbar.setBackgroundTint(
        ContextCompat.getColor(this.context, android.R.color.transparent)
    )
    binding.message.text    = message
    binding.btnCancel.setOnClickListener { snackbar.dismiss() }
    snackbar.animationMode  = BaseTransientBottomBar.ANIMATION_MODE_FADE
    snackbar.view.setPadding(0, 0, 0, 0)
    snackbar.show()
}

fun Activity.snackbar(msg: String) {
    Snackbar.make(this.findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT)
        .show()
}

fun Fragment.snackbar(msg: String) {
    activity?.snackbar(msg)
}

fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}
fun LinearProgressIndicator.load(isVisible: Boolean) {
    isIndeterminate = isVisible
}

fun View.enable(enabled: Boolean) {
    isEnabled = enabled
    alpha = if (enabled) 1f else 0.7f
}

fun View.onTouch(touch: (view: View, motionEvent: MotionEvent) -> Unit) {
    setOnTouchListener { v, event ->
        touch(v,event)
        v.performClick()
        true
    }
}
/** Activity - Fragment **/

fun Activity.getRealScreenSize(): Pair<Int, Int> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val size = Point()
        display?.getRealSize(size)
        Pair(size.x, size.y)
    } else {
        val size = Point()
        val displayManager = getSystemService<DisplayManager>()
        displayManager?.getDisplay(Display.DEFAULT_DISPLAY)?.getRealSize(size)
        Pair(size.x, size.y)
    }
}


fun String?.asUri(): Uri? {
    try {
        return Uri.parse(this)
    } catch (e: Exception) {}
    return null
}

fun Context.copyToClipboard(text: String) = ContextCompat
    .getSystemService(this, ClipboardManager::class.java)
    ?.setPrimaryClip(ClipData.newPlainText("", text))

fun Fragment.getTmpFileUri(): Uri = requireActivity().getTmpFileUri()

fun Activity.getTmpFileUri(): Uri {
    val tmpFile = File.createTempFile("tmp_image_file", ".png", this.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
}

fun Fragment.getImageUri(
    imageView: ImageView, originalSize: Boolean = false
): URI = requireActivity().getImageUri(imageView, originalSize)

fun Activity.getImageUri(imageView: ImageView, originalSize: Boolean = false): URI {
    val photo: Bitmap = if (originalSize) {
        imageView.drawable.toBitmap()
    } else {
        imageView.drawable.toBitmap(640,480)
    }

    val tmpFile = File.createTempFile("tmp_image_file", ".png",this.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }

    val fileOutputStream = tmpFile.outputStream()
    val byteArrayOutputStream = ByteArrayOutputStream()
    photo.compress(Bitmap.CompressFormat.PNG,90, byteArrayOutputStream)
    val bytearray = byteArrayOutputStream.toByteArray()
    fileOutputStream.write(bytearray)
    fileOutputStream.flush()
    fileOutputStream.close()
    byteArrayOutputStream.close()
    return tmpFile.toURI()
}

