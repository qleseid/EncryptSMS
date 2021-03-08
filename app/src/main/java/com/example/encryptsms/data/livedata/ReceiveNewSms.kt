package com.example.encryptsms.data.livedata

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import com.example.encryptsms.utility.LogMe

class ReceiveNewSms: LiveData<Boolean>()
{
    //Logger
    var l = LogMe()

    override fun onActive()
    {
        l.d("LIVEDATA ONACTIVE: $value")
    }

    override fun onInactive()
    {
        l.d("LIVEDATA ON***in***ACTIVE: $value")
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