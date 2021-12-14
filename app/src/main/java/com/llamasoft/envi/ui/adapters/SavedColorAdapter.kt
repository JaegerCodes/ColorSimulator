package com.llamasoft.envi.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.llamasoft.envi.R
import com.llamasoft.envi.data.SavedColors
import com.llamasoft.envi.util.AppListener

class SavedColorAdapter(
    private val colors: List<SavedColors>,
    private val listener: AppListener<SavedColors>
): RecyclerView.Adapter<SavedColorAdapter.ViewHolder>() {
    var savedColors: MutableList<SavedColors> = colors.toMutableList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(
            R.layout.item_swatch_saved_color,
            parent,
            false
        )
        return ViewHolder(view, parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(colors[position])
    }

    override fun getItemCount(): Int = colors.size

    fun delete(model: SavedColors) {
        val index = colors.indexOf(model)
        model.saved = false
        notifyItemChanged(index)
        listener.onPressDeleteButton(model)
    }


    inner class ViewHolder(itemView: View, val parent: ViewGroup) : RecyclerView.ViewHolder(itemView) {

        fun bindItem(savedColor: SavedColors) {
            val item    = itemView.findViewById<ConstraintLayout>(R.id.item)
            val layout = itemView.findViewById<ImageView>(R.id.image_saved_color)
            val image = itemView.findViewById<ImageView>(R.id.add_icon)
            if (savedColor.saved) {
                image.visibility = View.INVISIBLE
                layout.setBackgroundColor(Color.parseColor(savedColor.hex))
                item.setOnClickListener {
                    listener.onSelectItemView(savedColor)
                }
            } else {
                image.visibility = View.VISIBLE
                layout.setBackgroundResource(R.drawable.background_dotted_gray)
                item.setOnClickListener {}
            }
        }
    }
}