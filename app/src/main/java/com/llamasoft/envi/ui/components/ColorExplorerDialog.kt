package com.llamasoft.envi.ui.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.llamasoft.envi.R
import com.llamasoft.envi.util.AppListener

class ColorExplorerDialog(private val colorPickerLister: AppListener<String>) : DialogFragment() {
    private lateinit var pickerSV: PickerSV
    private lateinit var pickerH: PickerH
    private lateinit var swatchColor: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.dialog_color_explorer, container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pickerSV    = view.findViewById(R.id.pickerSV)
        pickerH     = view.findViewById(R.id.pickerH)
        swatchColor = view.findViewById(R.id.swatch_color)
        pickerH.callbackH = { hue: Float ->
            swatchColor.setBackgroundColor(pickerSV.hsvInt(hue))
        }
        pickerSV.callbackSV = {
            swatchColor.setBackgroundColor(pickerSV.hsvInt())
        }
        dialog?.findViewById<FloatingActionButton>(R.id.btn_close)?.setOnClickListener {
            dismiss()
        }
        dialog?.findViewById<Button>(R.id.btn_add_to_pallete)?.setOnClickListener {
            colorPickerLister.onPressPrimaryButton(pickerSV.hsvHex())
            dismiss()
        }
    }


    override fun getTheme(): Int = android.R.style.Theme_Black_NoTitleBar_Fullscreen

    companion object {
        fun newInstance(colorPickerLister: AppListener<String>): ColorExplorerDialog {
            return ColorExplorerDialog(colorPickerLister)
        }
    }
}