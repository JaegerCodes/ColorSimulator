package com.llamasoft.envi.ui.simulator

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.llamasoft.envi.R
import com.llamasoft.envi.data.OptionPaint
import com.llamasoft.envi.data.SavedColors
import com.llamasoft.envi.data.emptyColors
import com.llamasoft.envi.data.paintMenu
import com.llamasoft.envi.databinding.FragmentSimulatorBinding
import com.llamasoft.envi.ui.adapters.OptionPaintAdapter
import com.llamasoft.envi.ui.adapters.SavedColorAdapter
import com.llamasoft.envi.ui.components.ColorExplorerDialog
import com.llamasoft.envi.util.*
import com.llamasoft.envi.ar.segmentor.SeedPointAndColor
import com.llamasoft.envi.ar.segmentor.SegmentorImpl
import com.llamasoft.envi.databinding.SheetPaintToolsBinding
import com.tektonlabs.americancolors.app.util.onTouch
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Point

class SimulatorFragment : Fragment() {

    private var _binding: FragmentSimulatorBinding? = null
    private val binding get() = _binding!!
    private var originalBitmap  : Bitmap? = null
    private var srcBitmap       : Bitmap? = null
    private var brushStaging    : Bitmap? = null
    private var rgbWallColor    : List<Int> = arrayListOf(0, 0, 0)
    private val imageQueue = ArrayDeque<Bitmap>(100)
    private val historySize     : Int = 10
    private var oldHeight       : Float = 0f
    private var savedColors     : List<SavedColors> = ArrayList()
    private var paintBrushSize  : Int = 10
    private var paintType       : PaintType = PaintType.Paint
    private val segmentor       : SegmentorImpl = SegmentorImpl()
    private lateinit var optionPaintAdapter: OptionPaintAdapter
    private lateinit var sheet: SheetPaintToolsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSimulatorBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
    }

    private fun setupView() {
        sheet = binding.sheetPaintTools
        binding.apply {
            binding.root.viewTreeObserver
                .addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        binding.root.viewTreeObserver.removeOnPreDrawListener(this)
                        oldHeight = getSheetHeight()
                        return true
                    }
                })
            sheet.layout.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                if (oldHeight < getSheetHeight()) {
                    oldHeight = getSheetHeight() * 2
                    sheet.layout.animate().translationY(getSheetHeight()).duration = 1000
                }
            }
            sheet.peekButton.onTouch { _, motionEvent ->
                when (motionEvent.action) { MotionEvent.ACTION_DOWN -> runSheetAnimation() }
            }
            binding.sheetBg.onTouch { _, motionEvent ->
                when (motionEvent.action) { MotionEvent.ACTION_DOWN -> runSheetAnimation() }
            }
            selectedImage.onTouch { image, event ->
                when (paintType) {
                    PaintType.Paint -> {
                        when (event.action) {
                            MotionEvent.ACTION_UP -> paintPicture(image as ImageView, event)
                        }
                    }
                    PaintType.Brush -> {
                        showVerticalTools(event)
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                selectedImage.maxZoom = selectedImage.currentZoom
                                selectedImage.minZoom = selectedImage.currentZoom
                                startBrushEvent()
                            }
                            MotionEvent.ACTION_MOVE -> useBrush(image as ImageView, event)
                            MotionEvent.ACTION_UP,
                            MotionEvent.ACTION_CANCEL ->
                                stopBrushEvent()
                        }
                    }
                    else -> {}
                }
            }
            undoButton.setOnClickListener {
                onClickUndoButton()
            }
            sliderBackground.setOnClickListener {
                setAndShowBrushSlider(false)
            }
            brushSizeSlider.addOnChangeListener { _, value, _ -> paintBrushSize = value.toInt() }
        }
        setupPaintTools()
        configureOpenCV()
    }
    private fun welcomeAnimation() {
        sheet.layout.alpha = 1f
        sheet.layout.animate().translationY(getSheetHeight()).duration = 1000
    }
    private fun showVerticalTools(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> binding.verticalTools.visibility = View.VISIBLE
            else -> binding.verticalTools.visibility = View.INVISIBLE
        }
    }


    private fun onClickUndoButton() {
        setAndShowBrushSlider(false)
        undoStep()
        showNewestImage()
    }

    private fun setupPaintTools() = sheet.apply {
        val itemAnimator        = paintMenuList.itemAnimator as SimpleItemAnimator?
        itemAnimator?.supportsChangeAnimations = false
        optionPaintAdapter      = OptionPaintAdapter(paintMenu, paintToolsListener)
        paintMenuList.adapter   = optionPaintAdapter
        loadImage()
    }

    private fun getSheetHeight() = sheet.layout.height.toFloat() - sheet.peekButton.height.toFloat()

    private fun runSheetAnimation() {
        sheet.layout.alpha = 1f
        if (sheet.layout.translationY > 0) {
            sheet.layout.animate().translationY(0f).duration = 300
            binding.sheetBg.visibility = View.VISIBLE
        } else {
            sheet.layout.animate().translationY(getSheetHeight()).duration = 300
            binding.sheetBg.visibility = View.GONE
        }
    }

    private fun startBrushEvent(){
        val tmp = imageQueue.last()
        brushStaging = tmp.copy(tmp.config,true)
    }

    private fun stopBrushEvent(){
        addImageStep(brushStaging!!)
        binding.selectedImage.setImageBitmap(brushStaging)
    }

    private fun loadImage() = binding.apply {
        arguments?.apply {
            getString(BundleName.UriImage.value)?.let {
                val loadArg = getString(BundleName.LoadImage.value)
                when (LoadImage.get(loadArg)) {
                    LoadImage.Local -> loadLocalImage(Uri.parse(it))
                    LoadImage.Remote -> loadRemoteImage(it)
                }
            }

        }
    }
    private fun setImage(resource: Bitmap?){
        _binding?.selectedImage?.setImageBitmap(resource)
        srcBitmap = resource
        imageQueue.clear()
        if (srcBitmap != null) {
            imageQueue.addLast(srcBitmap!!)
        }
    }
    private fun loadRemoteImage(urlImage: String) = Glide.with(requireContext())
        .asBitmap()
        .placeholder(R.drawable.ic_image_placeholder)
        .load(urlImage)
        .into(object : BitmapImageViewTarget(binding.selectedImage) {
            override fun setResource(resource: Bitmap?) {
                originalBitmap = resource
                setImage(resource)
            }
        })

    private fun loadLocalImage(uriImage: Uri) = binding.apply {
        selectedImage.setImageURI(uriImage)
        selectedImage.drawable?.let {
            (it as BitmapDrawable).bitmap?.let { bm ->
                srcBitmap       = bm
                originalBitmap  = bm
                setImage(srcBitmap)
            }
            welcomeAnimation()
        }
    }

    private val paintToolsListener = object : PaintMenuListener {
        override fun onPressBucketButton(option: OptionPaint) {
            binding.selectedImage.scrollable = true
            setAndShowBrushSlider(false)
            paintType = PaintType.Paint
            optionPaintAdapter.changeBordersColor(option)
            showNewestImage()
            binding.currentTool.setImageResource(option.iconDrawableRes)
        }

        override fun onPressBrushButton(option: OptionPaint) {
            binding.selectedImage.scrollable = false
            setAndShowBrushSlider(true, resources.getString(R.string.brush_slider_title_paint_brush))
            paintType = PaintType.Brush
            optionPaintAdapter.changeBordersColor(option)
            showNewestImage()
            runSheetAnimation()
            binding.currentTool.setImageResource(option.iconDrawableRes)
        }

        override fun onPressMirrorButton(option: OptionPaint) {
            binding.selectedImage.scrollable = true
            setAndShowBrushSlider(false)
            optionPaintAdapter.changeBordersColor(option)
            binding.currentTool.setImageResource(option.iconDrawableRes)
            binding.selectedImage.setImageBitmap(originalBitmap)
            paintType = PaintType.None
            runSheetAnimation()
            binding.selectedImage.resetZoom()
        }

        override fun onPressUndoButton(option: OptionPaint) {
            onClickUndoButton()
        }

        override fun onPressRefreshButton(option: OptionPaint) {
            runSheetAnimation()
            onClickRefreshButton()
        }

        override fun onPressPinchButton(option: OptionPaint) {
            optionPaintAdapter.changeBordersColor(option)
            binding.selectedImage.scrollable = true
            paintType = PaintType.None
            binding.currentTool.setImageResource(option.iconDrawableRes)
            binding.selectedImage.resetZoom()
        }

        override fun onPressSwatchButton() {
            val colorExplorerDialog: ColorExplorerDialog = ColorExplorerDialog.newInstance(colorPickerLister)
            colorExplorerDialog.show(childFragmentManager, colorExplorerDialog.tag)
        }
    }

    private fun onClickRefreshButton() {
        setAndShowBrushSlider(false)
        setImage(originalBitmap)
    }

    private fun showNewestImage() {
        binding.selectedImage.setImageBitmap(imageQueue.last())
        binding.brushSizeSliderContainer.bringToFront()
    }
    private fun undoStep() {
        if (imageQueue.size > 1) {
            imageQueue.removeLast()
        }
    }

    private fun setAndShowBrushSlider(show : Boolean, title : String? = null) {
        if (!title.isNullOrBlank()) binding.brushSizeSliderTitleText.text = title
        if (show) {
            binding.brushSizeSliderContainer.visibility = View.VISIBLE
        } else {
            binding.brushSizeSliderContainer.visibility = View.GONE
        }
    }

    private val colorPickerLister: AppListener<String> = object : AppListener<String> {
        override fun onPressPrimaryButton(model: String) {
            updateColor(model)
        }
    }

    private fun paintPicture(image: ImageView, event: MotionEvent) {
        paintContoursFromSelectedWall(image, event)
        showNewestImage()
    }

    private fun useBrush(image: ImageView, event: MotionEvent) {
        setAndShowBrushSlider(false)
        paintWithBrush(image, event, paintBrushSize)
        binding.selectedImage.setImageBitmap(brushStaging)
    }

    private var listenerSavedColors: AppListener<SavedColors> = object : AppListener<SavedColors> {
        override fun onSelectItemView(model: SavedColors) {
            updateColor(model.hex)
            // binding.currentColor.imageTintList = ColorStateList.valueOf(Color.parseColor(model.hex))
            binding.currentColor.backgroundTintList = ColorStateList.valueOf(Color.parseColor(model.hex))
        }
    }

    fun updateColor(hexString: String) {
        val hex: Int = Color.parseColor(hexString)
        // val alpha = hex and -0x1000000 shr 24
        val red = hex and 0xFF0000 shr 16
        val green = hex and 0xFF00 shr 8
        val blue = hex and 0xFF
        rgbWallColor = arrayListOf(red, green, blue)
    }

    private fun addImageStep(bitmap: Bitmap){
        imageQueue.addLast(bitmap)
        if (imageQueue.size > historySize) {
            imageQueue.removeFirst()
        }
    }

    private fun paintWithBrush(imageView: ImageView, imageTap: MotionEvent, brushSize: Int) {
        val (x, y) = getMotionEvent(imageTap, imageView)
        brushStaging?.let {
            val seed = SeedPointAndColor(Point(x.toDouble(), y.toDouble()), rgbWallColor)
            brushStaging = segmentor.useBrush(it, seed, brushSize)
        }
    }

    private fun paintContoursFromSelectedWall(
        imageView: ImageView, imageTap: MotionEvent
    ) {
        if (rgbWallColor.isNotEmpty()) {
            val (x, y) = getMotionEvent(imageTap, imageView)
            val seed = SeedPointAndColor(Point(x.toDouble(), y.toDouble()), rgbWallColor)
            val bitmap = segmentor.predictAndColorMultiTapSingleMask(imageQueue.last(), listOf(seed))
            bitmap?.let { addImageStep(it) }
        }
    }

    private fun getMotionEvent(
        imageTap: MotionEvent,
        imageView: ImageView
    ): Pair<Int, Int> {
        val eventX = imageTap.x
        val eventY = imageTap.y
        val eventXY = floatArrayOf(eventX, eventY)

        val invertMatrix = Matrix()
        imageView.imageMatrix.invert(invertMatrix)

        invertMatrix.mapPoints(eventXY)
        var x = Integer.valueOf(eventXY[0].toInt())
        var y = Integer.valueOf(eventXY[1].toInt())

        val imgDrawable: Drawable = imageView.drawable
        val bitmap = (imgDrawable as BitmapDrawable).bitmap

        if (x < 0) {
            x = 0
        } else if (x > bitmap.width - 1) {
            x = bitmap.width - 1
        }

        if (y < 0) {
            y = 0
        } else if (y > bitmap.height - 1) {
            y = bitmap.height - 1
        }
        return Pair(x, y)
    }

    private fun configureOpenCV() {
        val loaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(requireContext()) {
            override fun onManagerConnected(status: Int) { if (status != SUCCESS) super.onManagerConnected(status) }
        }
        when (!OpenCVLoader.initDebug()) {
            true -> OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, requireContext(), loaderCallback)
            false -> loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }
}