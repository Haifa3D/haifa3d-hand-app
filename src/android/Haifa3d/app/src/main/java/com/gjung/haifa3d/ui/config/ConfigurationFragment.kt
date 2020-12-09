package com.gjung.haifa3d.ui.config

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toolbar
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.gjung.haifa3d.BleFragment
import androidx.appcompat.app.AppCompatActivity
import com.gjung.haifa3d.R
import com.gjung.haifa3d.adapter.ConfigAdapter
import com.gjung.haifa3d.ble.*
import com.gjung.haifa3d.databinding.FragmentConfigurationBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

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
            field.content.observe(viewLifecycleOwner, Observer {
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
        setActivityTitle("Config")

        adapter = ConfigAdapter(listOf())
        adapter.onItemEditClickListener = object : ConfigAdapter.OnItemClickListener {
            override fun onItemClick(field: IConfigField) {
               when (field) {
                   is IByteConfigField -> onByteConfigFieldEditClick(field)
                   is IBooleanConfigField -> onBooleanConfigFieldEditClick(field)
               }
            }
        }

        adapter.onItemClickListener = object : ConfigAdapter.OnItemClickListener {
            override fun onItemClick(field: IConfigField) {
                when (field) {
                    is ITriggerConfigField -> onTriggerConfigFieldClick(field)
                }
            }
        }

        rec.adapter = adapter
        rec.setHasFixedSize(true)

        rec.layoutManager = LinearLayoutManager(this.requireContext())
        rec.addItemDecoration(DividerItemDecoration(this.requireContext(), DividerItemDecoration.VERTICAL))

        // Inflate the layout for this fragment
        return binding.root
    }

    fun Fragment.setActivityTitle(title: String)
    {
        (activity as AppCompatActivity?)!!.supportActionBar?.title = title
    }

    private fun onByteConfigFieldEditClick(field: IByteConfigField) {
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

    private fun onBooleanConfigFieldEditClick(field: IBooleanConfigField) {
        MaterialAlertDialogBuilder(this@ConfigurationFragment.requireContext())
            .setTitle("Change configuration")
            .setMessage(requireContext().getString(R.string.config_bool_question, field.caption))
            .setPositiveButton("Enable") { _, _ ->
                GlobalScope.launch(Dispatchers.IO) {
                    field.setValue(true)
                    withContext(Dispatchers.Main) {
                        field.read()
                        showSnackbar(R.string.config_value_changed)
                    }
                }
            }
            .setNegativeButton("Disable") { _, _ ->
                GlobalScope.launch(Dispatchers.IO) {
                    field.setValue(false)
                    withContext(Dispatchers.Main) {
                        field.read()
                        showSnackbar(R.string.config_value_changed)
                    }
                }
            }
            .create()
            .show()
    }

    private fun onTriggerConfigFieldClick(field: ITriggerConfigField) {
        MaterialAlertDialogBuilder(this@ConfigurationFragment.requireContext())
            .setTitle(field.caption)
            .setMessage(requireContext().getString(R.string.config_trigger_field_question, field.caption))
            .setPositiveButton("Proceed") { _, _ ->
                GlobalScope.launch(Dispatchers.IO) {
                    field.trigger()
                    withContext(Dispatchers.Main) {
                        showSnackbar(requireContext().getString(R.string.config_trigger_field_triggered, field.caption))
                        delay(750)
                        configService?.readAllValues()
                    }
                }
            }
            .setNeutralButton("Cancel") { _, _ ->
            }
            .create()
            .show()
    }

    private fun showSnackbar(@StringRes resId: Int) {
        Snackbar.make(binding.root, resId, Snackbar.LENGTH_LONG)
            .show()
    }

    private fun showSnackbar(content: String) {
        Snackbar.make(binding.root, content, Snackbar.LENGTH_LONG)
            .show()
    }
}
