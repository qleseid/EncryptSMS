package com.lolson.encryptsms

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.navigation.NavigationView
import com.lolson.encryptsms.data.livedata.ReceiveNewSms
import com.lolson.encryptsms.databinding.ActivityMainBinding
import com.lolson.encryptsms.utility.AlertDialogs
import com.lolson.encryptsms.utility.LogMe

/**
 * Main activity with navigation drawer and action bar.
 */
@Suppress("DEPRECATION")
open class MainActivity : AppCompatActivity()
{
    // Activity global used in Conversation Fragment for sending the keys
//    var alertResult = -14

    private lateinit var appBarConfiguration: AppBarConfiguration

    //Binding View
    private lateinit var binding: ActivityMainBinding
    private lateinit var visSwitch: SwitchCompat

    //Intent extra strings
    private val appBarSetting: String = "Settings clicked"

    //Logger
    private var l = LogMe()

    private lateinit var  drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var navController: NavController

    private val sharedViewModel: MainSharedViewModel by viewModels()

    override fun onCreate(
        savedInstanceState: Bundle?
    )
    {
        super.onCreate(savedInstanceState)
        l.d("MA:: ON CREATE")

        //Inflate all views and bind to variable
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Find and set the activity toolbar
        setSupportActionBar(binding.appBarMain.toolbar)

        drawerLayout = binding.drawerLayout
        navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_threads), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setNavigationItemSelectedListener {
            navDrawerLogic(it)
        }

        // Observe title changes and update
        sharedViewModel.title.observe(this, {
            binding.appBarMain.toolbar.title = it
        })

        // Observe data changes from Broadcast Receiver
        ReceiveNewSms.get().observe(this, {

            l.d("MA: SMS RECEIVER OBSERVER: $it")
            if (it)
            {
                sharedViewModel.refresh(0)
                ReceiveNewSms.set(false)
            }
        })

        // Handle alert dialog launches from all over the fragments within this activity
        sharedViewModel.alert.observe(this, {
            l.d("MA:: ALERT OBSERVER $it")
            AlertDialogs(this, sharedViewModel).alertLauncher(it.first, it.second)
        })

        // Drawer toolbar visibility toggle listener
        visSwitch = binding.navView.menu[1]
            .actionView.findViewById(R.id.vis_switch_compat) as SwitchCompat

        // Hide app bar during startup
        supportActionBar?.hide()

        // Check Permissions
        checkSmsPermission()
    }

    override fun onPause()
    {
        super.onPause()
        l.d("MA:: ON PAUSE")
    }

    override fun onDestroy()
    {
        super.onDestroy()
        l.d("MA:: ON DESTROY")
    }

    /**
     * APP BAR LOGIC
     */
    override fun onCreateOptionsMenu(
        menu: Menu
    ): Boolean
    {
        // Inflate the menu; this adds keys to the action bar if it is present.
        menuInflater.inflate(R.menu.main_action_bar, menu)

        // Encryption toggle listener
        val rela = menu.findItem(R.id.app_bar_switch)?.actionView as RelativeLayout
        val switch = rela.findViewById(R.id.encrypt_switch_compat) as SwitchCompat

        switch.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked != sharedViewModel.encSwitch.value)
            {
                sharedViewModel.setEncryptedToggle(isChecked)
            }
        }

        // Look for switch changes between views to maintain switch position
        sharedViewModel.encSwitch.observe(this, {
            switch.isChecked = it
        })

        return true
    }

    override fun onSupportNavigateUp()
            : Boolean
    {
//        navController.navigate(R.id.nav_threads)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /**
     * ACTION BAR ITEM LOGIC
     */
    override fun onOptionsItemSelected(
        item: MenuItem
    ): Boolean
    {
        super.onOptionsItemSelected(item)

        return when (item.itemId)
        {
            //Handles the home menu click event
            R.id.action_settings ->
            {
                sharedViewModel.alertHelper(3, appBarSetting)
//                Toast.makeText(applicationContext, appBarSetting, Toast.LENGTH_LONG).show()

                //Nav drawer is the settings page
                binding.drawerLayout.open()
                true
            }
            R.id.action_invite ->
            {
                // TODO:: have this check invites first
                sharedViewModel.alertHelper(0, null)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * NAVIGATION DRAWER LOGIC
     */
    private fun navDrawerLogic(
        menuItem: MenuItem
    ): Boolean
    {
        return when (menuItem.itemId)
        {
            R.id.nav_vis     -> // App Bar visibility toggle
            {
                visSwitch.toggle()
                if (visSwitch.isChecked)  supportActionBar?.show() else supportActionBar?.hide()
                true
            }
            R.id.nav_threads -> // Inbox
            {
                navController.navigate(R.id.nav_threads)
                // Close drawer after click
                binding.drawerLayout.close()
                true
            }
            R.id.nav_about   -> // About
            {
                navController.navigate(R.id.nav_about)
                // Close drawer after click
                binding.drawerLayout.close()
                true
            }
            else  -> false
        }
    }

    /**
     * LAUNCH APP
     */
    private fun appLaunch()
    {
        // Launch background data gathering
        sharedViewModel.getAllThreads()

        //Create splash delay
        Handler(Looper.getMainLooper()).postDelayed({
            navController.navigate(R.id.nav_threads)
            // Show app bar after startup
            supportActionBar?.show()
        }, 1150)
    }

    /**
     * CHECK SMS PERMISSION
     */
    private fun checkSmsPermission()
    {
        if (this.let {
                ContextCompat.checkSelfPermission(
                    it, Manifest.permission
                        .READ_SMS)
            }
            != PackageManager.PERMISSION_GRANTED)
        {
            this.let {
                ActivityCompat.requestPermissions(
                    it, arrayOf(
                        Manifest.permission.READ_SMS,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_CONTACTS), 0)
            }
        }
        else
        {
            LogMe().i("WELCOME FRAGMENT: APP LAUNCH")

            // Start the app on its way after getting permission
            appLaunch()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray)
    {
        LogMe().i("WELCOME: ACTIVITY")
        // Make sure it's our original READ_CONTACTS request
        when (requestCode)
        {
            1    ->
            {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, "Read SMS permission granted", Toast.LENGTH_SHORT).show()

                    // Start the app on its way after getting permission
                    appLaunch()
                }
                else
                {
                    Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            else ->
            {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)

                // Start the app on its way after getting permission
                appLaunch()
            }
        }
    }
}