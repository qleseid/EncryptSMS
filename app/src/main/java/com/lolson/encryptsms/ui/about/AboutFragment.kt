package com.lolson.encryptsms.ui.about

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lolson.encryptsms.MainSharedViewModel
import com.lolson.encryptsms.R
import com.lolson.encryptsms.databinding.FragmentAboutBinding
import com.lolson.encryptsms.utility.LogMe

/**
 * The About [Fragment] in the nav drawer welcome activity.
 */
class AboutFragment : Fragment()
{
    //Logger
    private var l = LogMe()

    // Binding view
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    private val aboutViewModel: AboutViewModel by viewModels()
    private val aboutSharedViewModel: MainSharedViewModel by activityViewModels()

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
    ): View?
    {
        // Bindings
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        val rootView = binding.root

        // Application info
        binding.textviewSecond.text = aboutViewModel.appInfo

        // Console output
        aboutSharedViewModel.text.observe(viewLifecycleOwner, {
            binding.scrollView.text = it
            // Scroll to the bottom of the window as text come in
            binding.editTextTextMultiLine.fullScroll(View.FOCUS_DOWN)
        })

        // Watches the status of the secret keys for the contacts
        aboutSharedViewModel.contactKeysGood.observe(viewLifecycleOwner, {

            l.d("AA ABOUT STATUS: ${it.first} ${it.second} ${it.third}")
            binding.contactKeyTextViewStatus.text = if(it.first) "Good" else "Loading"
            binding.contactKeyTextViewKeyedAmount.text = it.second
            binding.totalContactsTextViewAmount.text = it.third
        })

        // Watches the main DH app key status
        aboutSharedViewModel.dhKeyGood.observe(viewLifecycleOwner, {
            binding.dhKeyTextViewStatus.text = if(it) "Good" else "Loading"
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
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = emailType
                putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
                putExtra(Intent.EXTRA_SUBJECT, subjectMessage)
                putExtra(Intent.EXTRA_TEXT, binding.scrollView.text)
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

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.app_bar_switch).isVisible = false
    }
}