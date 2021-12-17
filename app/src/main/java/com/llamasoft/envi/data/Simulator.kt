package com.llamasoft.envi.data

import androidx.annotation.DrawableRes
import com.llamasoft.envi.R
import com.llamasoft.envi.util.PaintMenu

data class OptionPaint(
    @DrawableRes var iconDrawableRes: Int,
    var type: PaintMenu,
    var clicked: Boolean,
    val clickable: Boolean = true
)

val paintMenu = arrayListOf(
    OptionPaint(R.drawable.ic_simulator_bucket, PaintMenu.Bucket, clicked = true),
    OptionPaint(R.drawable.ic_simulator_brush, PaintMenu.Brush, clicked = false),
    OptionPaint(R.drawable.ic_simulator_mirror, PaintMenu.Mirror, clicked = false),
// OptionPaint(R.drawable.ic_simulator_eraser, PaintMenu.Eraser, false),
    OptionPaint(R.drawable.ic_undo, PaintMenu.Undo, clicked = false, clickable = false),
    OptionPaint(R.drawable.ic_refresh, PaintMenu.Refresh, clicked = false, clickable = false),
    OptionPaint(R.drawable.ic_pinch, PaintMenu.Pinch, clicked = false),
    OptionPaint(R.drawable.ic_swatch, PaintMenu.Swatch, clicked = false, clickable = false),
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