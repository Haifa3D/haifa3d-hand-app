package com.gjung.haifa3d.ui.presets

import android.os.Bundle
import android.os.UserManager
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.gjung.haifa3d.BleFragment

import com.gjung.haifa3d.R
import com.gjung.haifa3d.adapter.MovementsAdapter
import com.gjung.haifa3d.ble.PresetService
import com.gjung.haifa3d.databinding.FragmentEditPresetBinding
import com.gjung.haifa3d.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 */
class EditPresetFragment : BleFragment() {
    private lateinit var binding: FragmentEditPresetBinding
    private var presetService: PresetService? = null
    private val args: EditPresetFragmentArgs by navArgs()
    private val adapter = MovementsAdapter()

    override fun onServiceConnected() {
        presetService = bleService!!.manager.presetService
        adapter.movements.clear()
        adapter.notifyDataSetChanged()
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val preset = presetService!!.readPreset(args.presetId)
                if (preset != null)
                    adapter.movements.addAll(preset.Movements)
            } catch(ex: Throwable) {
            }
            adapter.notifyDataSetChanged()
        }
    }

    override fun onServiceDisconnected() {
        presetService = null
        adapter.movements.clear()
        adapter.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_preset, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save_hand_action -> {
                saveHandAction()
                true
            }
            R.id.action_add_hand_movement -> {
                addHandMovement()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addHandMovement() {
        adapter.movements.add(HandMovement(
            TorqueStopModeDetail(TorqueStopThreshold.Low),
            TimeStopModeDetail(50),
            MotorsActivated(
                turn = true,
                finger1 = true
            ),
            MotorsDirection(
                turn = MotorDirection.Dir1,
                finger1 = MotorDirection.Dir1
            )
        ))
        adapter.notifyItemInserted(adapter.movements.size)
    }

    private fun saveHandAction() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                presetService!!.writePreset(args.presetId, HandAction(adapter.movements))
                val navController = this@EditPresetFragment.findNavController();
                navController.navigateUp()
            } catch(ex: Throwable) {
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditPresetBinding.inflate(layoutInflater, container, false)

        val rec = binding.recyclerViewMovements
        rec.adapter = adapter
        rec.setHasFixedSize(true)
        rec.layoutManager = LinearLayoutManager(this.requireContext())
        rec.addItemDecoration(DividerItemDecoration(this.requireContext(), DividerItemDecoration.VERTICAL))

        return binding.root
    }

}
