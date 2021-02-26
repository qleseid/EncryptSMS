package com.example.encryptsms.ui.welcome

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.encryptsms.MainActivity
import com.example.encryptsms.R
import com.example.encryptsms.utility.LogMe


/**
 * The welcome page [Fragment] for the WelcomeActivity.
 *
 * TODO:: Create a welcome splash screen instead of having the user click to start
 */
class WelcomeFragment : Fragment() {

    //Intent extra strings
    private val param1: String = "param1"
    private val mainIntentExtra: String = "welcome start clicked"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkSmsPermission()
    }

    private fun appLaunch()
    {
        //Create splash delay
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra(param1, mainIntentExtra)
            }
            startActivity(intent)
            activity?.finish()
        }, 2200)
    }

    /**
     * CHECK SMS PERMISSION
     */
    private fun checkSmsPermission()
    {
        if (activity?.applicationContext?.let {
                ContextCompat.checkSelfPermission(
                    it, Manifest.permission
                        .READ_SMS)
            }
            != PackageManager.PERMISSION_GRANTED)
        {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it, arrayOf(
                        Manifest.permission.READ_SMS,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_CONTACTS), 0)
            }
        }
        else
        {
            // Start the app on its way after getting permission
            appLaunch()

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray)
    {
        LogMe().i("WELCOME: On Request")

        // Make sure it's our original READ_CONTACTS request
        when (requestCode)
        {
            1    ->
            {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(activity, "Read SMS permission granted", Toast.LENGTH_SHORT).show()
                    // Start the app on its way after getting permission

                    appLaunch()
                }
                else
                {
                    Toast.makeText(activity, "Read SMS permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            else ->
            {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}