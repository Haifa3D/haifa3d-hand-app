package com.gjung.haifa3d.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gjung.haifa3d.databinding.HomeItemBinding
import com.gjung.haifa3d.databinding.PresetItemBinding
import com.gjung.haifa3d.model.Preset

class HomeAdapter(
    var presets: List<Preset>,
    var presetNames: Map<Preset, String>
): RecyclerView.Adapter<HomeAdapter.ViewHolder>() {
    var onItemClickListener: OnItemClickListener? = null

    @FunctionalInterface
    interface OnItemClickListener {
        fun onItemClick(preset: Preset)
    }

    init {
        setHasStableIds(true)
    }

    inner class ViewHolder(val binding: HomeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        var presetName: String
            get() = binding.caption.text.toString()
            set(value) { binding.caption.text = value }

        init {
            binding.cardView.setOnClickListener {
                onItemClickListener?.onItemClick(presets[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HomeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = presets.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val preset = presets[position]
        holder.presetName = presetNames[preset]?.let { "#${preset.id + 1} $it" } ?: "Preset #${preset.id + 1}"
    }
}