package com.tektonlabs.americancolors.app.ar.arutils

import kotlin.jvm.Volatile
import com.tektonlabs.americancolors.app.ar.models.ARParams

object ARObservables {
    @JvmField
    @Volatile
    var currentARParams = ARParams()

}