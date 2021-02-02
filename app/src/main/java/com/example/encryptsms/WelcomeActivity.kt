package com.example.encryptsms

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView

//private var ABOUT_FRAG = "about_frag"
private var ARG_PARAM1 = "param1"

/**
 * Title activity is the apps splash screen during startup.
 * It once housed an entire activity with nav controller but
 * has since moved everything into the MainActivity.
 * The commented code is left over from that and shouldn't be worried about.
 * Everything that needs to be known about this activity is housed in the
 * WelcomeFragment
 */
class TitleActivity : AppCompatActivity() {

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

        val navView: NavigationView = findViewById(R.id.title_nav_view)
        val navController = findNavController(R.id.title_nav_host_fragment)
        val intentFrag = intent.extras?.get(ARG_PARAM1).toString()
        Log.i(ContentValues.TAG,"intent frag: $intentFrag")

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
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            //Handles the home menu click event
//            android.R.id.home -> {
//                Toast.makeText(applicationContext, "Home Clicked in About", Toast.LENGTH_LONG).show()
//                onBackPressed()
//                true
//            }
//            else -> return super.onOptionsItemSelected(item)
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