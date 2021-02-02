package com.example.encryptsms

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.ui.*
import com.example.encryptsms.utility.LogMe

/**
 * Main activity with navigation drawer and action bar.
 */
open class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

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
    private lateinit var toolbar: Toolbar
    private lateinit var navView: NavigationView
    private lateinit var navController: NavController

        @SuppressLint("RestrictedApi")
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Find and set the activity toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_folder), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


    }

    override fun onBackPressed() {
        super.onBackPressed()
        l.d("Override for on back press")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_action_bar, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        l.d( "Trying to pop the stack with up button")
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Handle item selection in appbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            //Handles the home menu click event
            R.id.action_home -> {
                Toast.makeText(applicationContext, homeActionMessage, Toast.LENGTH_LONG).show()
                val intent = Intent(this, TitleActivity::class.java).apply {
                    putExtra(param1, homeIntentExtra)
                }
                startActivity(intent)
                true
            }
            //Handles the about menu click event
            R.id.action_about -> {
                Toast.makeText(applicationContext, aboutActionMessage, Toast.LENGTH_LONG).show()
                findNavController(R.id.nav_host_fragment).navigate(R.id.nav_about)
                true
            }
            //Handles the turn of action bar menu click event
            R.id.action_no_vis -> {
                Toast.makeText(applicationContext, hideActionMessage, Toast.LENGTH_LONG).show()
                supportActionBar?.hide()
                true
            }
            else -> super.onOptionsItemSelected(item);
        }
    }

    //Connected to the navigation drawer item to show action bar if hidden
    fun showActionBar(item: MenuItem){
        l.d( showBarLog + item.itemId)
        supportActionBar?.show()
    }
}