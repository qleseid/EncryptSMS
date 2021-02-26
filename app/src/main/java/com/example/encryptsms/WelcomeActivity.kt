package com.example.encryptsms

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.encryptsms.utility.LogMe
import com.google.android.material.navigation.NavigationView


//private var ABOUT_FRAG = "about_frag"
private var ARG_PARAM1 = "param1"

private var moveOn = false

/**
 * Title activity is the apps splash screen during startup.
 * It once housed an entire activity with nav controller but
 * has since moved everything into the MainActivity.
 * The commented code is left over from that and shouldn't be worried about.
 * Everything that needs to be known about this activity is housed in the
 * WelcomeFragment
 */
class TitleActivity : AppCompatActivity() {

    //Intent extra strings
    private val param1: String = "param1"
    private val mainIntentExtra: String = "welcome start clicked"
//    private lateinit var appBarConfiguration: AppBarConfiguration
//    private lateinit var mActionBar: ActionBar
//    private lateinit var drawerLayout: DrawerLayout
//    private lateinit var mDrawerToggle: ActionBarDrawerToggle
//
//    private var mBarListener: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_title)
//        setSupportActionBar(findViewById(R.id.toolbar))
//        mActionBar = supportActionBar!!


        // Check Permissions
//        checkSmsPermission()

        //TODO: working on getting Action Bar working properly between the fragments
//        drawerLayout = findViewById(R.id.title_drawer_layout)
//        mDrawerToggle = ActionBarDrawerToggle(
//        this,
//        drawerLayout,
//        findViewById(R.id.toolbar),
//        R.string.navigation_drawer_open,
//        R.string.navigation_drawer_close)

//        drawerLayout.addDrawerListener(mDrawerToggle)
//        mDrawerToggle.syncState()

//        while (!moveOn)
//        {
//            val temp = "waiting"
//            // Check Permissions
//            checkSmsPermission()
//            this.lifecycleScope.launch(Dispatchers.Default) {
//            //delay(1000)
//
//                LogMe().i("WELCOME: $temp")
//            }
//        }

        val navView: NavigationView = findViewById(R.id.title_nav_view)
        val navController = findNavController(R.id.title_nav_host_fragment)
        val intentFrag = intent.extras?.get(ARG_PARAM1).toString()
        Log.i(ContentValues.TAG, "intent frag: $intentFrag")

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        appBarConfiguration = AppBarConfiguration(setOf(
//            R.id.nav_first, R.id.nav_second), drawerLayout)
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

//        if (savedInstanceState != null) {
////            resolveUpButtonWithFragStack()
//        }else{
//            //Check intent arguments for specific fragment request
//            when (intentFrag) {
//                ABOUT_FRAG -> {
//                    Log.i(ContentValues.TAG, "Second Fragment created from intent")
////                    mDrawerToggle.isDrawerIndicatorEnabled = false
////                    mActionBar.setDisplayHomeAsUpEnabled(true)
////                    appBarConfiguration.drawerLayout.setDrawerIndicatorEnable(false)
//                    navController.navigate(R.id.nav_second)
//                }
//                else -> Log.i(ContentValues.TAG, "First Fragment created from intent in ELSE")
//            }
//        }
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
        else
        {
            // Start the app on its way after getting permission
            moveOn = true
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

                    moveOn = true
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
                moveOn = true
                appLaunch()

            }
        }
    }


    private fun appLaunch()
    {
        //Create splash delay
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(param1, mainIntentExtra)
            }
            startActivity(intent)
            this.finish()
        }, 2200)
    }

    /*
    private fun resolveUpButtonWithFragStack(){
        showUpButton(supportFragmentManager?.backStackEntryCount > 0)
    }
    private fun showUpButton(show: Boolean){
        if (show){
            mDrawerToggle.isDrawerIndicatorEnabled = false
            mActionBar.setDisplayHomeAsUpEnabled(true)

            if (!mBarListener){
                mDrawerToggle.setToolbarNavigationClickListener {
                        onBackPressed()
                }
                mBarListener = true
            }
        }else{
            mActionBar.setDisplayHomeAsUpEnabled(false)
            mDrawerToggle.isDrawerIndicatorEnabled = true
            mDrawerToggle.setToolbarNavigationClickListener { null }
            mBarListener = false
        }
    }

*/

//
//
//    override fun onOptionsItemSelected(conversation: MenuItem): Boolean {
//        return when (conversation.itemId) {
//            //Handles the home menu click event
//            android.R.id.home -> {
//                Toast.makeText(applicationContext, "Home Clicked in About", Toast.LENGTH_LONG).show()
//                onBackPressed()
//                true
//            }
//            else -> return super.onOptionsItemSelected(conversation)
//        }
//    }
////    override fun onCreateOptionsMenu(menu: Menu): Boolean {
////        // Inflate the menu; this adds items to the action bar if it is present.
////        menuInflater.inflate(R.menu.main, menu)
////        return true
////    }
//
//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.title_nav_host_fragment)
//        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
//    }
}