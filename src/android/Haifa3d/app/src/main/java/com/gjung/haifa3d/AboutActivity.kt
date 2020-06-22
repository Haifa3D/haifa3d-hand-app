package com.gjung.haifa3d

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.gjung.haifa3d.adapter.DiscoveredBluetoothDevice
import com.google.android.material.navigation.NavigationView

class AboutActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var device: DiscoveredBluetoothDevice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navController = findNavController(R.id.nav_host_fragment)

        navController.setGraph(R.navigation.about_navigation)

        // we don't have top level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        if (!navController.navigateUp(appBarConfiguration))
            onBackPressed()
        return true
    }
}
