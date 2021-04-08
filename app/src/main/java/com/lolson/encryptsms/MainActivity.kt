package com.lolson.encryptsms

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
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
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import com.lolson.encryptsms.data.model.Phone
import com.lolson.encryptsms.data.model.Sms
import com.lolson.encryptsms.databinding.ActivityMainBinding
import com.lolson.encryptsms.utility.LogMe

/**
 * Main activity with navigation drawer and action bar.
 */
open class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    //Binding View
    private lateinit var binding: ActivityMainBinding

    //Intent extra strings
    private val dialogError: String = "Invalid Number"
    private val appBarSetting: String = "Settings clicked"

    //Logger
    private var l = LogMe()

    private lateinit var  drawerLayout: DrawerLayout
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

        navView.setNavigationItemSelectedListener {
            navDrawerLogic(it)
        }

        // Observe title changes and update
        sharedViewModel.title.observe(this, {
            binding.appBarMain.toolbar.title = it
        })

        // Check Permissions
        checkSmsPermission()
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
            R.id.action_settings ->
            {
                Toast.makeText(applicationContext, appBarSetting, Toast.LENGTH_LONG).show()

                //Nav drawer is the settings page
                binding.drawerLayout.open()
                true
            }
            else                                                           -> super.onOptionsItemSelected(item)
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
                // Drawer toolbar visibility toggle listener
                val visSwitch = menuItem.actionView.findViewById(R.id.vis_switch_compat) as SwitchCompat

                visSwitch.setOnCheckedChangeListener { _, isChecked ->

                    if (isChecked)  supportActionBar?.show() else supportActionBar?.hide()
                }
                visSwitch.toggle()
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
    fun showAlertWithTextInput()
    {
        // Setup
        val textInputLayout = TextInputLayout(this)
        textInputLayout.setPadding(
            resources.getDimensionPixelOffset(R.dimen
                .dp_19),
            0,
            resources.getDimensionPixelOffset(R.dimen.dp_19),
            0
        )
        val input = EditText(this)
        textInputLayout.hint = "800-555-1212"
        textInputLayout.addView(input)

        val alert = AlertDialog.Builder(this)
            .setTitle("Enter Number")
            .setView(textInputLayout)
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()

        // Ensure keyboard is up and edit text field is selected
        alert.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        alert.show()
        input.requestFocus()

        // OK button logic for proper formatting
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

            if (Phone.pho().isCellPhoneNumber(input.text.toString())!!)
            {
                l.d("DIALOG BOX OK: GOOD NUMBER")
                sharedViewModel.setTitle(PhoneNumberUtils.formatNumber(input.text.toString()))
                sharedViewModel.tempSms = Sms.AppSmsShort()
                sharedViewModel.tempSms!!.address = input.text.toString()
                sharedViewModel.findThreadId()
                alert.dismiss()
            }
            else
            {
                l.d("DIALOG BOX OK: BAD NUMBER")
                input.requestFocus()
                input.highlightColor = Color.RED
                input.selectAll()
                val toast = Toast.makeText(applicationContext, dialogError, Toast.LENGTH_LONG)
                toast.setGravity(Gravity.TOP,0,0)
                toast.show()
            }
        }

        input.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            {
//                TODO("Nothing to implement")
            }

            // Builds the hint string as input arrives
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                if (start <= 2)
                {
                    textInputLayout.hint = "(${s?.toString()})"
                }
                if(start in 3..5)
                {
                    var temp = "(${s?.subSequence(0..2)})"
                    temp = "$temp ${s?.subSequence(3..start - before)}"
                    textInputLayout.hint = temp
                }
                if(start in 6..9)
                {
                    var temp = "(${s?.subSequence(0..2)}) ${s?.subSequence(3..5)}-"
                    temp = "$temp${s?.subSequence(6..start - before)}"
                    textInputLayout.hint = temp
                }
            }

            override fun afterTextChanged(s: Editable?)
            {
//                TODO("Nothing to implement")
            }
        })
    }
}