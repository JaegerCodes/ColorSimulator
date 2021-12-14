package com.llamasoft.envi.ui.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.tektonlabs.americancolors.app.util.onTouch
import okhttp3.internal.toHexString
import kotlin.math.abs


class PickerSV @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bitmapWidth             = 256
    private val bitmapHeight            = 256
    private val circleStrokeWidth       = 5.0f
    private val diameter                = 32f
    private var xc                      = diameter*2f
    private var yc                      = diameter*2f
    private var rect                    = Rect(0, 0, 0, 0)
    private var pixels      : IntArray  = IntArray(1)
    private var circlePaint : Paint     = Paint()
    var circleStrokeColor   : Int       = Color.rgb(0, 0, 0)
    var hue                 : Float     = 0.0f
    var saturation          : Float     = 0.0f
    var value               : Float     = 0.0f
    var callbackSV          : (() -> Unit)? = null
    private val hsv  = floatArrayOf(0f, 0f, 0f)
    private var lastDrawnHue: Float     = -1.0f
    private var svBitmap    : Bitmap    = Bitmap.createBitmap(
        bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888
    )

    init {
        pixels = IntArray(svBitmap.height * svBitmap.width)

        onTouch { _, motionEvent ->
            callbackSV?.let { it() }
            xc = motionEvent.x
                .coerceAtLeast(0.0f)
                .coerceAtMost(width - 1.0f)
            yc = motionEvent.y
                .coerceAtLeast(0.0f)
                .coerceAtMost(height - 1.0f)
            invalidate()
        }
    }

    private fun updateColorHSV(hueValue: Float? = null) {
        saturation  = xc / (width - 1)
        value       = 1 - (yc / (height - 1))
        hue         = hueValue?: hue
        hsv[0]      = hue
        hsv[1]      = saturation
        hsv[2]      = value
        circleStrokeColor = Color.HSVToColor(hsv) xor 0x00ffffff
        invalidate()
    }

    fun hsvInt(hueValue: Float? = null): Int {
        updateColorHSV(hueValue)
        return Color.HSVToColor(hsv)
    }

    fun hsvHex() = "#${hsvInt().toHexString()}"

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (hue != lastDrawnHue) {
            // val prev = System.nanoTime()
            var idx             = 0
            val heightMinus1    = (bitmapHeight - 1).toFloat()
            val widthMinus1     = (bitmapWidth - 1).toFloat()
            val saturationStep  = 1/widthMinus1
            val valueStep       = 255/heightMinus1
            var value           = 255.0f
            val hueSection      = abs((hue / 60).rem(2.0f) - 1)

            for (i in 0 until bitmapHeight) {
                val cStep   = value*saturationStep
                val xmStep  = cStep*hueSection
                var m       = value
                var xm      = value
                val iValue  = value.toInt()
                for (j in 0 until bitmapWidth) {
                    when {
                        hue < 60 -> {
                            pixels[idx++] = Color.rgb(iValue, xm.toInt(), m.toInt())
                        }
                        hue < 120 -> {
                            pixels[idx++] = Color.rgb(xm.toInt(), iValue, m.toInt())
                        }
                        hue < 180 -> {
                            pixels[idx++] = Color.rgb(m.toInt(), iValue, xm.toInt())
                        }
                        hue < 240 -> {
                            pixels[idx++] = Color.rgb(m.toInt(), xm.toInt(), iValue)
                        }
                        hue < 300 -> {
                            pixels[idx++] = Color.rgb(xm.toInt(), m.toInt(), iValue)
                        }
                        else -> {
                            pixels[idx++] = Color.rgb(iValue, m.toInt(), xm.toInt())
                        }
                    }
                    m   -= cStep
                    xm  -= xmStep
                }
                value -= valueStep
            }
            // println("HSV:$hue, ${(System.nanoTime()-prev)}")

            svBitmap.setPixels(
                pixels,
                0,
                svBitmap.width,
                0,
                0,
                svBitmap.width,
                svBitmap.height
            )
            lastDrawnHue = hue
        }

        circlePaint.isAntiAlias = false
        circlePaint.style       = Paint.Style.STROKE
        circlePaint.strokeWidth = circleStrokeWidth
        circlePaint.color       = circleStrokeColor
        rect.set(0,0, width, height)

        canvas?.drawBitmap(svBitmap,null, rect ,null)
        canvas?.drawCircle(xc, yc, diameter, circlePaint)
    }
}