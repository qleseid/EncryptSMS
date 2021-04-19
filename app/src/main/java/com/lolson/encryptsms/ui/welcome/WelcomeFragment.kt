package com.lolson.encryptsms.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lolson.encryptsms.R
import com.lolson.encryptsms.databinding.FragmentWelcomeBinding


/**
 * The welcome page [Fragment] for the WelcomeActivity.
 *
 * TODO:: Create a welcome splash screen instead of having the user click to start
 */
class WelcomeFragment : Fragment()
{
    // Binding view
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        // Bindings
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)

        binding.progressBar.animate()
        activity?.window?.setFlags(1024, 1024)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        // Hide the Floating Button in this Fragment
        activity?.findViewById<FloatingActionButton>(R.id.fab)?.hide()
    }

    override fun onPause()
    {
        super.onPause()
        // Fall from fullscreen
        activity?.window?.clearFlags(1024)
    }
}