package com.tektonlabs.americancolors.app.ar.models

class SeedPointWithColor {
    /* renamed from: b */
    var b = 0
    var colorUid: String? = null

    /* renamed from: g */
    var g = 0

    /* renamed from: r */
    var r = 0
    var xPos: Double
    var yPos: Double

    constructor(d: Double, d2: Double, i: Int, i2: Int, i3: Int) {
        xPos = d
        yPos = d2
        r = i
        g = i2
        b = i3
    }

    constructor(d: Double, d2: Double, str: String?) {
        xPos = d
        yPos = d2
        colorUid = str
    }
}