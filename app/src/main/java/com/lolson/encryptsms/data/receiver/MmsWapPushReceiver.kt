package com.lolson.encryptsms.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MmsWapPushReceiver: BroadcastReceiver() {
    val EXTRA_SUBSCRIPTION = "subscription"
    val EXTRA_DATA = "data"

    @Override
    override fun onReceive(context: Context, intent: Intent) {
//        if (Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION == intent.action
//            && "application/vnd.wap.mms-message" == (intent.type)) {
//            if (PhoneUtils.getDefault().isSmsEnabled()) {
//                // Always convert negative subIds into -1
//                final int subId = PhoneUtils.getDefault().getEffectiveIncomingSubIdFromSystem(
//                        intent, MmsWapPushReceiver.EXTRA_SUBSCRIPTION);
//                final byte[] data = intent.getByteArrayExtra(MmsWapPushReceiver.EXTRA_DATA);
//                mmsReceived(subId, data);
//            }
//        }
    }

//    open fun mmsReceived(final int subId, final byte[] data) {
//        if (!PhoneUtils.getDefault().isSmsEnabled()) {
//            return;
//        }
//
//        final ReceiveMmsMessageAction action = new ReceiveMmsMessageAction(subId, data);
//        action.start();
//    }
}
