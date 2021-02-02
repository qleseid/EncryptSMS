package com.example.encryptsms.ui.about

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.encryptsms.TitleActivity

/**
 * A [Fragment] that belongs to mainActivity. It's original purpose was to link two
 * activities together when their 'about' option was selected. More of a proof of concept
 * and learning experience rather than a useful feature.
 *
 *
 *
 * UPDATE:: This [Fragment] is no longer used, can be safely
 * deleted
 */
class AboutLinkFragment : Fragment() {

    //Logger strings
    private val aboutLogCreation: String = "In about fragment intenting to title activity"

    //Toast message string
    private val aboutClickToast: String = "About Clicked in fragment"

    //Intent strings
    private val param1: String = "param1"
    private val sendingExtra: String = "about_frag"

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        //Send app to title activity and to the about fragment
        Log.i(ContentValues.TAG, aboutLogCreation)
        Toast.makeText(context, aboutClickToast, Toast.LENGTH_LONG).show()
        val intent = Intent(context, TitleActivity::class.java).apply {
            putExtra(param1, sendingExtra)
        }
        startActivity(intent)
        return null
    }
}