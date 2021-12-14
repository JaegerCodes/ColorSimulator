package com.llamasoft.envi.ar.segmentor

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.util.*


class SegmentorImpl : Segmentor {
    private var cacheCanny: Mat? = null
    private var cacheImage: Mat? = null
    private var maskMat = Mat()
    private var cannyMat = Mat()
    private var resultMat = Mat()
    private var srcmat = Mat()
    private var hierarchy = Mat()
    private var canvas = Mat()
    private var clone = Mat()
    private var matSectionImage = Mat()
    private var cachePoints: List<SeedPointAndColor>? = null
    private var cacheResult: Bitmap? = null
    private var imageSimilarityThreshold = 3.0f
    private var mResizedImageWidth: Int
    //private var pointSimilarityThreshold = 3.0

    private fun isSimilarImage(mat: Mat): Boolean {
        val mat2 = cacheImage
        if (mat2 == null || mat2.size() != mat.size()) {
            return false
        }
        val mat3 = Mat(mat.size(), CvType.CV_8UC4)
        Core.absdiff(cacheImage, mat, mat3)
        val coreMeanVal = Core.mean(mat3).`val`
        return (coreMeanVal[0] + coreMeanVal[1] + coreMeanVal[2]) / 3.0 <= imageSimilarityThreshold.toDouble()
    }

    private fun isSimilarTapPoints(seedsPointAndColors: List<SeedPointAndColor>?): Boolean {
        if (seedsPointAndColors == null || cachePoints == null || seedsPointAndColors.size != cachePoints!!.size) {
            return false
        }
        return true
    }

    override fun invalidateCache() {
        cacheImage = null
        cachePoints = null
        cacheResult = null
    }

    override fun predictAndColorMultiTapSingleMask(
        bitmap: Bitmap?,
        seeds: List<SeedPointAndColor>
    ): Bitmap? {
        return predictAndColorMultiTapSingleMask(bitmap, seeds, ArrayList())
    }

    override fun setIsDarkWallShiftFlag(darkWallShiftEnabled: Boolean) {}
    override fun setIsLightWallShiftFlag(lightWallShiftEnabled: Boolean) {}
    override fun predictAndColorMultiTapSingleMask(
        bitmap: Bitmap?,
        pointAndColorSeeds: List<SeedPointAndColor>,
        endPointsLines: List<LineEndPoints>
    ): Bitmap? {
        var contours1: List<MatOfPoint>
        var hierarchy1: Mat
        var positionPointsColorsSeed: Int
        var contourIdx: Int
        var currentSrcMat: Mat
        var fillPolyIndicesUpdated: MutableList<Int>
        var contourIdxListUpdated: MutableList<Int?>
        var secondMatOfPoint2fUpdated: MatOfPoint2f?
        var firstMatOfPoint2fUpdated: MatOfPoint2f
        var imageWidthPortion2Updated: Int
        var currentCanvasMat: Mat
        var hierarchy1Mat: Mat
        var maxLevel: Int
        var hierarchy1MatUpdated: Mat
        var lineType: Int
        var contourIdxUpdated: Int
        var contoursUpdated: List<MatOfPoint>
        var srcImageMat: Mat
        var colorScalar: Scalar
        var pointColorSeedIndex: Int
        val pointsColorsSeeds: List<SeedPointAndColor> = ArrayList(pointAndColorSeeds)
        val heightBitmap = bitmap!!.height
        val widthBitmap = bitmap.width
        val resizedImageWidth = mResizedImageWidth
        val newImageWidth = (resizedImageWidth shr 2) + 1 shl 2
        val sectionImage = ((resizedImageWidth.toFloat() / (widthBitmap.toFloat() / heightBitmap.toFloat())).toInt() shr 2) + 1 shl 2
        val newImageWidthCols = (newImageWidth shr 2) + 1 shl 2
        val sectionImageRows = (sectionImage shr 2) + 1 shl 2
        matSectionImage = Mat(sectionImage, newImageWidth, CvType.CV_8UC4)
        val createScaledBitmap = Bitmap.createScaledBitmap(
            bitmap, newImageWidth, sectionImage, true
        ) ?: return bitmap
        Utils.bitmapToMat(createScaledBitmap, matSectionImage)
        clone = matSectionImage.clone()
        maskMat = Mat(sectionImage, newImageWidth, CvType.CV_8UC1)
        Imgproc.cvtColor(clone, maskMat, 11)
        val mat9 = Mat(sectionImageRows, newImageWidthCols, CvType.CV_8UC1)
        Imgproc.resize(maskMat, mat9, Size(newImageWidthCols.toDouble(), sectionImageRows.toDouble()))
        cannyMat = Mat(sectionImageRows, newImageWidthCols, CvType.CV_8UC1)
        if (!isSimilarImage(clone) || !isSimilarTapPoints(pointsColorsSeeds)) {
            val imageMat = cacheImage
            imageMat?.release()
            cacheImage = clone.clone()
            Imgproc.medianBlur(mat9, mat9, 3)
            Imgproc.Canny(mat9, cannyMat, 20.0, 25.0, 3, true)
            Core.bitwise_not(cannyMat, cannyMat)
            val structuringElement = Imgproc.getStructuringElement(2, Size(11.0, 11.0))
            Imgproc.erode(cannyMat, cannyMat, structuringElement)
            Imgproc.dilate(cannyMat, cannyMat, structuringElement)
            Imgproc.erode(cannyMat, cannyMat, structuringElement)
            Imgproc.resize(cannyMat, cannyMat, Size(newImageWidth.toDouble(), sectionImage.toDouble()))
            val mat12 = cacheCanny
            mat12?.release()
            cacheCanny = cannyMat.clone()
            cachePoints = pointsColorsSeeds
        } else {
            cannyMat = cacheCanny!!.clone()
        }

        val newImageWidthDividedByBitmapWidth = newImageWidth.toFloat() / widthBitmap.toFloat()

        val sectionImageDividedByBitmapHeight = sectionImage.toFloat() / heightBitmap.toFloat()
        val points: MutableList<Point> = ArrayList()
        var pointColorCounter = 0
        while (true) {
            var tapPointYUpdated = 0.0f
            if (pointColorCounter >= pointsColorsSeeds.size) {
                break
            }
            val tapPoint = pointsColorsSeeds[pointColorCounter].tapPoint
            var tapPointX = tapPoint.x.toFloat()
            val tapPointY = tapPoint.y.toFloat()
            if (tapPointX < 0.0f) {
                tapPointX = 0.0f
            }
            val tapPointXWidth = (widthBitmap - 1).toFloat()
            if (tapPointX > tapPointXWidth) {
                tapPointX = tapPointXWidth
            }
            if (tapPointY >= 0.0f) {
                tapPointYUpdated = tapPointY
            }
            val tapPointYHeight = (heightBitmap - 1).toFloat()
            if (tapPointYUpdated > tapPointYHeight) {
                tapPointYUpdated = tapPointYHeight
            }
            points.add(Point((tapPointX * newImageWidthDividedByBitmapWidth).toDouble(), (tapPointYUpdated * sectionImageDividedByBitmapHeight).toDouble()))
            pointColorCounter++
        }
        canvas = matSectionImage
        val boxedLineEndpoints: MutableList<LineEndPoints> = ArrayList()
        var endPointsLinesCounter = 0

        while (endPointsLinesCounter < endPointsLines.size) {
            boxedLineEndpoints.add(
                LineEndPoints()
            )
            endPointsLinesCounter++
        }
        if (boxedLineEndpoints.size > 0) {
            maskMat.setTo(Scalar(0.0))
            for (boxedLineEndPoint in boxedLineEndpoints.indices) {
                val lineEndPointsUnit = boxedLineEndpoints[boxedLineEndPoint]
                Imgproc.line(
                    maskMat,
                    lineEndPointsUnit.startPoint,
                    lineEndPointsUnit.endPoint,
                    Scalar(255.0),
                    2
                )
            }
            Core.bitwise_not(maskMat, maskMat)
            Core.bitwise_and(cannyMat, maskMat, cannyMat)
        }
        var contours: List<MatOfPoint> = ArrayList()
        hierarchy = Mat()
        Imgproc.findContours(cannyMat, contours, hierarchy, 3, 2)
        val imageWidthPortion1 = (newImageWidth.toDouble() * 0.05).toInt()
        var imageWidthPortion2 = (newImageWidth.toDouble() * 0.05).toInt()
        var firstMatOfPoint2f = MatOfPoint2f()
        var secondMatOfPoint2f: MatOfPoint2f? = MatOfPoint2f()
        maskMat.setTo(Scalar(0.0))
        srcmat = Mat(sectionImage, newImageWidth, CvType.CV_16SC4, Scalar(0.0, 0.0, 0.0, 0.0))
        var contourIdxList: MutableList<Int?> = ArrayList()
        var fillPolyIndicesList: MutableList<Int> = ArrayList()
        var pointsColorsSeedsCounter = 0
        while (pointsColorsSeedsCounter < pointsColorsSeeds.size) {
            fillPolyIndicesList.add(-1)
            val seedPointAndColor = pointsColorsSeeds[pointsColorsSeedsCounter]
            val pointColorSeed = points[pointsColorsSeedsCounter]
            var currentContour = 0
            var contoursCounter = 0
            var currentContourIdx = -1
            while (true) {
                if (currentContour < 0) {
                    contours1 = contours
                    hierarchy1 = hierarchy
                    positionPointsColorsSeed = pointsColorsSeedsCounter
                    contourIdx = currentContourIdx
                    currentSrcMat = srcmat
                    break
                }
                currentSrcMat = srcmat
                if (contoursCounter >= contours.size) {
                    contours1 = contours
                    hierarchy1 = hierarchy
                    positionPointsColorsSeed = pointsColorsSeedsCounter
                    contourIdx = currentContourIdx
                    break
                }

                val nextContourPosition = contoursCounter + 1
                val contourMat: Mat = contours[currentContour]
                val boundingRect3 = Imgproc.boundingRect(contourMat)
                val hierarchy0 = hierarchy[0, currentContour]
                val nextContour = hierarchy0[0].toInt()
                if (boundingRect3.width >= imageWidthPortion1 && boundingRect3.height >= imageWidthPortion2) {
                    contourMat.convertTo(secondMatOfPoint2f, CvType.CV_32FC2)
                    Imgproc.approxPolyDP(secondMatOfPoint2f, firstMatOfPoint2f, Imgproc.arcLength(secondMatOfPoint2f, true) * 0.005, true)
                    val matOfPoint = MatOfPoint()
                    firstMatOfPoint2f.convertTo(matOfPoint, CvType.CV_32SC2)
                    val boundingRect4 = Imgproc.boundingRect(matOfPoint)
                    if (boundingRect4.width >= imageWidthPortion1 && boundingRect4.height >= imageWidthPortion2) {
                        pointColorSeedIndex = pointsColorsSeedsCounter
                        if (Imgproc.pointPolygonTest(secondMatOfPoint2f, pointColorSeed, true) > 12.0) {
                            fillPolyIndicesList[pointColorSeedIndex] = currentContour
                            currentContourIdx = currentContour
                        }
                        pointsColorsSeedsCounter = pointColorSeedIndex
                    }
                }
                contoursCounter = nextContourPosition
                currentContour = nextContour
                srcmat = currentSrcMat
            }
            contourIdxList.add(contourIdx)
            if (contourIdx >= 0) {
                val red = seedPointAndColor.red.toFloat()
                val green = seedPointAndColor.green.toFloat()
                val blue = seedPointAndColor.blue.toFloat()
                hierarchy1Mat = hierarchy1
                val hierarchy1MatField0 = hierarchy1Mat[0, contourIdx]
                imageWidthPortion2Updated = imageWidthPortion2
                firstMatOfPoint2fUpdated = firstMatOfPoint2f
                val hierarchy1MatField2 = hierarchy1MatField0[2].toInt()
                secondMatOfPoint2fUpdated = secondMatOfPoint2f

                maskMat.setTo(Scalar(0.0))
                Imgproc.drawContours(maskMat, contours1, contourIdx, Scalar(255.0), -1, Imgproc.LINE_8, hierarchy1Mat, 1)

                contourIdxListUpdated = contourIdxList
                fillPolyIndicesUpdated = fillPolyIndicesList

                currentCanvasMat = canvas
                val maskCoreMean = Core.mean(currentCanvasMat, maskMat)
                val maskCoreMeanValue = maskCoreMean.`val`
                val redScalar = maskCoreMeanValue[0]
                val greenScalar = maskCoreMeanValue[1]
                val blueScalar = maskCoreMeanValue[2]

                val redValue = red.toDouble() - redScalar
                val greenValue = green.toDouble() - greenScalar
                val blueValue = blue.toDouble() - blueScalar
                lineType = Imgproc.LINE_8
                maxLevel = if (hierarchy1MatField2 != -1) {
                    0
                } else {
                    1
                }
                srcImageMat = currentSrcMat
                contoursUpdated = contours1
                contourIdxUpdated = contourIdx
                hierarchy1MatUpdated = hierarchy1Mat
                Imgproc.drawContours(
                    srcImageMat,
                    contoursUpdated,
                    contourIdxUpdated,
                    Scalar(redValue, greenValue, blueValue, 0.0),
                    -1,
                    8,
                    hierarchy1MatUpdated,
                    0
                )
                colorScalar = Scalar(redValue, greenValue, blueValue, 0.0)
                Imgproc.drawContours(srcImageMat, contoursUpdated, contourIdxUpdated, colorScalar, EXTEND_LENGTH, lineType, hierarchy1MatUpdated, maxLevel)
                Core.mean(currentCanvasMat, maskMat)
                Imgproc.drawContours(srcImageMat, contoursUpdated, contourIdxUpdated, colorScalar, EXTEND_LENGTH, lineType, hierarchy1MatUpdated, maxLevel)
            } else {
                imageWidthPortion2Updated = imageWidthPortion2
                firstMatOfPoint2fUpdated = firstMatOfPoint2f
                secondMatOfPoint2fUpdated = secondMatOfPoint2f
                fillPolyIndicesUpdated = fillPolyIndicesList
                currentCanvasMat = canvas
                hierarchy1Mat = hierarchy1
                contourIdxListUpdated = contourIdxList
            }
            pointsColorsSeedsCounter = positionPointsColorsSeed + 1
            hierarchy = hierarchy1Mat
            canvas = currentCanvasMat
            imageWidthPortion2 = imageWidthPortion2Updated
            srcmat = currentSrcMat
            contours = contours1
            firstMatOfPoint2f = firstMatOfPoint2fUpdated
            secondMatOfPoint2f = secondMatOfPoint2fUpdated
            contourIdxList = contourIdxListUpdated
            fillPolyIndicesList = fillPolyIndicesUpdated
        }

        resultMat = Mat(sectionImage, newImageWidth, CvType.CV_16SC4, Scalar(0.0, 0.0, 0.0, 0.0))
        canvas.convertTo(resultMat, CvType.CV_16SC4, 1.0)
        Core.add(resultMat, srcmat, resultMat)
        Core.max(resultMat, Scalar(0.0, 0.0, 0.0, 0.0), resultMat)
        Core.min(resultMat, Scalar(255.0, 255.0, 255.0, 255.0), resultMat)
        resultMat.convertTo(canvas, CvType.CV_8UC4, 1.0)
        Imgproc.resize(canvas, canvas, Size(widthBitmap.toDouble(), heightBitmap.toDouble()))
        val createBitmap = Bitmap.createBitmap(widthBitmap, heightBitmap, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(canvas, createBitmap)

        createScaledBitmap.recycle()
        return createBitmap
    }


    override fun useBrush(bitmap: Bitmap, seed: SeedPointAndColor, brushSize: Int): Bitmap {
        val canvasMat = Mat()
        val widthBitmap         = bitmap.width
        val heightBitmap        = bitmap.height

        Utils.bitmapToMat(bitmap, canvasMat)
        Imgproc.circle(
            canvasMat,
            Point(seed.tapPoint.x, seed.tapPoint.y),
            brushSize,
            Scalar(seed.red.toDouble(), seed.green.toDouble(), seed.blue.toDouble(), 255.0),
            -1
        )
        Utils.matToBitmap(canvasMat, bitmap)

        val createBitmap = Bitmap.createBitmap(widthBitmap, heightBitmap, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(canvasMat, createBitmap)
        return createBitmap
    }

    companion object {
        const val IMAGE_RESIZE_FRAME_WIDTH = 600
        var EXTEND_LENGTH = 14
    }

    init {
        // Boolean bool, Boolean bool2, Boolean bool3, int i, float f, double d
        // true, false, true, 2, 7f, 7.0
        Core.setNumThreads(12)
        mResizedImageWidth = IMAGE_RESIZE_FRAME_WIDTH
    }
}