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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.widget.PopupMenu
import kotlinx.android.synthetic.main.activity_connected.*
import kotlinx.android.synthetic.main.app_bar_connected.*


class ConnectedActivity : BleActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityConnectedBinding

    private var isInFront = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appbar.toolbar)
        val navView: NavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
            ), binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        binding.disconnectButton.setOnClickListener { disconnect() }

        val bottomNavView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)


        toolbar_save.setOnClickListener {
            showPopup(toolbar_save)
        }
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.menu_main)

        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {

                R.id.nav_about -> {
                    Toast.makeText(this@ConnectedActivity, item.title, Toast.LENGTH_SHORT).show()
                    findNavController(R.id.nav_host_fragment).navigate(R.id.nav_about)
                }
                R.id.disconnect_button -> {
                    Toast.makeText(this@ConnectedActivity, item.title, Toast.LENGTH_SHORT).show()
                    disconnect()
                }
            }

            true
        })

        popup.show()
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


    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.nav_home)
                }
                R.id.nav_livecontrol -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.nav_livecontrol)
                }
                R.id.nav_presets -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.nav_presets)
                }
                R.id.nav_configuration -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.nav_configuration)
                }

            }
            false
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
