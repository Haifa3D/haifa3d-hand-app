package com.gjung.haifa3d.ui.presets

import android.app.Activity
import android.widget.Toast
import android.os.Bundle
import android.os.UserManager
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.nav_header_connected.*
import kotlinx.coroutines.*


import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.gjung.haifa3d.databinding.ActivityConnectedBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.gjung.haifa3d.R
import com.google.android.material.navigation.NavigationView





val n = 10

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
        super.onCreateOptionsMenu(menu,inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_about -> {
                return false
            }
            R.id.disconnect_button -> {
                return false
            }
            R.id.action_try_preset -> {
                tryPreset()
                return true
            }
            R.id.action_save_hand_action -> {
                saveHandAction()
                return true
            }
            R.id.action_add_hand_movement -> {
                addHandMovement()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun tryPreset() {
        directExecuteService?.executeAction(HandAction(movements))
    }

    private fun addHandMovement() {

        if(movements.size >= n){
            Toast.makeText(getActivity(), "Limited to 10 movements!", Toast.LENGTH_SHORT).show()


            return
        }

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

     fun saveHandAction() {
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
