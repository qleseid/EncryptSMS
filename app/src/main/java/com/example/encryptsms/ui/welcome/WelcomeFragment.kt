package com.example.encryptsms.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.encryptsms.MainActivity
import com.example.encryptsms.R


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

        //Create splash delay
        Handler().postDelayed({
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra(param1, mainIntentExtra)
            }
            startActivity(intent)
            activity?.finish()
        }, 2200)
    }
}