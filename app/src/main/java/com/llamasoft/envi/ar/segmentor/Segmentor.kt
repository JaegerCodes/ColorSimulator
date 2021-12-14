package com.llamasoft.envi.ar.segmentor

import android.graphics.Bitmap
import androidx.annotation.NonNull

interface Segmentor {
    fun invalidateCache()
    fun predictAndColorMultiTapSingleMask(@NonNull bitmap: Bitmap?, seeds: List<SeedPointAndColor>): Bitmap?
    fun predictAndColorMultiTapSingleMask(
        @NonNull bitmap: Bitmap?,
        pointAndColorSeeds: List<SeedPointAndColor>,
        endPointsLines: List<LineEndPoints>
    ): Bitmap?
    fun useBrush(bitmap: Bitmap, seed: SeedPointAndColor, brushSize: Int = 10): Bitmap
    fun setIsDarkWallShiftFlag(darkWallShiftEnabled: Boolean)
    fun setIsLightWallShiftFlag(lightWallShiftEnabled: Boolean)
}