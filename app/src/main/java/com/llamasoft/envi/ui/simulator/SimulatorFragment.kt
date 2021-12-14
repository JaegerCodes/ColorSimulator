package com.llamasoft.envi.ui.simulator

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
import com.tektonlabs.americancolors.app.util.onTouch
import org.opencv.core.Point

class SimulatorFragment : Fragment() {

    private var _binding: FragmentSimulatorBinding? = null
    private val binding get() = _binding!!
    private var rgbWallColor    : List<Int> = arrayListOf(0, 0, 0)
    private var srcBitmap       : Bitmap? = null
    private var paintedImage    : Bitmap? = null
    private var paintType       : PaintType = PaintType.Paint
    private var paintBrushSize  : Int = 10
    private val segmentor       : SegmentorImpl = SegmentorImpl()
    private var seeds: MutableList<SeedPointAndColor> = ArrayList()
    private lateinit var optionPaintAdapter: OptionPaintAdapter

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
        binding.apply {
            selectedImage.onTouch { image, event ->
                binding.mainScroll.setScrollingEnabled(false)
                when (event.action) {
                    MotionEvent.ACTION_UP ->
                        if (paintType == PaintType.Paint) paintPicture(image as ImageView, event)
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE ->
                        if (paintType == PaintType.Brush) useBrush(image as ImageView, event)
                    MotionEvent.ACTION_CANCEL ->
                        binding.mainScroll.setScrollingEnabled(true)
                }
            }
            recyclerView.adapter = SavedColorAdapter(emptyColors, listenerSavedColors)
            brushSizeSlider.addOnChangeListener { _, value, _ -> paintBrushSize = value.toInt() }
            optionPaintAdapter  = OptionPaintAdapter(paintMenu, paintOptions)
            val itemAnimator    = paintMenuList.itemAnimator as SimpleItemAnimator?
            itemAnimator?.supportsChangeAnimations = false
            paintMenuList.adapter = optionPaintAdapter
            loadImage()
        }
        //colorsVM.getSavedColors()
        //simulatorVM.updateColor("#FFFFFFFF")
        //colorsVM.setProjectStep(ProjectSteps.ImagePainter.name)
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

    private fun loadRemoteImage(urlImage: String) = Glide.with(requireContext())
        .asBitmap()
        .placeholder(R.drawable.ic_image_placeholder)
        .load(urlImage)
        .into(object : BitmapImageViewTarget(binding.selectedImage) {
            override fun setResource(resource: Bitmap?) {
                _binding?.originalImage?.setImageBitmap(resource)
                _binding?.brushImage?.setImageBitmap(resource)
                _binding?.selectedImage?.setImageBitmap(resource)
                srcBitmap = resource
                //simulatorVM.initializeSimulator(srcBitmap)
            }
        })

    private fun loadLocalImage(uriImage: Uri) = binding.apply {
        selectedImage.setImageURI(uriImage)
        selectedImage.drawable?.let {
            srcBitmap = (it as BitmapDrawable).bitmap
            originalImage.setImageBitmap(srcBitmap)
            brushImage.setImageBitmap(srcBitmap)
            //simulatorVM.initializeSimulator(srcBitmap)
        }
    }

    private val paintOptions = object : PaintMenuListener {
        override fun onPressBucketButton(option: OptionPaint) {
            setAndShowBrushSlider(false)
            paintType = PaintType.Paint

            optionPaintAdapter.changeBordersColor(option)
            binding.selectedImage.drawable?.let {
                srcBitmap = (it as BitmapDrawable).bitmap
                binding.brushImage.setImageBitmap(srcBitmap)
            }
            bringToFront(binding.selectedImage)
        }

        override fun onPressBrushButton(option: OptionPaint) {
            setAndShowBrushSlider(true, resources.getString(R.string.brush_slider_title_paint_brush))
            paintType = PaintType.Brush
            optionPaintAdapter.changeBordersColor(option)
            binding.selectedImage.drawable?.let {
                srcBitmap = (it as BitmapDrawable).bitmap
                binding.brushImage.setImageBitmap(srcBitmap)
            }
            bringToFront(binding.brushImage)
        }

        override fun onPressMirrorButton(option: OptionPaint) {
            setAndShowBrushSlider(false)
            paintType = PaintType.None
            optionPaintAdapter.changeBordersColor(option)
            bringToFront(binding.originalImage)
        }

        override fun onPressUndoButton(option: OptionPaint) {
            setAndShowBrushSlider(false)
            bringToFront(binding.selectedImage)
            undoPoint(srcBitmap)
            binding.selectedImage.setImageBitmap(paintedImage)
        }


        override fun onPressSwatchButton() {
            val colorExplorerDialog: ColorExplorerDialog = ColorExplorerDialog.newInstance(colorPickerLister)
            colorExplorerDialog.show(childFragmentManager, colorExplorerDialog.tag)
        }
    }
    private fun undoPoint(srcBitmap: Bitmap?) {
        val lastSeed = seeds.lastOrNull()
        paintedImage = if (lastSeed != null) {
            seeds.remove(lastSeed)
            segmentor.predictAndColorMultiTapSingleMask(srcBitmap, seeds)
        } else {
            segmentor.predictAndColorMultiTapSingleMask(srcBitmap, arrayListOf())
        }

    }
    private fun bringToFront(image: ImageView) = binding.apply {
        when (image.id) {
            R.id.selectedImage -> {
                originalImage.visibility = View.INVISIBLE
                selectedImage.bringToFront()
                selectedImage.visibility = View.VISIBLE
            }
            R.id.originalImage -> {
                selectedImage.visibility = View.INVISIBLE
                originalImage.bringToFront()
                originalImage.visibility = View.VISIBLE
            }
        }
        brushSizeSliderContainer.bringToFront()
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

    /*private val colorFinderListener: AppListener<String> = object : AppListener<String> {
        override fun onPressPrimaryButton(model: String) {
            updateColor(model)
            //colorsVM.saveColor(ColorUnits("TITLE", "102", model))
        }
    }*/

    private fun paintPicture(image: ImageView, event: MotionEvent) {
        paintContoursFromSelectedWall(srcBitmap, image, event)
        binding.selectedImage.setImageBitmap(paintedImage)
    }

    private fun useBrush(image: ImageView, event: MotionEvent) {
        setAndShowBrushSlider(false)
        paintWithBrush(srcBitmap, image, event, paintBrushSize)
        binding.selectedImage.setImageBitmap(paintedImage)
    }

    private var listenerSavedColors: AppListener<SavedColors> = object : AppListener<SavedColors> {
        override fun onSelectItemView(model: SavedColors) {
            updateColor(model.hex)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateColor(hexString: String) {
        val hex: Int = Color.parseColor(hexString)
        // val alpha = hex and -0x1000000 shr 24
        val red = hex and 0xFF0000 shr 16
        val green = hex and 0xFF00 shr 8
        val blue = hex and 0xFF
        rgbWallColor = arrayListOf(red, green, blue)
    }

    private fun paintWithBrush(srcBitmap: Bitmap?, imageView: ImageView, imageTap: MotionEvent, brushSize: Int) {
        val (x, y) = getMotionEvent(imageTap, imageView)
        if (seeds.size <= 500) {
            //updateWall(srcBitmap, x.toDouble(), y.toDouble())
            srcBitmap?.let {
                val seed = SeedPointAndColor(Point(x.toDouble(), y.toDouble()), rgbWallColor)
                paintedImage = segmentor.useBrush(it, seed, brushSize)
            }

        }
    }

    private fun paintContoursFromSelectedWall(
        srcBitmap: Bitmap?, imageView: ImageView, imageTap: MotionEvent
    ) {
        if (rgbWallColor.isNotEmpty()) {
            val (x, y) = getMotionEvent(imageTap, imageView)
            if (seeds.size <= 500) {
                //updateWall(srcBitmap, x.toDouble(), y.toDouble())
                addSeedColor(x.toDouble(), y.toDouble())
                paintedImage = segmentor.predictAndColorMultiTapSingleMask(srcBitmap, seeds)
            }
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

    private fun addSeedColor(tapPointX: Double, tapPointY: Double) = seeds
        .add(SeedPointAndColor(Point(tapPointX, tapPointY), rgbWallColor))
}