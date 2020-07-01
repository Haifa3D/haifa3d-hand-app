package com.gjung.haifa3d.ui.config

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.gjung.haifa3d.BleFragment

import com.gjung.haifa3d.R
import com.gjung.haifa3d.adapter.ConfigAdapter
import com.gjung.haifa3d.ble.IConfigField
import com.gjung.haifa3d.ble.IConfigurationService
import com.gjung.haifa3d.databinding.FragmentConfigurationBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalUnsignedTypes
class ConfigurationFragment : BleFragment() {

    private lateinit var binding: FragmentConfigurationBinding
    private lateinit var adapter: ConfigAdapter
    private var configService: IConfigurationService? = null

    override fun onServiceConnected() {
        configService = bleService!!.manager.configurationService
        val fields = configService!!.fields
        adapter.fields = fields
        adapter.notifyDataSetChanged()
        fields.forEachIndexed { idx, field ->
            field.value.observe(viewLifecycleOwner, Observer {
                adapter.notifyItemChanged(idx)
            })
        }
        configService!!.readAllValues()
    }

    override fun onServiceDisconnected() {
        configService = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConfigurationBinding.inflate(layoutInflater, container, false)

        val rec = binding.configRecyclerView

        adapter = ConfigAdapter(listOf())
        adapter.onItemEditClickListener = object : ConfigAdapter.OnItemClickListener {
            override fun onItemClick(field: IConfigField) {
                val editText = EditText(requireContext())
                editText.inputType = InputType.TYPE_CLASS_NUMBER
                MaterialAlertDialogBuilder(this@ConfigurationFragment.requireContext())
                    .setTitle("Change configuration")
                    .setMessage(field.caption)
                    .setView(editText)
                    .setPositiveButton("Change") { _, _ ->
                        val value = editText.text.toString().toUByteOrNull()
                        if (value == null) {
                            showSnackbar(R.string.config_value_invalid)
                        } else {
                            GlobalScope.launch(Dispatchers.IO) {
                                field.setValue(value)
                                withContext(Dispatchers.Main) {
                                    field.read()
                                    showSnackbar(R.string.config_value_changed)
                                }
                            }
                        }
                    }
                    .setNeutralButton("Cancel") { _, _ ->
                    }
                    .create()
                    .show()
            }

        }

        rec.adapter = adapter
        rec.setHasFixedSize(true)

        rec.layoutManager = LinearLayoutManager(this.requireContext())
        rec.addItemDecoration(DividerItemDecoration(this.requireContext(), DividerItemDecoration.VERTICAL))

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun showSnackbar(@StringRes resId: Int) {
        Snackbar.make(binding.root, resId, Snackbar.LENGTH_LONG)
            .show()
    }
}
