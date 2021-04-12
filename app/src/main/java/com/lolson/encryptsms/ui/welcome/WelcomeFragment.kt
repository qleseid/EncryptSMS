package com.lolson.encryptsms.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lolson.encryptsms.R


/**
 * The welcome page [Fragment] for the WelcomeActivity.
 *
 * TODO:: Create a welcome splash screen instead of having the user click to start
 */
class WelcomeFragment : Fragment()
{

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        // Hide the Floating Button in this Fragment
        activity?.findViewById<FloatingActionButton>(R.id.fab)?.hide()
    }
}