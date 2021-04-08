package com.lolson.encryptsms

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
import com.google.android.material.navigation.NavigationView
import com.lolson.encryptsms.utility.LogMe


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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_title)


        val navView: NavigationView = findViewById(R.id.title_nav_view)
        val navController = findNavController(R.id.title_nav_host_fragment)
        val intentFrag = intent.extras?.get(ARG_PARAM1).toString()
        Log.i(ContentValues.TAG, "intent frag: $intentFrag")

        navView.setupWithNavController(navController)
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
}