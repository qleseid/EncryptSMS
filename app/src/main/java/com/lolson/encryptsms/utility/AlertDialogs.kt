package com.lolson.encryptsms.utility

import android.content.Context
import android.graphics.Color
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.lolson.encryptsms.MainSharedViewModel
import com.lolson.encryptsms.R
import com.lolson.encryptsms.data.model.Phone
import com.lolson.encryptsms.data.model.Sms

class AlertDialogs(_context: Context, _viewModel: MainSharedViewModel, _findViewById: View)
{
    private val mContext = _context
    private val mVM = _viewModel
    private val mView = _findViewById

    //Logger
    private var l = LogMe()

    // Toast strings
    private val dialogError: String = "Invalid Number"

    /**
     * HANDLER FUNCTION FOR ALL ALERTS
     * Lets me expand the list and shorten code in Activity
     */
    fun alertLauncher(
        selection: Int,
        message: String?,
        command: String?
    )
    {
        l.d("AD:: ALERT LAUNCHER: $selection : $command")
        when(selection)
        {
            0 ->
            {
                inviteSendAlert()
                mVM.alertHelper(-1, message, command)
            }
            1 ->
            {
                snackMessages(message, command)
                mVM.alertHelper(-11, message, command)
            }
            2 ->
            {
                numberInputAlert()
                mVM.alertHelper(-22, message, command)
            }
            3 ->
            {
                toastMessages(message)
                mVM.alertHelper(-33, message, command)
            }
            else -> { }
        }
    }

    /**
     * TOAST MESSAGE BUILDER
     */
    private fun toastMessages(
        message: String?
    )
    {
        Toast.makeText(mContext.applicationContext, message, Toast.LENGTH_LONG).show()
    }

    /**
     * SNACK BAR BUILDER
     */
    private fun snackMessages(
        message: String?,
        command: String?
    )
    {
        val snack = Snackbar.make(mView,"" + message, Snackbar.LENGTH_LONG)
        val view = snack.view
        val params = view.layoutParams as CoordinatorLayout.LayoutParams
        params.gravity = Gravity.TOP
        params.height = 156
        view.layoutParams = params
        snack.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE

        command?.let {cmd ->
            snack.setAction(cmd) {
                when (cmd)
                {
                    "Delete" ->
                    {
                        mVM.deleteSmsMessage()
                        l.d("AD:: SNACK BAR COMMAND Delete: $cmd")
                    }
                    "Undo"   ->
                    {

                    }
                    "Bacon"  ->
                    {

                    }
                    else     ->
                    {
                        l.d("AD:: SNACK BAR COMMAND ELSE: $cmd")
                    }
                }
            }
        }
        snack.show()
    }

    /**
     * NUMBER INPUT ALERT
     * Alert dialog for inputting a new number in app bar title
     */
    private fun numberInputAlert()
    {
        // Setup
        val textInputLayout = TextInputLayout(mContext)
        textInputLayout.setPadding(
            mContext.resources.getDimensionPixelOffset(
                R.dimen
                .dp_19),
            0,
            mContext.resources.getDimensionPixelOffset(R.dimen.dp_19),
            0
        )
        val input = EditText(mContext)
        textInputLayout.hint = "800-555-1212"
        textInputLayout.addView(input)

        val alert = AlertDialog.Builder(mContext)
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
                l.d("AD:: DIALOG BOX OK: GOOD NUMBER")
                mVM.setTitle(PhoneNumberUtils.formatNumber(input.text.toString()))
                mVM.tempSms = Sms.AppSmsShort()
                mVM.tempSms!!.address = input.text.toString()
                mVM.findThreadId()
                alert.dismiss()
            }
            else
            {
                l.d("AD:: DIALOG BOX OK: BAD NUMBER")
                input.requestFocus()
                input.highlightColor = Color.RED
                toastMessages(dialogError)
            }
        }

        input.addTextChangedListener(object : TextWatcher
        {
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

    /**
     * ALERT POP DIALOG BOX FOR INVITE ACTION
     */
    private fun inviteSendAlert()
    {
        val alert = AlertDialog.Builder(mContext)
            .setTitle("Send Invite")
            .setMessage("This contact doesn't have a saved key.\n\n" +
                    "Send an invite?")
            .setPositiveButton("Send Invite"){ dialog, _ ->
                mVM.sendSmsInviteMessage()
                dialog.cancel()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
//                mVM.setEncryptedToggle(false)
                dialog.cancel()
            }
            .create()
        alert.show()
    }

    /**
     * ALERT POP DIALOG BOX FOR INVITE ACTION
     */
    private fun appNotDefault()
    {
        val alert = AlertDialog.Builder(mContext)
            .setTitle("App not default")
            .setMessage("Some functionality is limited when not default.\n\n" +
                    "Set app as default?")
            .setPositiveButton("Pick Default"){ dialog, _ ->
                mVM.sendSmsInviteMessage()
                dialog.cancel()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()
        alert.show()
    }
}