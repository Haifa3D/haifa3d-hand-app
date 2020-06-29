package com.gjung.haifa3d.ui.presets

import android.os.Bundle
import android.view.*
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gjung.haifa3d.BleFragment

import com.gjung.haifa3d.R
import com.gjung.haifa3d.ble.IDirectExecuteService
import com.gjung.haifa3d.ble.IPresetService
import com.gjung.haifa3d.databinding.FragmentEditMovementBinding
import com.gjung.haifa3d.model.*
import com.gjung.haifa3d.notifyObserver
import com.gjung.haifa3d.setNavigationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class EditMovementFragment : BleFragment() {
    private lateinit var binding: FragmentEditMovementBinding
    private var directExecuteService: IDirectExecuteService? = null
    private var presetService: IPresetService? = null
    private val args: EditMovementFragmentArgs by navArgs()
    private val presetsViewModel: PresetsViewModel by activityViewModels()

    private val preset
        get() = presetsViewModel.presets.value!![args.presetId]
    private var movement
        get() = preset.handAction!!.Movements[args.movementId]
        set(value) {
            preset.handAction!!.Movements[args.movementId] = value
        }

    override fun onServiceConnected() {
        presetService = bleService!!.manager.presetService
        directExecuteService = bleService!!.manager.directExecuteService
    }

    override fun onServiceDisconnected() {
        presetService = null
        directExecuteService = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_movement, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_try_movement -> {
                tryMovement()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun tryMovement() {
        directExecuteService?.executeAction(HandAction(editMovementValue))
    }

    private var editMovementValue
        get() = HandMovement(
                    TorqueStopModeDetail(if (binding.torqueSwitch.isChecked) TorqueStopThreshold.High else  TorqueStopThreshold.Low),
                    TimeStopModeDetail(255u),
                    MotorsActivated(
                        binding.turnLeftButton.isChecked || binding.turnRightButton.isChecked,
                        binding.finger1OpenButton.isChecked || binding.finger1CloseButton.isChecked,
                        binding.finger2OpenButton.isChecked || binding.finger2CloseButton.isChecked,
                        binding.finger3OpenButton.isChecked || binding.finger3CloseButton.isChecked,
                        binding.finger4OpenButton.isChecked || binding.finger4CloseButton.isChecked
                    ),
                    MotorsDirection(
                        if (binding.turnLeftButton.isChecked) MotorDirection.Dir2 else MotorDirection.Dir1,
                        if (binding.finger1CloseButton.isChecked) MotorDirection.Dir2 else MotorDirection.Dir1,
                        if (binding.finger2CloseButton.isChecked) MotorDirection.Dir2 else MotorDirection.Dir1,
                        if (binding.finger3CloseButton.isChecked) MotorDirection.Dir2 else MotorDirection.Dir1,
                        if (binding.finger4CloseButton.isChecked) MotorDirection.Dir2 else MotorDirection.Dir1
                    )
                )
        set(value) {
            binding.torqueSwitch.isChecked = value.torqueDetail.turn == TorqueStopThreshold.High
            binding.turnRightButton.isChecked = value.motorsActivated.turn && value.motorsDirection.turn == MotorDirection.Dir1
            binding.turnLeftButton.isChecked = value.motorsActivated.turn && value.motorsDirection.turn == MotorDirection.Dir2
            binding.finger1OpenButton.isChecked = value.motorsActivated.finger1 && value.motorsDirection.turn == MotorDirection.Dir1
            binding.finger1CloseButton.isChecked = value.motorsActivated.finger1 && value.motorsDirection.turn == MotorDirection.Dir2
            binding.finger2OpenButton.isChecked = value.motorsActivated.finger2 && value.motorsDirection.turn == MotorDirection.Dir1
            binding.finger2CloseButton.isChecked = value.motorsActivated.finger2 && value.motorsDirection.turn == MotorDirection.Dir2
            binding.finger3OpenButton.isChecked = value.motorsActivated.finger3 && value.motorsDirection.turn == MotorDirection.Dir1
            binding.finger3CloseButton.isChecked = value.motorsActivated.finger3 && value.motorsDirection.turn == MotorDirection.Dir2
            binding.finger4OpenButton.isChecked = value.motorsActivated.finger4 && value.motorsDirection.turn == MotorDirection.Dir1
            binding.finger4CloseButton.isChecked = value.motorsActivated.finger4 && value.motorsDirection.turn == MotorDirection.Dir2
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditMovementBinding.inflate(layoutInflater, container, false)
        setHasOptionsMenu(true)

        binding.turnLeftButton.setOnClickListener { binding.turnRightButton.isChecked = false; movement = editMovementValue }
        binding.turnRightButton.setOnClickListener { binding.turnLeftButton.isChecked = false; movement = editMovementValue }
        binding.finger1OpenButton.setOnClickListener { binding.finger1CloseButton.isChecked = false; movement = editMovementValue }
        binding.finger1CloseButton.setOnClickListener { binding.finger1OpenButton.isChecked = false; movement = editMovementValue }
        binding.finger2OpenButton.setOnClickListener { binding.finger2CloseButton.isChecked = false; movement = editMovementValue }
        binding.finger2CloseButton.setOnClickListener { binding.finger2OpenButton.isChecked = false; movement = editMovementValue }
        binding.finger3OpenButton.setOnClickListener { binding.finger3CloseButton.isChecked = false; movement = editMovementValue }
        binding.finger3CloseButton.setOnClickListener { binding.finger3OpenButton.isChecked = false; movement = editMovementValue }
        binding.finger4OpenButton.setOnClickListener { binding.finger4CloseButton.isChecked = false; movement = editMovementValue }
        binding.finger4CloseButton.setOnClickListener { binding.finger4OpenButton.isChecked = false; movement = editMovementValue }

        binding.torqueSwitch.setOnCheckedChangeListener { _, checked ->
            binding.torqueValue.setText(if (checked) R.string.edit_movement_torque_high else R.string.edit_movement_torque_low)
            movement = editMovementValue
        }

        editMovementValue = movement

        return binding.root
    }
}
