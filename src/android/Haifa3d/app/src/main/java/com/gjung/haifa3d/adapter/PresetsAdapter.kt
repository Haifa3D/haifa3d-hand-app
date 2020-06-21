package com.gjung.haifa3d.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gjung.haifa3d.databinding.PresetItemBinding
import com.gjung.haifa3d.model.Preset
import com.gjung.haifa3d.ui.presets.PresetsViewModel

class PresetsAdapter(private val viewModel: PresetsViewModel): RecyclerView.Adapter<PresetsAdapter.ViewHolder>() {
    var onItemClickListener: OnItemClickListener? = null
    var onItemEditClickListener: OnItemClickListener? = null
    private val presets
        get() = viewModel.presets.value!!

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
        holder.presetName = "Preset #${position + 1}"
        holder.subtitle = "Tap here to start this action right now"
    }
}