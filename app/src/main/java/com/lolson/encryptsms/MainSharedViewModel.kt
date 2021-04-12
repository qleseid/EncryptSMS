package com.lolson.encryptsms

import android.app.Application
import android.util.Base64
import android.util.Base64.DEFAULT
import androidx.lifecycle.*
import com.lolson.encryptsms.contents.KeyContent
import com.lolson.encryptsms.data.model.Phone
import com.lolson.encryptsms.data.model.Sms
import com.lolson.encryptsms.repository.ContactsKeyRepository
import com.lolson.encryptsms.repository.DhRepository
import com.lolson.encryptsms.repository.SmsRepository
import com.lolson.encryptsms.utility.CryptoMagic
import com.lolson.encryptsms.utility.LogMe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.security.InvalidKeyException
import java.security.KeyPair
import java.security.PublicKey
import java.util.*
import javax.crypto.KeyAgreement
import javax.crypto.spec.SecretKeySpec

/**
 * Shared view model for main activity fragments
 */
class MainSharedViewModel(application: Application): AndroidViewModel(application)
{
    // Invite message
    private val mInviteMessage =
            "Encrypt SMS: I'm using a Secure SMS app, join me! Here is my " +
            "public key when you're ready!"

    // Logger
    private var l = LogMe()

    // Context of activity
    private val context = application.applicationContext

    // App health
    private var _dhKeyGood: MutableLiveData<Boolean> = MutableLiveData(false)
    val dhKeyGood: LiveData<Boolean>
        get() = _dhKeyGood

    private var _contactKeysGood: MutableLiveData<Triple<Boolean, String, String>> = MutableLiveData(
        Triple(false, "0", "0"))
    val contactKeysGood: LiveData<Triple<Boolean, String, String>>
        get() = _contactKeysGood

    // Threads
    private var _threads: MutableLiveData<ArrayList<Sms.AppSmsShort>?> = MutableLiveData()
    val threads: LiveData<ArrayList<Sms.AppSmsShort>?>
        get() = _threads
    private var sqlThreads = ArrayList<Sms.AppSmsShort>()

    // Messages
    private var _messages: MutableLiveData<ArrayList<Sms.AppSmsShort>?> = MutableLiveData()
    val messages: LiveData<ArrayList<Sms.AppSmsShort>?>
        get() = _messages
    private var sqlMessages = ArrayList<Sms.AppSmsShort>()

    // Logic for the encryption switch
    private var _encryptSwitch: MutableLiveData<Boolean> = MutableLiveData(false)
    val encSwitch: LiveData<Boolean>
        get() = _encryptSwitch

    // Console output in About Fragment
    val text: LiveData<String>
        get() = _text

    // App Bar Title
    private var _title: MutableLiveData<String> = MutableLiveData("Inbox")
    val title: LiveData<String>
        get() = _title

    // Contact keys
    private var _contactKeys: ArrayList<KeyContent.AppKey> = ArrayList()

    // Map for contact keys by thread_id
    private lateinit var _contactKeysMap: Map<String, SecretKeySpec>

    // SMS container for individual conversations
    var tempSms: Sms.AppSmsShort? = null

    lateinit var draftSms: Sms.AppSmsShort

    // Application key agreement
    private val keyAgree = KeyAgreement.getInstance("DH")

    // Application DH key
    private var dhKey: KeyPair? = null

    // Repositories
    private val contactKeyRep = ContactsKeyRepository(context)
    private val smsRep = SmsRepository(context)
    private val dhRep = DhRepository(context)

    init
    {
        viewModelScope.launch(Dispatchers.IO) {

            // Get DH Keys
            getDhKeys()

//            // Get all the contacts keys
//            getContactsKeys()
        }
    }

    /**
     * SET ENCRYPTED TOGGLE
     */
    fun setEncryptedToggle(
        bool: Boolean
    )
    {

            l.d("MVM ENCRYPTED TOGGLE: $bool ${_encryptSwitch.value}")

            // Each Fragment resets this toggle, this stop unnecessary runs
            if (_encryptSwitch.value != bool)
            {
                _encryptSwitch.value = bool
                //Create a new coroutine to move execution off of UI thread
                viewModelScope.launch(Dispatchers.IO) {

                if (tempSms != null)
                {
                    l.d("MVM ENCRYPTED TOGGLE REFRESH BOTH: $bool ${_encryptSwitch.value}")

                    // Refreshes the recycler view in real-time
                    _threads.postValue(deLoop(sqlThreads))
                    _messages.postValue(deLoop(sqlMessages))
                }
                else
                {
                    l.d("MVM ENCRYPTED TOGGLE REFRESH THREAD ONLY: $bool ${_encryptSwitch.value}")
                    _threads.postValue(deLoop(sqlThreads))
                }
            }
        }
    }

    /**
     * CHECK FOR ENCRYPTION KEY
     */
    fun checkForEncryptionKey(
        thread_id: Long
    ):Boolean
    {
        return _contactKeysMap.containsKey(thread_id.toString())
    }

    /**
     * CHECK IF INVITE WAS SENT
     */
    private fun checkInviteSent(
        thread_id: Long
    ):Boolean
    {
        l.d("MVM CHECK INVITE SENT START: $thread_id")
        var result = false

        for (k in _contactKeys)
        {
            if (k.thread_id.toLong() == thread_id)
            {
                l.d("MVM CHECK INVITE SENT: ${k.sent}")
                result = k.sent
            }
        }
        return result
    }

    /**
     * REFRESH THREADS
     */
    fun refresh(
        select: Int
    )
    {
        l.d("MVM REFRESH: $select")
        when(select)
        {
            0 -> getAllThreads() // 0 = refresh the threads
            1 -> getAllMessages()
        }
    }

    /**
     * SEND MESSAGE
     */
    fun sendSmsMessage(
        msg: String
    )
    {
        val data = ArrayList<Sms.AppSmsShort>()

        viewModelScope.launch(Dispatchers.IO) {
            // Creates shallow copy of objects
            _messages.value?.map { data.add(it.copy()) }

            if (tempSms == null)
            {
                tempSms = Sms.AppSmsShort()
            }

            // Build the message
            tempSms!!.read = 1
            tempSms!!.status = 0
            tempSms!!.type = 2
            tempSms!!.date = System.currentTimeMillis()
            tempSms!!.date_sent = tempSms!!.date
            tempSms!!.creator = "com.example.encryptsms"
            tempSms!!.body = msg

            // Encrypt message if switch is set
            if (encSwitch.value!! && checkForEncryptionKey(tempSms!!.thread_id))
            {
                tempSms!!.body = CryptoMagic.encrypt(
                    tempSms!!,
                    _contactKeysMap[tempSms!!.thread_id.toString()]
                )
            }

            // Copy to make new object: fixes reference copy issues
            draftSms = tempSms!!.copy()

            if (smsRep.send(draftSms))
            {
                l.d("MVM SHARE SEND SMS SUCCESS!")

                // Don't add the keys to message conversation
                if (msg.startsWith("$"+"CILBUP"))
                {
                    // Add to end of list
                    draftSms.body = "Public Key Sent"
                    l.d("MVM PUBLIC KEY: $msg")
                }
                else
                {
                    // Add correct msg
                    draftSms.body = msg
                }
                // Add to end of list
                data.add(data.lastIndex + 1, draftSms)

                // Add to local list
                sqlMessages.add(data.lastIndex, draftSms)
                // Populate messages live data to refresh recycler
                _messages.postValue(data)
            }
        }
    }

    /**
     * SEND INVITE MESSAGE WITH PUBLIC KEY
     */
    fun sendSmsInviteMessage()
    {
        viewModelScope.launch(Dispatchers.Default) {
            // Need a key to sent and not sent an invite yet
            if (dhKeyGood.value!! && !checkInviteSent(tempSms!!.thread_id))
            {
                l.d("MVM SEND INVITE: ${tempSms!!.thread_id}")
                // Send the invite header message
                sendSmsMessage(mInviteMessage)

                // Send you're public key
                val en = Base64.encodeToString(dhKey?.public?.encoded, DEFAULT)
                sendSmsMessage("$" + "CILBUP" + en + "DNE$")

//                // Debug truth
//                try
//                {
//
//                    l.d("MVM SEND INVITE ENCODE: $en")
//
//                    val de = KeyFactory.getInstance("DH").generatePublic(
//                        X509EncodedKeySpec(Base64.decode(en, DEFAULT)))
//                    val truth = de == dhKey?.public
//
//                    l.d("MVM SEND INVITE DECODE: $de :: $truth")
//                }catch (e: Exception){}
            }
        }
    }

    /**
     * GET ALL MESSAGES FOR CONTACT STORED IN TEMP SMS
     */
    fun getAllMessages()
    {
        val data = ArrayList<Sms.AppSmsShort>()

        viewModelScope.launch(Dispatchers.IO) {
            smsRep.getAllMessages(
                arrayListOf(
                    Phone.pho(
                        tempSms?.thread_id.toString(),
                        tempSms?.address
                    )
                )
            ).let {
                if (it != null)
                {
                    data.addAll(it)
                }
            }

            // shallow copy and local storage of threads
            sqlMessages.clear()
            data.map { sqlMessages.add(it.copy()) }

            // Decrypt the messages and post to live data
            _messages.postValue(deLoop(data))
//            _messages.postValue(data)
        }
    }

    /**
     * GET ALL THREADS
     */
    fun getAllThreads()
    {
        val data = ArrayList<Sms.AppSmsShort>()

        //Create a new coroutine to move execution off of UI thread
        viewModelScope.launch(Dispatchers.IO) {

            smsRep.getAllThreads().let {
                if (it != null)
                {
                    data.addAll(it)
                }
            }

            // shallow copy and local storage of threads
            sqlThreads.clear()
            data.map { sqlThreads.add(it.copy()) }

            // Decrypt the messages and post to live data
            _threads.postValue(deLoop(data))

            // Rough estimate of total contacts with messages
//            l.d("MVM THREAD STATUS: ${_contactKeysGood.value!!.first} " +
//                    "${_contactKeysGood.value!!.second} " +
//                    " ${data.size}")

            // Some hardware handles IO operations different; data is missed outside of Main
            viewModelScope.launch(Dispatchers.Main) {
                _contactKeysGood.value = Triple(
                    _contactKeysGood.value!!.first,
                    _contactKeysGood.value!!.second,
                    data.size.toString()
                )
            }
//            genContacts(data)
        }
    }

    /**
     * FIND THREAD ID
     */
    fun findThreadId()
    {
        viewModelScope.launch(Dispatchers.IO) {

            tempSms?.thread_id = tempSms?.address?.let { smsRep.find(it) }!!

            getAllMessages()
        }
    }

    /**
     * MESSAGE DECRYPT LOOP
     */
    private fun deLoop(
        data: ArrayList<Sms.AppSmsShort>
    ):ArrayList<Sms.AppSmsShort>
    {
        val result = ArrayList<Sms.AppSmsShort>()
            data.map { result.add(it.copy()) }

            l.d("MVM DECRYPTING START: ${encSwitch.value!!}")
            // con = count, d = individual data
            for ((con, d) in data.withIndex())
            {
                if (encSwitch.value!!)
                {
                    result[con].body = CryptoMagic.decrypt(
                        d,
                        _contactKeysMap[d.thread_id.toString()]
                    )
                }
//                l.d("DECRYPTING END: ${data[con].body}")
            }
        return result
    }

        /**
     * SET MAIN ACTIVITY TITLE
     */
    fun setTitle(
        stg: String
    )
    {
        _title.postValue(stg)
    }

    /**
     * GET DH KEY
     */
    private suspend fun getDhKeys()
    {
        dhKey = dhRep.getKey()
//        dhKey = CryptoMagic.generateDHKeys()
//        saveDhKeys(dhKey!!)

        if (dhKey == null)
        {
            l.d("MVM $$\\NO DH KEYS YET$$")
            dhKey = CryptoMagic.generateDHKeys()
            saveDhKeys(dhKey!!)
        }
        // Init key agreement
        try
        {
            keyAgree.init(dhKey!!.private)

            // Once the DH key is returned and init, build secrets
            getContactsKeys()

            _dhKeyGood.postValue(true)
        }
        catch (e: InvalidKeyException)
        {
            l.e("MVM Error creating private key agreement: $e")
        }

//        l.d("PUB GEN: ${Base64.encodeToString(dhKey!!.public.encoded, DEFAULT)}")
//        l.d("PVT GEN: ${Base64.encodeToString(dhKey!!.private.encoded, DEFAULT)}")
//        l.d("PUB ENCODE: ${dhKey!!.public.encoded.contentEquals(dhKey!!.private.encoded)}")

    }

    /**
     * SET DH KEY
     */
    private suspend fun saveDhKeys(
        key: KeyPair
    )
    {
        if (!dhRep.saveKey(key)){l.e("MVM ERROR: Failed to save DH KEY")}
    }

    /**
     * GENERATE SECRET FOR CONTACT
     */
    private fun generateSecret(
        key: PublicKey
    ):SecretKeySpec
    {
        l.d("MVM ^^^^^^^GENERATING SECRET^^^^^^")
        keyAgree.doPhase(key, true)
        return SecretKeySpec(
            keyAgree.generateSecret(),
            0,
            32,
            "AES"
        )
    }

    /**
     * GET CONTACTS KEYS
     */
    private suspend fun getContactsKeys()
    {
        // Get keys from database
        _contactKeys.addAll(contactKeyRep.getAll())

        mapContactKeys()

        l.d("MVM GETTING CON KEYS, total: ${_contactKeys.size}")
    }

    /**
     * MAP CONTACT KEYS
     */
    private fun mapContactKeys()
    {
        // Map ArrayList by thread_id to its secret key
        _contactKeysMap = _contactKeys.associate {
            it.thread_id to generateSecret(it.publicKey)
        }
//        l.d("MVM CONTACTS STATUS: ${_contactKeysGood.value!!.first} " +
//                "${_contactKeysGood.value!!.second} " +
//                " ${_contactKeysGood.value!!.third}")

        // Some hardware handles IO operations different; data is missed outside of Main
        viewModelScope.launch(Dispatchers.Main) {
            _contactKeysGood.value = Triple(
                true,
                _contactKeysMap.size.toString(),
                _contactKeysGood.value!!.third
            )
        }
        l.d("MVM MAPPING CON KEYS, total: ${_contactKeysMap.size}")
    }

    /**
     * UPDATE CONTACTS KEY
     */
    private fun updateContactsKey()
    {

    }

    /**
     * DELETE CONTACTS KEY
     */
    private fun deleteContactKey()
    {

    }

    /**
     * CREATE CONTACTS KEY
     */
    private suspend fun createContactsKey(
        key: KeyContent.AppKey
    ):Boolean
    {
        return contactKeyRep.create(key)
    }

    /**
     * ABOUT CONSOLE OUTPUT FLOW
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    private val conFlow: Flow<String> = flow {
        l.d("Flow runup")
        Runtime.getRuntime().exec("logcat -c")
        Runtime.getRuntime().exec("logcat")
            .inputStream
            .bufferedReader()
            .useLines { lines -> lines.forEach { line -> emit(line) } }
    }
        .onStart {
            l.d("Flow started")
            emit("FLOW START") }
        .flowOn(Dispatchers.Default)

    private var _text = conFlow.asLiveData(viewModelScope.coroutineContext + Dispatchers.Default)
//    private var _text = consStream()

//    private suspend fun genContacts(
//        data: ArrayList<Sms.AppSmsShort>
//    )
//    {
//        for(d in data)
//        {
//            val key = KeyContent.AppKey(
//                "",                                    // key primary id
//                d.thread_id.toString(),                // thread id
//                CryptoMagic.generateDHKeys().public    // public key
//            )
//
//            if (createContactsKey(key))
//            {
//                l.d("CREATE CONTACT KEYS: ${key.id} ${d.thread_id}")
//                _contactKeys.add(key)
//                mapContactKeys()
//            }
//            else
//            {
//                l.d("ERROR CREATING CONTACT KEY: ${d.address}")
//            }
//        }
//    }
}