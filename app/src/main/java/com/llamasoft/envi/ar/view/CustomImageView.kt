package com.llamasoft.envi.ar.view

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import com.tektonlabs.americancolors.app.ar.models.MaskTapeSeedInfo
import android.util.TypedValue
import android.graphics.DashPathEffect
import android.graphics.drawable.BitmapDrawable
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.ArrayList

class CustomImageView : AppCompatImageView {
    var mImageViewLeft = 0
    var mImageViewTop = 0
    var mMaskTapeEndPoint: List<View> = ArrayList()
    var mMaskTapeStartPoint: List<View> = ArrayList()
    var mMaskingNodeWidth = 0f
    var mPaint = Paint()
    private var reMappedMaskedPointData1: MutableList<MaskTapeSeedInfo> = ArrayList()
    private var reMappedMaskedPointData2: MutableList<MaskTapeSeedInfo> = ArrayList()

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attributeSet: AttributeSet?) : super(
        context!!, attributeSet
    ) {
        init()
    }

    constructor(context: Context?, attributeSet: AttributeSet?, i: Int) : super(
        context!!, attributeSet, i
    ) {
        init()
    }

    private fun init() {
        setWillNotDraw(false)
        mPaint.alpha = 255
        mPaint.strokeWidth = TypedValue.applyDimension(1, 3.0f, resources.displayMetrics)
        mPaint.isAntiAlias = true
        mPaint.color = -1
        mPaint.style = Paint.Style.STROKE
        mPaint.pathEffect = DashPathEffect(floatArrayOf(10.0f, 10.0f, 10.0f, 10.0f), 0.0f)
    }

    private fun remapViewsToPoints(canvas: Canvas?) {
        val list: List<View?> = ArrayList()
        reMappedMaskedPointData1.clear()
        reMappedMaskedPointData2.clear()
        val list2 = mMaskTapeStartPoint
        if (list2.isNotEmpty() && list.isNotEmpty()) {
            var i = 0
            while (i < mMaskTapeStartPoint.size && i < mMaskTapeEndPoint.size) {
                val view = mMaskTapeStartPoint[i]
                val view2 = mMaskTapeEndPoint[i]
                val x = view.x - mImageViewLeft.toFloat() + mMaskingNodeWidth / 2.0f
                val y = view.y - mImageViewTop.toFloat() + mMaskingNodeWidth / 2.0f
                val x2 = view2.x - mImageViewLeft.toFloat() + mMaskingNodeWidth / 2.0f
                val y2 = view2.y - mImageViewTop.toFloat() + mMaskingNodeWidth / 2.0f
                canvas?.drawLine(x, y, x2, y2, mPaint)
                reMappedMaskedPointData1.add(MaskTapeSeedInfo(x, y))
                reMappedMaskedPointData2.add(MaskTapeSeedInfo(x2, y2))
                i++
            }
        }
    }

    fun getReMappedMaskedPointData1(): List<MaskTapeSeedInfo> {
        return reMappedMaskedPointData1
    }

    fun getReMappedMaskedPointData2(): List<MaskTapeSeedInfo> {
        return reMappedMaskedPointData2
    }

    public override fun onDraw(paramCanvas: Canvas) {
        if (drawable is BitmapDrawable) {
            val localBitmap = (drawable as BitmapDrawable).bitmap
            if (localBitmap != null && localBitmap.isRecycled) {
                return
            }
        }
        super.onDraw(paramCanvas)
        remapViewsToPoints(paramCanvas)
    }

    fun updateDrawLine(i: Int, i2: Int, list: List<View>, list2: List<View>, f: Float) {
        mMaskTapeStartPoint = list
        mMaskTapeEndPoint = list2
        mImageViewTop = i2
        mImageViewLeft = i
        mMaskingNodeWidth = f
        remapViewsToPoints(null as Canvas?)
    }
}