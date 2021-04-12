package com.lolson.encryptsms.data.livedata

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import com.lolson.encryptsms.utility.LogMe

/**
 * SIGNALS APP TO REFRESH DATA WHEN ITS RECEIVED
 */
class ReceiveNewSms: LiveData<Boolean>()
{
    //Logger
    var l = LogMe()

    override fun onActive()
    {
        l.d("RNS LIVEDATA ONACTIVE OBSERVER: $value")
    }

    override fun onInactive()
    {
        l.d("RNS LIVEDATA ON***INACTIVE OBSERVER: $value")
    }

    companion object {
        private lateinit var sInstance: ReceiveNewSms

        @MainThread
        fun get(): ReceiveNewSms {
            sInstance = if (::sInstance.isInitialized) sInstance else ReceiveNewSms()
            return sInstance
        }

        fun set(value: Boolean)
        {
            get()
            sInstance.postValue(value)
        }
    }

}