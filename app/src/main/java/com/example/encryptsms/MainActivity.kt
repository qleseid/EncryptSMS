package com.example.encryptsms

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.encryptsms.databinding.ActivityMainBinding
import com.example.encryptsms.utility.LogMe
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout

/**
 * Main activity with navigation drawer and action bar.
 */
open class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    //Binding View
    private lateinit var binding: ActivityMainBinding

    //Button click action variables
    private val homeActionMessage: String = "Home Clicked"
    private val aboutActionMessage: String = "About Clicked"
    private val hideActionMessage: String = "Hide Action Bar Clicked"

    //Intent extra strings
    private val param1: String = "param1"
    private val appBarSwitchOn: String = "Switch On appbar"
    private val appBarSwitchOff: String = "Switch Off appbar"
    private val appBarSetting: String = "Settings clicked"

    //Logger
    private var l = LogMe()

    //Logging string
    private val showBarLog: String = "Show Action Bar: "

    private lateinit var  drawerLayout: DrawerLayout
//    private lateinit var toolbar: Toolbar
    private lateinit var navView: NavigationView
    private lateinit var navController: NavController

    private val sharedViewModel: MainSharedViewModel by viewModels()

    // Activity instance to use with receivers
    private lateinit var inst: MainActivity

    open fun instance(): MainActivity?
    {
        return inst
    }

    override fun onStart()
    {
        super.onStart()
        inst = this
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    )
    {
        super.onCreate(savedInstanceState)

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
        navView.setupWithNavController(navController)

        // Check Permissions
        checkSmsPermission()
    }

    override fun onCreateOptionsMenu(
        menu: Menu
    ): Boolean
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_action_bar, menu)

        // Encryption toggle listener
        val rela = menu.findItem(R.id.app_bar_switch)?.actionView as RelativeLayout
        val switch = rela.findViewById(R.id.switch_compat) as SwitchCompat

        switch.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked)
            {
                sharedViewModel.setEncryptedToggle(true)
            }
            else
            {
                sharedViewModel.setEncryptedToggle(false)
            }
        }


        // Look for switch changes between views to maintain switch position
        sharedViewModel.encSwitch.observe(this, {
            switch.isChecked = it
        })

        return true
    }

    override fun onSupportNavigateUp(): Boolean
    {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Handle conversation selection in appbar
    override fun onOptionsItemSelected(
        item: MenuItem
    ): Boolean
    {
        super.onOptionsItemSelected(item)

        return when (item.itemId)
        {
            //Handles the home menu click event
            R.id.action_home ->
            {
                Toast.makeText(applicationContext, homeActionMessage, Toast.LENGTH_LONG).show()
                true
            }
            //Handles the about menu click event
            R.id.action_about ->
            {
                Toast.makeText(applicationContext, aboutActionMessage, Toast.LENGTH_LONG).show()
                findNavController(R.id.nav_host_fragment).navigate(R.id.nav_about)
                true
            }
            //Handles the turn of action bar menu click event
            R.id.action_no_vis ->
            {
                Toast.makeText(applicationContext, hideActionMessage, Toast.LENGTH_LONG).show()
                supportActionBar?.hide()
                true
            }
            R.id.action_settings ->
            {
                Toast.makeText(applicationContext, appBarSetting, Toast.LENGTH_LONG).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //Connected to the navigation drawer conversation to show action bar if hidden
    fun showActionBar(
        item: MenuItem
    )
    {
        l.d(showBarLog + item.itemId)
        supportActionBar?.show()
    }

    /**
     * CHECK SMS PERMISSION
     */
    private fun checkSmsPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_SMS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_CONTACTS), 0)
        }
    }

    /**
     * ALERT POP DIALOG BOX
     */
    fun showAlertWithTextInput() {
        val textInputLayout = TextInputLayout(this)
        textInputLayout.setPadding(
            resources.getDimensionPixelOffset(R.dimen.dp_19),
            0,
            resources.getDimensionPixelOffset(R.dimen.dp_19),
            0
        )
        val input = EditText(this)
        textInputLayout.hint = "Recipient"
        textInputLayout.addView(input)

        val alert = AlertDialog.Builder(this)
            .setTitle("Enter Number")
            .setView(textInputLayout)
            .setMessage("Please enter phone number")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                binding.appBarMain.toolbar.title = PhoneNumberUtils.formatNumber(input.text.toString())
            sharedViewModel.tempSms.address = input.text.toString()
            sharedViewModel.findThreadId()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }.create()

        alert.show()
    }
}