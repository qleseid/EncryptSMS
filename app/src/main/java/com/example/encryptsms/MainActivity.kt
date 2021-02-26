package com.example.encryptsms

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
    private val homeIntentExtra: String = "Home clicked appbar"
    private val aboutIntentExtra: String = "about_frag"

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

    @SuppressLint("RestrictedApi")
    override fun onCreate(
        savedInstanceState: Bundle?
    )
    {
        super.onCreate(savedInstanceState)
//        // Send context to factory
//        FactoryImpl().register(applicationContext, this.application)

        //Inflate all views and bind to variable
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Find and set the activity toolbar
//        toolbar = binding.appBarMain.toolbar
//        toolbar = findViewById(R.id.toolbar)
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

    override fun onBackPressed()
    {
        super.onBackPressed()
        l.d("Override for on back press")
    }

    override fun onCreateOptionsMenu(
        menu: Menu
    ): Boolean
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_action_bar, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean
    {
        l.d("Trying to pop the stack with up button")
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Handle conversation selection in appbar
    override fun onOptionsItemSelected(
        item: MenuItem
    ): Boolean
    {
        return when (item.itemId)
        {
            //Handles the home menu click event
            R.id.action_home ->
            {
                Toast.makeText(applicationContext, homeActionMessage, Toast.LENGTH_LONG).show()
//                val intent = Intent(this, TitleActivity::class.java).apply {
//                    putExtra(param1, homeIntentExtra)
//                }
//                startActivity(intent)
                sharedViewModel.refresh()
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
     * SEND MESSAGE BUTTON
     */
    fun sendMessage(
        view: View
    ){
        Toast.makeText(this, "Message Sent", Toast.LENGTH_SHORT).show()
    }
}