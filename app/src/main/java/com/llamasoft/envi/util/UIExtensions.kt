package com.llamasoft.envi.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Environment
import android.text.SpannedString
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.google.android.material.button.MaterialButton
import com.llamasoft.envi.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

fun MaterialButton.coloredButton(buttonWidth: Int? = null, @ColorRes colorId: Int = R.color.teal) {
    setBackgroundColor( ContextCompat.getColor(context, colorId))
    width           = buttonWidth?: ViewGroup.LayoutParams.WRAP_CONTENT
    maxLines        = 1
    cornerRadius    = 14
    strokeWidth     = 4
    strokeColor     = AppCompatResources.getColorStateList(context, R.color.gray_800)
    rippleColor     = AppCompatResources.getColorStateList(context, R.color.white)
}

fun MaterialButton.changeColorButton(
    @ColorRes bgColor: Int = R.color.gray_800, @ColorRes textColor: Int = R.color.white
) {
    width           = width
    maxLines        = maxLines
    cornerRadius    = cornerRadius
    strokeWidth     = strokeWidth
    strokeColor     = AppCompatResources.getColorStateList(context, R.color.gray_800)
    rippleColor     = AppCompatResources.getColorStateList(context, R.color.white)
    setBackgroundColor(ContextCompat.getColor(context, bgColor))
    setTextColor(ContextCompat.getColor(context, textColor))
}

fun MaterialButton.textButton(buttonWidth: Int? = null) {
    setBackgroundColor( ContextCompat.getColor(context, R.color.transparent))
    width           = buttonWidth?: ViewGroup.LayoutParams.WRAP_CONTENT
    maxLines        = 1
    cornerRadius    = 1
    strokeColor     = AppCompatResources.getColorStateList(context, R.color.transparent)
    rippleColor     = AppCompatResources.getColorStateList(context, R.color.gray_400)
}

fun regularTextWithBoldText(regularText: String, boldText: String): SpannedString {
    return buildSpannedString {
        append(regularText)
        append(" ")
        bold { append(boldText) }
    }
}

fun getBitmapFromView(view: View): Bitmap? {
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
}

fun Activity.bitmapToFile(bitmap: Bitmap, fileNameToSave: String): File? {
    var file: File? = null
    val bos = ByteArrayOutputStream()
    return try {
        val path = "${this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}/$fileNameToSave.jpeg"
        file = File(path)
        file.createNewFile()

        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
        val bitmapdata = bos.toByteArray()

        val fos = FileOutputStream(file)
        fos.write(bitmapdata)
        fos.flush()
        fos.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        file
    }
}

val mimeTypes = arrayListOf("image/jpeg", "image/png", "image/jpg")