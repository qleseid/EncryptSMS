package com.example.encryptsms.ui.about

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.encryptsms.R

/**
 * The About [Fragment] in the nav drawered welcome activity.
 * TODO:: Move fragment to main activity when slash screen is complete in welcome activity.
 */
class AboutFragment : Fragment() {

    private lateinit var aboutViewModel: AboutViewModel

    //Send an email variables
    private val emailType: String = "message/rfc822"
    private val emailAddress: String = "lolson002@regis.edu"
    private val subjectMessage: String = "Balance Book Q&A"
    private val emailSignature: String = "Sent from Balance Book"

    //Log for when email is sent text
    private val sentEmailLog: String = "Send Email Clicked"

    //Chooser title string
    private val emailChooserText: String = "Email Developer"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        aboutViewModel =
            ViewModelProviders.of(this).get(AboutViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_about, container, false)
        val textView: TextView = root.findViewById(R.id.textview_second)
        aboutViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show the Up button in the action bar.
        //TODO:: This is still broken; no up caret, still hamburger icon.
        //Guess is the fragment doesn't set the action bar so changes need to be done
        //in the activity instead.
        setHasOptionsMenu(true)


        //Send email to me for comments or questions
        view.findViewById<Button>(R.id.button_second).setOnClickListener {
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

    @Override
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.action_home).isVisible = false
        menu.findItem(R.id.action_about).isVisible = false
        menu.findItem(R.id.action_no_vis).isVisible = false
    }
}