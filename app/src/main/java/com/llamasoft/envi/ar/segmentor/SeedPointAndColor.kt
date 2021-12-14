package com.llamasoft.envi.ar.segmentor

import org.opencv.core.Mat
import org.opencv.core.CvType
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc

class SeedPointAndColor(seedPoint: Point, rgbColors: List<Int>) : Cloneable {

    var outside: Boolean
    var firstCheck: Boolean

    @JvmField
    var blue: Int = rgbColors[2]
    @JvmField
    var cb: Int
    @JvmField
    var cr: Int
    @JvmField
    var diffY = 0
    @JvmField
    var green: Int = rgbColors[1]
    @JvmField
    var meanY = 0.0
    @JvmField
    var red: Int = rgbColors[0]
    @JvmField
    var tapPoint: Point = seedPoint
    @JvmField
    var validInArea: Boolean
    @JvmField
    var y: Int

    init {
        val mat = Mat(1, 1, CvType.CV_32SC3)
        mat.put(0, 0, rgbColors.toIntArray())
        mat.convertTo(mat, CvType.CV_8UC3)
        val clone = mat.clone()
        Imgproc.cvtColor(mat, clone, 37)
        clone.convertTo(clone, CvType.CV_32SC3)
        y = clone[0, 0][0].toInt()
        cb = clone[0, 0][1].toInt()
        cr = clone[0, 0][2].toInt()
        validInArea = true
        firstCheck = false
        outside = false
    }
}