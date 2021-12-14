package com.tektonlabs.americancolors.app.ar.models

class ARParams {
    var isApplyHoughExtend = true
    var blurKernel: Int
    var isCannyFlag = false
    var cannyMax: Int
    var cannyMin: Int
    var delayValue = 60
    var edKernel: Int
    var imageExposure = 0f
    var isOTSUFlag = true
    var showPoints = false
    // var imageContrast = 0f
    // var imageHighlights = 0f
    fun showPoints(): Boolean {
        return showPoints
    }

    init {
        delayValue = 40
        cannyMin = 20
        cannyMax = 25
        blurKernel = 3
        edKernel = 11
    }


}