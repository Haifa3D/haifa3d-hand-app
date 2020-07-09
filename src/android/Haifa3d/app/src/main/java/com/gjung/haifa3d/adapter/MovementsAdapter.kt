package com.gjung.haifa3d.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gjung.haifa3d.databinding.MovementItemBinding
import com.gjung.haifa3d.model.HandMovement
import com.gjung.haifa3d.model.MotorDirection
import com.gjung.haifa3d.model.TorqueStopThreshold
import com.gjung.haifa3d.notifyObserver
import com.gjung.haifa3d.ui.presets.PresetsViewModel

class MovementsAdapter(private val viewModel: PresetsViewModel, private val presetId: Int): RecyclerView.Adapter<MovementsAdapter.ViewHolder>() {
    private val movements
        get() = viewModel.presets.value!![presetId].handAction!!.Movements

    var onItemClickListener: OnItemClickListener? = null

    @FunctionalInterface
    interface OnItemClickListener {
        fun onItemClick(movementIndex: Int, movement: HandMovement)
    }

    init {
        setHasStableIds(true)
    }

    inner class ViewHolder(private val binding: MovementItemBinding) : RecyclerView.ViewHolder(binding.root) {
        var title: String
            get() = binding.title.text.toString()
            set(value) { binding.title.text = value }

        var torqueDetail: String
            get() = binding.torqueValue.text.toString()
            set(value) { binding.torqueValue.text = value }

        var timeDetail: String
            get() = binding.timeValue.text.toString()
            set(value) { binding.timeValue.text = value }

        var motorsDetails: String
            get() = binding.motorsValue.text.toString()
            set(value) { binding.motorsValue.text = value }

        init {
            binding.presetContainer.setOnClickListener {
                onItemClickListener?.onItemClick(adapterPosition, movements[adapterPosition])
            }

            binding.deleteButton.setOnClickListener {
                movements.removeAt(adapterPosition)
                viewModel.presets.notifyObserver()
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MovementItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = movements.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title = "Movement #${position + 1}"
        val mvmt = movements[position]
        holder.timeDetail = "${mvmt.timeDetail.durationMs} ms"
        holder.torqueDetail = if (mvmt.torqueDetail.finger1 == TorqueStopThreshold.Low) "Low" else "High"
        var motorDetail = ""
        if (mvmt.motorsActivated.turn)
            motorDetail += "Turn: " + if (mvmt.motorsDirection.turn == MotorDirection.Dir1) "right" else "left"
        if (mvmt.motorsActivated.finger1)
            motorDetail += "\nFinger 1: " + if (mvmt.motorsDirection.finger1 == MotorDirection.Dir1) "open" else "close"
        if (mvmt.motorsActivated.finger2)
            motorDetail += "\nFinger 2: " + if (mvmt.motorsDirection.finger2 == MotorDirection.Dir1) "open" else "close"
        if (mvmt.motorsActivated.finger3)
            motorDetail += "\nFinger 3: " + if (mvmt.motorsDirection.finger3 == MotorDirection.Dir1) "open" else "close"
        if (mvmt.motorsActivated.finger4)
            motorDetail += "\nFinger 4: " + if (mvmt.motorsDirection.finger4 == MotorDirection.Dir1) "open" else "close"
        holder.motorsDetails = motorDetail.trim()
    }
}