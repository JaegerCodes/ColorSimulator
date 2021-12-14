package com.llamasoft.envi.util

import com.llamasoft.envi.data.OptionPaint

interface AppListener<M: Any> {
    fun onPressDeleteButton(model: M) {}
    //, action: (() -> Unit)? = null fun onPressEditButton(model: M) {}
    fun onSelectItemView(model: M) {}
    fun onChangeTab(tabNumber: Int) {}
    fun onPressPrimaryButton(model: M) {}
    fun onPressCancelButton(model: M) {}
}

interface PaintMenuListener {
    fun onPressBucketButton(option: OptionPaint)
    fun onPressBrushButton(option: OptionPaint)
    fun onPressMirrorButton(option: OptionPaint)
    fun onPressUndoButton(option: OptionPaint)
    //fun onPressRedoButton(option: OptionPaint)
    //fun onPressEraserButton(option: OptionPaint)
    fun onPressSwatchButton()
}


