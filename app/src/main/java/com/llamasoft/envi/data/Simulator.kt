package com.llamasoft.envi.data

import androidx.annotation.DrawableRes
import com.llamasoft.envi.R
import com.llamasoft.envi.util.PaintMenu

data class OptionPaint(
    @DrawableRes var iconDrawableRes: Int,
    var type: PaintMenu,
    var clicked: Boolean
)

val paintMenu = arrayListOf(
    OptionPaint(R.drawable.ic_simulator_bucket, PaintMenu.Bucket, true),
    OptionPaint(R.drawable.ic_simulator_brush, PaintMenu.Brush, false),
    OptionPaint(R.drawable.ic_simulator_mirror, PaintMenu.Mirror, false),
    // OptionPaint(R.drawable.ic_simulator_eraser, PaintMenu.Eraser, false),
    OptionPaint(R.drawable.ic_undo, PaintMenu.Undo, false),
    // OptionPaint(R.drawable.ic_redo, PaintMenu.Redo, false),
    OptionPaint(R.drawable.ic_swatch, PaintMenu.Swatch, false),
)

data class SavedColors(
    var saved: Boolean  = false,
    var title: String   = "",
    var code: String    = "",
    var hex: String    = "#ffffff",
)

val emptyColors = arrayListOf(
    SavedColors(saved = true, hex = "#FFBB86FC"),
    SavedColors(saved = true, hex = "#FF6200EE"),
    SavedColors(saved = true, hex = "#FF3700B3"),
    SavedColors(saved = true, hex = "#FF03DAC5"),
    SavedColors(saved = true, hex = "#FF018786"),
    SavedColors(saved = true, hex = "#FF000000")
)