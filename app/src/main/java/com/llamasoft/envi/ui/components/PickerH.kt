package com.llamasoft.envi.ui.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.tektonlabs.americancolors.app.util.onTouch

class PickerH @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var callbackH: ((hue: Float) -> Unit)? = null

    private val hsv                     = FloatArray(3)
    private val bitmapWidth             = 360
    private val bitmapHeight            = 1
    private var rect                    = Rect(0, 0, 0, 0)
    private var hue         : Float     = 0.0f
    private var rectPaint   : Paint     = Paint()
    private var pixels      : IntArray  = IntArray(1)
    private var myBitmap    : Bitmap    = Bitmap.createBitmap(
        bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888
    )

    init {
        pixels = IntArray(myBitmap.height * myBitmap.width)
        onTouch { _, motionEvent ->
            callbackH?.let { it(hue) }
            val x = motionEvent.x
                .coerceAtLeast(0.0f)
                .coerceAtMost(width - 1.0f)
            hue = x/(width - 1)*359
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        myBitmap.getPixels(
            pixels,
            0,
            myBitmap.width,
            0,
            0,
            myBitmap.width,
            myBitmap.height
        )

        for (j in 0 until bitmapWidth) {
            hsv[0] = j.toFloat()
            hsv[1] = 1.0f
            hsv[2] = 1.0f
            val color = Color.HSVToColor(hsv)
            pixels[j] = color
        }

        myBitmap.setPixels(
            pixels,
            0,
            myBitmap.width,
            0,
            0,
            myBitmap.width,
            myBitmap.height
        )

        val rectangleWidth  = 20.0f
        val strokeWidth     = 5.0f

        rectPaint.style         = Paint.Style.STROKE
        rectPaint.strokeWidth   = strokeWidth
        rectPaint.color         = Color.rgb(0, 0, 0)
        rect.set(0,0, width, height)
        canvas?.drawBitmap(myBitmap,null, rect,null)

        val offset = hue/359*width

        canvas?.drawRect(
            offset - rectangleWidth/2.0f,
            strokeWidth/2.0f,
            offset + rectangleWidth/2.0f,
            height.toFloat() - strokeWidth/2.0f,
            rectPaint
        )
    }
}