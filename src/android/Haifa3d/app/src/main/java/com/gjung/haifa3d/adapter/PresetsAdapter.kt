package com.gjung.haifa3d.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.gjung.haifa3d.databinding.PresetItemBinding
import com.example.haifa3d_ble_api.model.Preset
//import com.gjung.haifa3d.model.Preset
import com.gjung.haifa3d.ui.presets.PresetsViewModel

class PresetsAdapter(var presets: List<Preset>, var presetNames: Map<Preset, String>, var starredPresets: Collection<Preset>): RecyclerView.Adapter<PresetsAdapter.ViewHolder>() {
    var onItemClickListener: OnItemClickListener? = null
    var onItemEditClickListener: OnItemClickListener? = null

    @FunctionalInterface
    interface OnItemClickListener {
        fun onItemClick(preset: Preset)
    }

    init {
        setHasStableIds(true)
    }

    inner class ViewHolder(val binding: PresetItemBinding) : RecyclerView.ViewHolder(binding.root) {
        var presetName: String
            get() = binding.presetName.text.toString()
            set(value) { binding.presetName.text = value }

        var subtitle: String
            get() = binding.presetName.text.toString()
            set(value) { binding.presetSubtitle.text = value }

        var isStarred: Boolean
            get() = binding.starredImage.visibility == View.VISIBLE
            set(value) { binding.starredImage.visibility = if(value) View.VISIBLE else View.GONE }

        init {
            binding.presetContainer.setOnClickListener {
                onItemClickListener?.onItemClick(presets[adapterPosition])
            }

            binding.editButton.setOnClickListener {
                onItemEditClickListener?.onItemClick(presets[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PresetItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = presets.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val preset = presets[position]
        holder.presetName = presetNames[preset]?.let { "#${position + 1} $it" } ?: "Preset #${position + 1}"
        holder.isStarred = starredPresets.contains(preset)
        holder.subtitle = "Tap here to start this action right now"
    }
}