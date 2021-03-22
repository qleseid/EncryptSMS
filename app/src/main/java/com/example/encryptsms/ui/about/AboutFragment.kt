package com.example.encryptsms.ui.about

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.encryptsms.R
import com.example.encryptsms.databinding.FragmentAboutBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * The About [Fragment] in the nav drawer welcome activity.
 */
class AboutFragment : Fragment() {

    // Binding view
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    private val aboutViewModel: AboutViewModel by viewModels()

    //Send an email variables
    private val emailType: String = "message/rfc822"
    private val emailAddress: String = "lolson002@regis.edu"
    private val subjectMessage: String = "Encrypt SMS Q&A"
    private val emailSignature: String = "Sent from Encrypt SMS"

    //Log for when email is sent text
    private val sentEmailLog: String = "Send Email Clicked"

    //Chooser title string
    private val emailChooserText: String = "Email Developer"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Bindings
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        val rootView = binding.root

        aboutViewModel.text.observe(viewLifecycleOwner, Observer {
            binding.textviewSecond.text = it
        })
        return rootView
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        // Hide the Floating Button in this Fragment
        activity?.findViewById<FloatingActionButton>(R.id.fab)?.hide()

        //Send email to me for comments or questions
        binding.buttonSecond.setOnClickListener {
//            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = emailType
                putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
                putExtra(Intent.EXTRA_SUBJECT, subjectMessage)
                putExtra(Intent.EXTRA_TEXT, emailSignature)
            }

            Log.i(ContentValues.TAG, sentEmailLog)

            // Create intent to show chooser
            val chooser = Intent.createChooser(intent, emailChooserText)

            // Verify the intent will resolve to at least one activity
            if (intent.resolveActivity(activity?.packageManager!!) != null) {
                startActivity(chooser)
            }
        }
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }

    @Override
    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {
        super.onCreateOptionsMenu(menu, inflater)
//        menu.findItem(R.id.action_home).isVisible = false
//        menu.findItem(R.id.action_about).isVisible = false
//        menu.findItem(R.id.action_no_vis).isVisible = false
    }
}