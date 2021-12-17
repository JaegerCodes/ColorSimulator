package com.llamasoft.envi.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.llamasoft.envi.R
import com.llamasoft.envi.data.OptionPaint
import com.llamasoft.envi.util.PaintMenu
import com.llamasoft.envi.util.PaintMenuListener

class OptionPaintAdapter(
    private var options: MutableList<OptionPaint>,
    private val listener: PaintMenuListener
): RecyclerView.Adapter<OptionPaintAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(
            R.layout.item_option_paint,
            parent,
            false
        )
        return ViewHolder(view, parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(options[position])
    }

    override fun getItemCount(): Int = options.size

    fun changeBordersColor(option: OptionPaint) {
        val position = options.indexOf(option)
        options[position].clicked = true
        options.mapIndexed { index, _ ->
            if (index != position) { options[index].clicked = false }
        }
        notifyItemRangeChanged(0, options.size - 1)
    }

    inner class ViewHolder(itemView: View, val parent: ViewGroup) : RecyclerView.ViewHolder(itemView) {

        fun bindItem(option: OptionPaint) {
            val layout = itemView.findViewById<ConstraintLayout>(R.id.item)
            val container = itemView.findViewById<ShapeableImageView>(R.id.border)
            val icon = itemView.findViewById<ImageView>(R.id.icon_simulator)
            icon.setImageResource(option.iconDrawableRes)
            if (option.clickable) {
                changeItemTint(container, icon, option.clicked)
            }
            layout.setOnClickListener {
                when (option.type) {
                    PaintMenu.Bucket    -> listener.onPressBucketButton(option)
                    PaintMenu.Brush     -> listener.onPressBrushButton(option)
                    PaintMenu.Mirror    -> listener.onPressMirrorButton(option)
                    PaintMenu.Undo      -> listener.onPressUndoButton(option)
                    PaintMenu.Refresh   -> listener.onPressRefreshButton(option)
                    PaintMenu.Pinch     -> listener.onPressPinchButton(option)
                    PaintMenu.Swatch    -> listener.onPressSwatchButton()
                }
            }
        }

        private fun changeItemTint(container: ShapeableImageView, icon: ImageView, clicked: Boolean) {
            if (clicked) {
                container.strokeColor = AppCompatResources
                    .getColorStateList(itemView.context, R.color.gray_800)
                container.backgroundTintList = AppCompatResources
                    .getColorStateList(itemView.context, R.color.gray_800)
                icon.imageTintList = AppCompatResources
                    .getColorStateList(itemView.context, R.color.gray_60)
            } else {
                container.strokeColor = AppCompatResources
                    .getColorStateList(itemView.context, R.color.gray_300)
                container.backgroundTintList = AppCompatResources
                    .getColorStateList(itemView.context, R.color.gray_60)
                icon.imageTintList = AppCompatResources
                    .getColorStateList(itemView.context, R.color.gray_800)
            }
        }
    }
}