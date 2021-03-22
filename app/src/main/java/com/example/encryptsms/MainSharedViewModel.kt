package com.example.encryptsms

import android.app.Application
import android.net.Uri
import android.provider.Telephony
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.encryptsms.data.model.Phone
import com.example.encryptsms.data.model.Sms
import com.example.encryptsms.keys.KeyContent
import com.example.encryptsms.repository.ItemRepository
import com.example.encryptsms.utility.CryptoMagic
import com.example.encryptsms.utility.LogMe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Shared view model for main activity fragments
 */
class MainSharedViewModel(application: Application): AndroidViewModel(application)
{
    //Context of activity
    private val context = application.applicationContext

    // SMS URI
    private val smsContentUri: Uri = Uri.parse("content://sms")

    // Threads
    private var _threads: MutableLiveData<ArrayList<Sms.AppSmsShort>?> = MutableLiveData()
    val threads: LiveData<ArrayList<Sms.AppSmsShort>?>
        get() = _threads

    // Messages
    private var _messages: MutableLiveData<ArrayList<Sms.AppSmsShort>?> = MutableLiveData()
    val messages: LiveData<ArrayList<Sms.AppSmsShort>?>
        get() = _messages


    //Items and map
    private var _items: MutableLiveData<ArrayList<KeyContent.AppKey>> = MutableLiveData()
    val items: LiveData<ArrayList<KeyContent.AppKey>>
        get() = _items

    private var _itemsMap: MutableLiveData<MutableMap<String, Int>> = MutableLiveData()
    val itemsMap: LiveData<MutableMap<String, Int>>
            get() = _itemsMap

    private var _encryptSwitch: MutableLiveData<Boolean> = MutableLiveData(false)
    val encSwitch: LiveData<Boolean>
        get() = _encryptSwitch

    private var _title: MutableLiveData<String> = MutableLiveData("Inbox")
    val title: LiveData<String>
        get() = _title

    lateinit var tempSms: Sms.AppSmsShort
    lateinit var draftSms: Sms.AppSmsShort


    private val smsM = com.example.encryptsms.data.manager.SmsManager(context)

    lateinit var draft: Telephony.Mms.Draft
    //Logger
    private var l = LogMe()

    //Create instance to save keys to file
    private val itemRep = ItemRepository(context)

    init
    {
        //Get all messages
        getAllThreads()
    }

    /**
     * SET ENCRYPTED TOGGLE
     */
    fun setEncryptedToggle(bool: Boolean)
    {
        _encryptSwitch.postValue(bool)

        if (this::tempSms.isInitialized)
        {
            // Refreshes the recycler view in real-time
            refresh(0)
            refresh(1)
        }
        else
        {
            refresh(0)
        }
    }

    /**
     * REFRESH THREADS
     */
    fun refresh( select: Int)
    {
        when(select)
        {
            0 -> getAllThreads() // 0 = refresh the threads
            1 -> getAllMessages()
        }
    }

    /**
     * ADD MESSAGE
     */
    fun sendSmsMessage(msg: String)
    {
        val data = ArrayList<Sms.AppSmsShort>()
        // Creates shallow copy of objects
//        _messages.value?.let { data = it.clone() as ArrayList<Sms.AppSmsShort> }
        _messages.value?.map { data.add(it) }

        // Encrypt message if switch is set
        if(encSwitch.value!!)
        {
            tempSms.body = CryptoMagic.encrypt(msg)
        }
        else
        {
            tempSms.body = msg
        }

        // Build the message
        tempSms.read = 1
        tempSms.status = 0
        tempSms.type = 2
        tempSms.date = System.currentTimeMillis()
        tempSms.date_sent = tempSms.date
        tempSms.creator = "com.example.encryptsms"

        // Copy to make new object: fixes reference copy issues
        draftSms = tempSms.copy()

        if (smsM.sendMessage(draftSms, smsContentUri))
        {
            l.d("SHARE SEND SMS SUCCESS!")
            // Add to end of list
            data.add(data.lastIndex + 1, draftSms)

            // Populate messages live data to refresh recycler
            _messages.postValue(data)
        }
    }

    /**
     * GET ALL MESSAGES FOR CONTACT STORED IN TEMP SMS
     */
    fun getAllMessages()
    {
        val data = ArrayList<Sms.AppSmsShort>()

        // Create a new coroutine to move execution off of UI thread
        viewModelScope.launch(Dispatchers.IO) {

            // Use thread ID and address to get all the messages
            smsM.getSms(arrayListOf(Phone.pho(tempSms.thread_id.toString(),tempSms.address)
                ))?.let { data.addAll(it) }

            // Decrypt the messages
            deLoop(data)

            _messages.postValue(data)
        }
    }

    /**
     * GET ALL THREADS
     */
    private fun getAllThreads()
    {
        val data = ArrayList<Sms.AppSmsShort>()

        //Create a new coroutine to move execution off of UI thread
        viewModelScope.launch(Dispatchers.IO) {

            smsM.getConvoThreads()?.let { data.addAll(it) }

            // Decrypt the messages
            deLoop(data)

            _threads.postValue(data)
        }
    }

    /**
     * FIND THREAD ID
     */
    fun findThreadId()
    {
        tempSms.thread_id = smsM.getThreadId(tempSms.address)

        getAllMessages()
    }

    /**
     * MESSAGE DECRYPT LOOP
     */
    private fun deLoop(data: ArrayList<Sms.AppSmsShort>)
    {
        // con = count, d = individual data
        for ((con, d) in data.withIndex())
        {
            l.d("DECRYPTING START: ${d.body}")
//            if (encSwitch.value!! && d.type == 2)
            if (encSwitch.value!!)
            {
                data[con].body = CryptoMagic.decrypt(d.body)
            }
            l.d("DECRYPTING END: ${data[con].body}")
        }
    }

    /**
     * SET MAIN ACTIVITY TITLE
     */
    fun setTitle(stg: String)
    {
        _title.postValue(stg)
    }
}