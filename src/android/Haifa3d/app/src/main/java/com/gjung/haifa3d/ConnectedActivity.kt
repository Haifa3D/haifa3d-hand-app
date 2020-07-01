package com.gjung.haifa3d

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.gjung.haifa3d.databinding.ActivityConnectedBinding

class ConnectedActivity : BleActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityConnectedBinding
    private var isInFront = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appbar.toolbar)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_status, R.id.nav_livecontrol, R.id.nav_presets, R.id.nav_about, R.id.nav_configuration
            ), binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        binding.disconnectButton.setOnClickListener { disconnect() }
    }

    private fun disconnect() {
        bleService?.manager?.disconnect()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.connected, menu)
        return true
    }

    override fun onServiceConnected() {
        if (bleService?.manager?.isConnected != true) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        bleService?.manager?.state?.observe(this, Observer {
            if (it.isConnected)
                return@Observer
            if (isInFront) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            finish()
        })
    }

    override fun onServiceDisconnected() {
        // if our service was killed it's pointless to stay alive
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        isInFront = true
        if (bleService?.manager?.isConnected == false) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onPause() {
        isInFront = false
        super.onPause()
    }
}
