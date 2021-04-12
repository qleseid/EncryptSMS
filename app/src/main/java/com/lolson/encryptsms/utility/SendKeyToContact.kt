package com.lolson.encryptsms.utility

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.lolson.encryptsms.MainSharedViewModel

class SendKeyToContact(_context: Context, _viewModel: MainSharedViewModel)
{
    private val mContext = _context
    private val mVM = _viewModel
    //Logger
    private var l = LogMe()

    /**
     * ALERT POP DIALOG BOX
     */
    fun showAlertWithTextInput()
    {

        val alert = AlertDialog.Builder(mContext)
            .setTitle("No Secure Key Found")
            .setMessage("This contact doesn't have a saved key.\n\n" +
                    "Send an invite?")
            .setPositiveButton("Send Invite"){ dialog, _ ->
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