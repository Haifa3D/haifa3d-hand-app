package com.gjung.haifa3d.ui.presets

import android.os.Bundle
import android.os.UserManager
import android.view.*
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.gjung.haifa3d.*

import com.gjung.haifa3d.adapter.MovementsAdapter
import com.gjung.haifa3d.adapter.PresetsAdapter
import com.gjung.haifa3d.ble.IDirectExecuteService
import com.gjung.haifa3d.ble.IPresetService
import com.gjung.haifa3d.databinding.FragmentEditPresetBinding
import com.gjung.haifa3d.model.*
import com.gjung.haifa3d.util.InjectorUtils
import kotlinx.coroutines.*

/**
 * A simple [Fragment] subclass.
 */
class EditPresetFragment : BleFragment(), MovementsAdapter.OnItemClickListener {
    private lateinit var binding: FragmentEditPresetBinding
    private var presetService: IPresetService? = null
    private var directExecuteService: IDirectExecuteService? = null
    private val args: EditPresetFragmentArgs by navArgs()
    private val presetsViewModel: PresetsViewModel by activityViewModels {
        InjectorUtils.providePresetsViewModelFactory(requireContext())
    }
    private lateinit var adapter: MovementsAdapter

    private val preset by lazy {
        Transformations.map(presetsViewModel.presets) { presets ->
            presets[args.presetId]
        }
    }

    private val movements
        get() = preset.value!!.handAction!!.Movements

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
        inflater.inflate(R.menu.edit_preset, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_try_preset -> {
                tryPreset()
                true
            }
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

    private fun tryPreset() {
        directExecuteService?.executeAction(HandAction(movements))
    }

    private fun addHandMovement() {
        hideKeyboard(requireActivity())
        movements.add(HandMovement(
            TorqueStopModeDetail(TorqueStopThreshold.Low),
            TimeStopModeDetail(20u),
            MotorsActivated(
                turn = true,
                finger1 = true
            ),
            MotorsDirection(
                turn = MotorDirection.Dir1,
                finger1 = MotorDirection.Dir1
            )
        ))
        adapter.notifyItemInserted(movements.size)
        presetsViewModel.presets.notifyObserver()
    }

    private fun saveHandAction() {
        hideKeyboard(requireActivity())
        GlobalScope.launch(Dispatchers.IO) {
            presetService!!.writePreset(args.presetId, HandAction(movements))
            var name: String? = presetsViewModel.currentEditPresetName.value
            if (name.isNullOrBlank())
                name = null
            var starred = presetsViewModel.currentEditPresetStarred.value ?: false
            presetsViewModel.setPresetInfo(
                args.presetId,
                HandAction(movements),
                name,
                starred
            )
            withContext(Dispatchers.Main) {
                val navController = this@EditPresetFragment.findNavController();
                navController.navigateUp()
            }
        }
    }

    override fun onItemClick(movementIndex: Int, movement: HandMovement) {
        hideKeyboard(requireActivity())
        val act = EditPresetFragmentDirections.editMovement(args.presetId, movementIndex)
        this.findNavController().navigate(act)
    }

    override fun onPause() {
        hideKeyboard(requireActivity())
        super.onPause()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditPresetBinding.inflate(layoutInflater, container, false)
        setHasOptionsMenu(true)

        adapter = MovementsAdapter(presetsViewModel, args.presetId)

        val rec = binding.recyclerViewMovements
        rec.adapter = adapter
        rec.setHasFixedSize(true)
        rec.layoutManager = LinearLayoutManager(this.requireContext())
        rec.addItemDecoration(DividerItemDecoration(this.requireContext(), DividerItemDecoration.VERTICAL))

        adapter.onItemClickListener = this

        Transformations.switchMap(presetsViewModel.presetNames) { names ->
            Transformations.map(preset) { preset ->
                names[preset]
            }
        }.observe(viewLifecycleOwner, Observer {
            if (presetsViewModel.currentEditPresetName.value.isNullOrBlank()) {
                presetsViewModel.currentEditPresetName.postValue(it)
            }
        })

        binding.presetNameEdit.doOnTextChanged { text, _, _, _ ->
            val tx: String = text.toString()
            if (presetsViewModel.currentEditPresetName.value != tx)
                presetsViewModel.currentEditPresetName.postValue(tx)
        }

        presetsViewModel.currentEditPresetName.observe(viewLifecycleOwner, Observer {
            if (binding.presetNameEdit.text?.toString() != it)
                binding.presetNameEdit.setText(it)
        })

        binding.starredCheck.setOnCheckedChangeListener { _, isChecked ->
            if (presetsViewModel.currentEditPresetStarred.value != isChecked)
                presetsViewModel.currentEditPresetStarred.postValue(isChecked)
        }

        presetsViewModel.currentEditPresetStarred.observe(viewLifecycleOwner, Observer {
            if (binding.starredCheck.isChecked != it)
                binding.starredCheck.isChecked = it
        })

        return binding.root
    }

}
