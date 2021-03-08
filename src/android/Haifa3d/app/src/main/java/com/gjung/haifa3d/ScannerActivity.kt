/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gjung.haifa3d

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ParcelUuid
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gjung.haifa3d.Uuids.HandDirectExecuteService
import com.gjung.haifa3d.adapter.DevicesAdapter
import com.gjung.haifa3d.adapter.DiscoveredBluetoothDevice
import com.example.haifa3d_ble_api.ble.BleService
import com.gjung.haifa3d.databinding.ActivityScannerBinding
import com.gjung.haifa3d.util.Utils
import com.gjung.haifa3d.viewmodel.ScannerStateLiveData
import com.gjung.haifa3d.viewmodel.ScannerViewModel
import com.google.android.material.snackbar.Snackbar
import no.nordicsemi.android.ble.livedata.state.ConnectionState


class ScannerActivity : BleActivity(), DevicesAdapter.OnItemClickListener {

    private val scannerViewModel: ScannerViewModel by viewModels()
    private lateinit var binding: ActivityScannerBinding
    private var isInFront = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)
        this.setTitle("Scanner")

        // Create view model containing utility methods for scanning
        scannerViewModel!!.scannerState.observe(
            this,
            Observer { state: ScannerStateLiveData ->
                startScan(
                    state
                )
            }
        )

        // Configure the recycler view
        val recyclerView = binding.recyclerViewBleDevices
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        val animator = recyclerView.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
        val adapter = DevicesAdapter(this, scannerViewModel!!.devices)
        adapter.setOnItemClickListener(this)
        recyclerView.adapter = adapter

        binding.noLocationPermission.actionGrantLocationPermission.setOnClickListener{ onGrantLocationPermissionClicked() }
        binding.noLocationPermission.actionPermissionSettings.setOnClickListener { onPermissionSettingsClicked() }
        binding.bluetoothOff.actionEnableBluetooth.setOnClickListener{ onEnableBluetoothClicked() }
        binding.noDevices.actionEnableLocation.setOnClickListener{ onEnableLocationClicked() }
        binding.demoButton.setOnClickListener {
            bleService?.mockConnect()
            val intent = Intent(this, ConnectedActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onRestart() {
        super.onRestart()
        clear()
    }

    override fun onServiceConnected() {
        // lets just hope the user cant click a device before the ble service is there
        // this seems reasonable though
        bleService!!.manager.state.observe(this, Observer {
            if (it.isConnected && isInFront) {
                val controlBlinkIntent = Intent(this, ConnectedActivity::class.java)
                startActivity(controlBlinkIntent)
                finish()
            }
        })
    }

    override fun onServiceDisconnected() {
        // if our service was killed it's pointless to stay alive
        finish()
    }

    override fun onStop() {
        if (bleService?.manager?.isConnected != true) {
            bleService?.stopService()
        }
        stopScan()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        isInFront = true
    }

    override fun onPause() {
        isInFront = false
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.scanner, menu)
        menu.findItem(R.id.filter_uuid).isChecked = scannerViewModel!!.isUuidFilterEnabled
        menu.findItem(R.id.filter_nearby).isChecked = scannerViewModel!!.isNearbyFilterEnabled
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filter_uuid -> {
                item.isChecked = !item.isChecked
                scannerViewModel!!.filterByUuid(item.isChecked)
                return true
            }
            R.id.filter_nearby -> {
                item.isChecked = !item.isChecked
                scannerViewModel!!.filterByDistance(item.isChecked)
                return true
            }
            R.id.menu_about -> {
                intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(device: DiscoveredBluetoothDevice) {
        if (device.scanResult.scanRecord?.serviceUuids?.contains(ParcelUuid(HandDirectExecuteService)) != true) {
            Snackbar.make(binding.root, R.string.not_a_haifa3d_device, Snackbar.LENGTH_SHORT)
                .show()
            return
        }
        Snackbar.make(binding.root, R.string.connecting, Snackbar.LENGTH_LONG)
            .show()
        if (bleService!!.manager.state.value?.state != ConnectionState.State.DISCONNECTED)
            return
        Api_obj.connect(device.device)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            scannerViewModel!!.refresh()
        }
    }

    private fun onEnableLocationClicked() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    private fun onEnableBluetoothClicked() {
        val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivity(enableIntent)
    }

    private fun onGrantLocationPermissionClicked() {
        Utils.markLocationPermissionRequested(this)
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_ACCESS_FINE_LOCATION
        )
    }

    private fun onPermissionSettingsClicked() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

    /**
     * Start scanning for Bluetooth devices or displays a message based on the scanner state.
     */
    private fun startScan(state: ScannerStateLiveData) {
        // First, check the Location permission. This is required on Marshmallow onwards in order
        // to scan for Bluetooth LE devices.
        if (Utils.isLocationPermissionsGranted(this)) {
            binding.noLocationPermission.root.visibility = View.GONE
            // Bluetooth must be enabled.
            if (state.isBluetoothEnabled) {
                binding.bluetoothOff.root.visibility = View.GONE
                // We are now OK to start scanning.
                scannerViewModel!!.startScan()
                binding.stateScanning.visibility = View.VISIBLE
                if (!state.hasRecords()) {
                    binding.noDevices.root.visibility = View.VISIBLE
                    if (!Utils.isLocationRequired(this) || Utils.isLocationEnabled(this)) {
                        binding.noLocationPermission.root.visibility = View.INVISIBLE
                    } else {
                        binding.noLocationPermission.root.visibility = View.VISIBLE
                    }
                } else {
                    binding.noDevices.root.visibility = View.GONE
                }
            } else {
                binding.bluetoothOff.root.visibility = View.VISIBLE
                binding.stateScanning.visibility = View.INVISIBLE
                binding.noDevices.root.visibility = View.GONE
                clear()
            }
        } else {
            binding.noLocationPermission.root.visibility = View.VISIBLE
            binding.bluetoothOff.root.visibility = View.GONE
            binding.stateScanning.visibility = View.INVISIBLE
            binding.noDevices.root.visibility = View.GONE
            val deniedForever: Boolean = Utils.isLocationPermissionDeniedForever(this)
            binding.noLocationPermission.actionGrantLocationPermission.visibility = if (deniedForever) View.GONE else View.VISIBLE
            binding.noLocationPermission.actionPermissionSettings.visibility = if (deniedForever) View.VISIBLE else View.GONE
        }
    }

    /**
     * stop scanning for bluetooth devices.
     */
    private fun stopScan() {
        scannerViewModel!!.stopScan()
    }

    /**
     * Clears the list of devices, which will notify the observer.
     */
    private fun clear() {
        scannerViewModel!!.devices.clear()
        scannerViewModel!!.scannerState.clearRecords()
    }

    companion object {
        private const val REQUEST_ACCESS_FINE_LOCATION = 1022 // random number
    }

}