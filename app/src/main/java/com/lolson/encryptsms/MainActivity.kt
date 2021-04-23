package com.lolson.encryptsms

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.view.Menu
import android.view.MenuItem
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.viewModels
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
import com.google.android.material.navigation.NavigationView
import com.lolson.encryptsms.data.livedata.ReceiveNewSms
import com.lolson.encryptsms.databinding.ActivityMainBinding
import com.lolson.encryptsms.utility.AlertDialogs
import com.lolson.encryptsms.utility.LogMe
import com.lolson.encryptsms.utility.NotificationUtils

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

    // View Model
    private val sharedViewModel: MainSharedViewModel by viewModels()

    // Saved state
    private var saveState: Bundle? = null

    override fun onCreate(
        savedInstanceState: Bundle?
    )
    {
        super.onCreate(savedInstanceState)
        saveState = savedInstanceState
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
                NotificationUtils(this).getManager().cancelAll()
            }
        })

        // Handle alert dialog launches from all over the fragments within this activity
        sharedViewModel.alert.observe(this, {
            l.d("MA:: ALERT OBSERVER $it")
            AlertDialogs(this, sharedViewModel, binding.appBarMain.myCoordinatorLayout)
                .alertLauncher(it.first, it.second, it.third)
        })

        // Drawer toolbar visibility toggle listener
        visSwitch = binding.navView.menu[1]
            .actionView.findViewById(R.id.vis_switch_compat)

        // If first start, this will be null
        if (savedInstanceState?.isEmpty == null)
        {
            // Hide app bar during startup
            supportActionBar?.hide()

            // Check app is default
            defaultApp()
        }
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
//        sharedViewModel.cleanUpMessages()
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
                sharedViewModel.alertHelper(3, appBarSetting, null)
//                Toast.makeText(applicationContext, appBarSetting, Toast.LENGTH_LONG).show()

                //Nav drawer is the settings page
                binding.drawerLayout.open()
                true
            }
            R.id.action_invite ->
            {
                // TODO:: have this check invites first
                sharedViewModel.alertHelper(0, null, null)
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
     * CHECK DEFAULT APP
     */
    private fun defaultApp()
    {
        if (Telephony.Sms.getDefaultSmsPackage(this) != this.packageName)
        {
            LogMe().i("MA:: DEFAULT APP CHECK")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                val roleManager = getSystemService(RoleManager::class.java) as RoleManager
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                startActivityForResult(intent, 4216)
            }
            else
            {
                val smsIntent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                smsIntent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, this.packageName)
                startActivityForResult(smsIntent, 4217)
            }
        }
        else
        {
            LogMe().i("MA:: DEFAULT APP CHECK ELSE")

            // Check Permissions
            checkSmsPermission()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 4216 || requestCode == 4217)
//            if (requestCode.toString() == "4216")
        {
            LogMe().i("MA:: ON ACTIVITY RESULT: $requestCode")
            checkSmsPermission()
        }
        else
        {
            LogMe().i("MA:: ON ACTIVITY RESULT ELSE: $requestCode -:- $requestCode -:- $data")
        }
    }

    /**
     * LAUNCH APP
     */
    private fun appLaunch()
    {
        // Launch background data gathering
        if (saveState?.isEmpty == null)
        {
            sharedViewModel.getAllThreads()
        }

        // Create splash delay
        Handler(Looper.getMainLooper()).postDelayed({
            navController.navigate(R.id.nav_threads)
            // Show app bar after startup
            supportActionBar?.show()
        }, 850)

        // Allow app to start normal and then move to Conversation
        Handler(Looper.getMainLooper()).postDelayed({
        intent.extras?.getString("address")?.let {
            // Nav to Conversation Fragment and adds in the selected thread as arg
            val bundle = Bundle().apply {
                putSerializable("notify", it)
            }
            navController.navigate(R.id.nav_conversations, bundle)
        }
        }, 990)
    }

    /**
     * CHECK SMS PERMISSION
     */
    private fun checkSmsPermission()
    {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_SMS
            ) ==  PackageManager.PERMISSION_GRANTED)
        {
            LogMe().i("MA:: CHECK PERMISSION: APP LAUNCH")
            appLaunch()
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS))
                {
                    // TODO:: Create a rationale page to explain stuff
                    LogMe().i("MA:: CHECK PERMISSION SHOW RATIONALE")
                    smsRequests()
                }
                else
                {
                    LogMe().i("MA:: CHECK PERMISSION SHOW RATIONALE ELSE")
                    smsRequests()
                }
            }
            else
            {
                LogMe().i("MA:: CHECK PERMISSION ELSE")
                smsRequests()
            }
        }
    }

    private fun smsRequests()
    {
        this.let {
            ActivityCompat.requestPermissions(
                it, arrayOf(
                    Manifest.permission.READ_SMS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_CONTACTS), 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray)
    {
        LogMe().i("MA:: REQUEST PERMISSION RESULT: $requestCode ${grantResults.toList()}")
        // Make sure it's our original READ_CONTACTS request
        when (requestCode)
        {
            0    ->
            {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                    Toast.makeText(this, "Read SMS permission granted", Toast.LENGTH_LONG).show()

                    // Start the app on its way after getting permission
                    appLaunch()
                }
                else
                {
                    Toast.makeText(this, "Need Read SMS permission! Change in Phone Settings", Toast
                        .LENGTH_LONG).show()
//                    exitProcess(7)
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