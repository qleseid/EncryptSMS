package com.lolson.encryptsms

import android.app.Application
import android.util.Base64
import android.util.Base64.DEFAULT
import androidx.lifecycle.*
import com.lolson.encryptsms.contents.KeyContent
import com.lolson.encryptsms.data.model.Phone
import com.lolson.encryptsms.data.model.Sms
import com.lolson.encryptsms.modelviewhelpers.CryptoHelper
import com.lolson.encryptsms.modelviewhelpers.KeyHelper
import com.lolson.encryptsms.modelviewhelpers.SmsHelper
import com.lolson.encryptsms.repository.ContactsKeyRepository
import com.lolson.encryptsms.repository.EcRepository
import com.lolson.encryptsms.repository.SmsRepository
import com.lolson.encryptsms.utility.CryptoMagic
import com.lolson.encryptsms.utility.LogMe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.InvalidKeyException
import java.security.KeyPair
import javax.crypto.KeyAgreement
import javax.crypto.spec.SecretKeySpec

/**
 * Shared view model for main activity fragments
 */
class MainSharedViewModel(application: Application): AndroidViewModel(application)
{
    // Invite message
    private val mInviteMessage =
            "EncryptSMS: I'm using a Secure SMS app, join me! Here is my " +
                    "public key when you're ready!" +
                    "\nhttps://github.com/qleseid/EncryptSMS/tree/main/docs/apk"

    // Logger
    private var l = LogMe()

    // Context of activity
    private val context = application.applicationContext

    // App health
    private var _dhKeyGood: MutableLiveData<Boolean> = MutableLiveData(false)
    val dhKeyGood: LiveData<Boolean>
        get() = _dhKeyGood

    // 1: Keys retrieved, 2: # keys, 3: # contacts
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
    private var _text: MutableLiveData<String> = MutableLiveData()
    val text: LiveData<String>
        get() = _text

    // App Bar Title
    private var _title: MutableLiveData<String> = MutableLiveData("Inbox")
    val title: LiveData<String>
        get() = _title

    // Alert Dialog selector
    private var _alert: MutableLiveData<Triple<Int, String?, String?>> = MutableLiveData(Triple(
        -1,
        null,
        null))
    val alert: LiveData<Triple<Int, String?, String?>> = _alert
        .asFlow()
        .catch { l.e("MVM:: ALERT FLOW ERROR") }
        .asLiveData(Dispatchers.Default)

    // Contact keys
    private var _contactKeys: ArrayList<KeyContent.AppKey> = ArrayList()

    // Map for contact keys by thread_id
    private var _contactKeysMap: MutableMap<Long, KeyContent.AppKey> = mutableMapOf()

    // Map for contact keys secret by thread_id
    private var _contactKeysSecMap: MutableMap<Long, SecretKeySpec?> = mutableMapOf()

    // SMS container for individual conversations
    var tempSms: Sms.AppSmsShort? = null

    lateinit var draftSms: Sms.AppSmsShort

    // Application key agreement
    private val keyAgree = KeyAgreement.getInstance("ECDH")

    // Application DH key
    private var ecKey: KeyPair? = null

    // Holder variable for all console output
    private var consoleHolder = "MOO COW:\n"

    // Repositories
    private val contactKeyRep = ContactsKeyRepository(context)
    private val smsRep = SmsRepository(context)
    private val ecRep = EcRepository(context)

    init
    {
        viewModelScope.launch(Dispatchers.Default) {

//            l.d("MVM:: KEY PROVIDERS: ${Security.getProviders().asList()}")
//            Security.getProviders().forEach {
//
//                l.d("MVM:: KEY PROVIDER: ${it.info} Services: ${it.services}")
//            }
//            l.d("MVM:: KEY ALGORITHM: ${Security.getAlgorithms("keyagreement").toList()}")
//            l.d("MVM:: KEY ALGORITHM: ${Security.getAlgorithms("keypairgenerator").toList()}")
//
//            l.d("MVM:: GET PROVIDER: ${KeyPairGenerator.getInstance("EC", "BC")
//                .provider}")
            // Get DH Keys
            getDhKeys()

            // Flow the console data as it comes
            conFlow
                .catch { e ->
                    l.e("MVM:: INIT FLOW ERROR: console flow $e")
                }
                .collect {
                    // Reset after 21k characters, some devices have crazy output
                    if (consoleHolder.length > 21000){consoleHolder = ""}

                    // Hopefully append the strings value properly
                    consoleHolder += "\n" + it
                    _text.postValue(consoleHolder)
                }
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

        l.d("MVM:: ENCRYPTED TOGGLE: $bool ${_encryptSwitch.value}")

        // Each Fragment resets this toggle, this stop unnecessary runs
        if (_encryptSwitch.value != bool && _contactKeysGood.value!!.first)
        {
            _encryptSwitch.value = bool
            //Create a new coroutine to move execution off of UI thread
            viewModelScope.launch(Dispatchers.Default) {

                if (tempSms != null)
                {
                    l.d("MVM:: ENCRYPTED TOGGLE REFRESH BOTH: $bool ${_encryptSwitch.value}")

                    // Refreshes the recycler view in real-time
                    _threads.postValue(
                        if (encSwitch.value!!)
                        {
                            CryptoHelper().deLoop(sqlThreads, _contactKeysSecMap)
                        }
                        else
                        {
                            sqlThreads
                        })
                    _messages.postValue(
                        if (encSwitch.value!!)
                        {
                            CryptoHelper().deLoop(sqlMessages, _contactKeysSecMap)
                        }
                        else
                        {
                            sqlMessages
                        })
                }
                else
                {
                    l.d("MVM:: ENCRYPTED TOGGLE REFRESH THREAD ONLY: $bool ${_encryptSwitch.value}")
                    _threads.postValue(
                        if (encSwitch.value!!)
                        {
                            CryptoHelper().deLoop(sqlThreads, _contactKeysSecMap)
                        }
                        else
                        {
                            sqlThreads
                        })
                }
                // Check for invite and alert when encrypt set to true
//                tempSms?.let { alertHelper(0, null)}
            }
        }
    }

    /**
     * CHECK FOR ENCRYPTION KEY
     * TODO:: Key can be null, create check
     * Uses:
     * 1- Check for a key
     */
    private fun checkForEncryptionKey(
        thread_id: Long
    ):Boolean
    {
        return KeyHelper().checkForEncryptionKey(thread_id, _contactKeysSecMap)
    }

    /**
     * CHECK IF INVITE WAS SENT
     *
     * Uses:
     * 1- Check if an invite has been sent
     */
    private fun checkInviteSent(
        thread_id: Long
    ):Boolean
    {
        return KeyHelper().checkInviteSent(thread_id, _contactKeys)
    }

    /**
     * ALERT HELPER
     * Helps check for invite sent and key discovery
     */
    fun alertHelper(
        select: Int,
        msg: String?,
        cmd: String?
    )
    {
        viewModelScope.launch(Dispatchers.Default) {
            l.d("MVM:: ALERT HELPER SELECTION: $select -:- $cmd")

            when (select)
            {
                0    -> // Invite alert dialog
                {
                    // Check invite and alert when encrypt set to true
//                    TODO:: Fix this before sending it
//                    if (_encryptSwitch.value!! && tempSms!!.thread_id != -1L)
                    if (tempSms!!.thread_id != -1L)
                    {
                        _alert.postValue(Triple(select, msg, cmd))
                    }
                }
                1    -> // Snack alert dialogs
                {
                    _alert.postValue(Triple(select, msg, cmd))
                }
                2    -> // Input number alert dialog
                {
                    _alert.postValue(Triple(select, msg, cmd))
                }
                3    -> // Toast alert dialogs
                {
                    _alert.postValue(Triple(select, msg, cmd))
                }
                else ->
                {
                    _alert.postValue(Triple(select, msg, cmd))
                    l.d("MVM:: ALERT HELPER SELECTION ELSE: $select")
                }
            }
        }
    }

    /**
     * REFRESH THREADS
     */
    fun refresh(
        select: Int
    )
    {
        l.d("MVM:: REFRESH: $select")
        when(select)
        {
            0 -> getAllThreads() // 0 = refresh the threads
            1 -> getAllMessages()
        }
    }

    /**
     * UPDATE SMS
     */
    fun updateSmsMessage()
    {
        viewModelScope.launch(Dispatchers.Default) {
            if (smsRep.update(draftSms))
            {
                // Inform that message was updated
//                alertHelper(1, "Message was updated.", null)
                l.d("MVM:: UPDATE SMS SUCCESS!")
                for ((con, m) in sqlMessages.withIndex())
                {
                    if (m.id == draftSms.id)
                    {
                        l.d("MVM:: UPDATED SMS AT: $con : ${sqlMessages[con].read}")
                        sqlMessages[con] = draftSms
                        _messages.postValue(sqlMessages)
                        break
                    }
                }
                for ((con, m) in sqlThreads.withIndex())
                {
                    if (m.thread_id == draftSms.thread_id)
                    {
                        l.d("MVM:: UPDATED SMS THREAD AT: $con : ${sqlThreads[con].read}")
                        sqlThreads[con].read = 1
                        _threads.postValue(sqlThreads)
                        break
                    }
                }
            }
            else
            {
                // Inform that message didn't update
//                alertHelper(1, "Message update FAILED! Is app set as default?", null)
                l.d("MVM:: UPDATE SMS FAILED!")
            }
        }
    }

    /**
     * DELETE SMS
     */
    fun deleteSmsMessage()
    {
        viewModelScope.launch(Dispatchers.Default) {
            if (smsRep.delete(draftSms))
            {
                // Inform that message was deleted
                alertHelper(1, "Message was deleted.", null)

                for ((con, m) in sqlMessages.withIndex())
                {
                    if (m.id == draftSms.id)
                    {
                        l.d("MVM:: DELETE SMS AT: $con")
                        sqlMessages.removeAt(con)
                        _messages.postValue(sqlMessages)
                        refresh(0) // Update the threads
                        break
                    }
                }
            }
            else
            {
                // Inform that message didn't deleted
                alertHelper(1, "Message delete FAILED! Is app set as default?", null)
            }
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

        viewModelScope.launch(Dispatchers.Default) {
            // Creates shallow copy of objects
            _messages.value?.map { data.add(it.copy()) }


            if (tempSms == null){ tempSms = Sms.AppSmsShort() }
            SmsHelper().buildSmsMessage(msg, tempSms!!)

            // Encrypt message if switch is set
            // TODO:: Fix sending an invite encrypted
            if (encSwitch.value!! && checkForEncryptionKey(tempSms!!.thread_id))
            {
                tempSms!!.body = CryptoMagic.encrypt(
                    tempSms!!,
                    _contactKeysSecMap[tempSms!!.thread_id]
                )
            }

            // Copy to make new object: fixes reference copy issues
            draftSms = tempSms!!.copy()

            if (smsRep.send(draftSms))
            {
                l.d("MVM:: SEND SMS SUCCESS!")
                // Update the local thread
                sqlThreads = SmsHelper().updateThread(draftSms, sqlThreads)

                // Add correct msg
                draftSms.body = msg

                // Add to end of list
                data.add(data.lastIndex + 1, draftSms)

                // Add to local list
                sqlMessages.add(data.lastIndex, draftSms)

                // Populate messages and thread live data to refresh recycler
                _messages.postValue(data)
                _threads.postValue(sqlThreads)
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

            // TODO:: set that an invite has been sent
            if (dhKeyGood.value!!)
            {
                l.d("MVM:: SEND INVITE: ${tempSms!!.thread_id}")

                try
                {
                    val contact = _contactKeysMap.getValue(
                        tempSms!!.thread_id
                    )
                    // Update the key map value
                    // This updates _contactKeys as well
                    contact.sent = true
                    if (updateContactsKey(contact))
                    {
                        // Send the invite header message
                        sendSmsMessage(mInviteMessage)

                        // Send you're public key
                        val en = Base64.encodeToString(ecKey?.public?.encoded, DEFAULT)
                        sendSmsMessage("$" + "CILBUP" + en + "DNE$")

                        l.d("MVM:: SEND INVITE PUBLIC KEY SIZE: ${en.length}")

                        l.d(
                            "MVM:: SEND INVITE map: " +
                                    "${_contactKeysMap[tempSms!!.thread_id]?.sent}")
                        l.d(
                            "MVM:: SEND INVITE secmap: " +
                                    "${_contactKeys[_contactKeys.indexOf(contact)].sent}")
                    }
                }
                catch (e: Exception)
                {
                    l.e("MVM:: SEND INVITE ERROR: $e")
                }
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
            )?.let {
                data.addAll(it)
                buildMessage(data)
            }
        }
    }

    /**
     * MESSAGE BUILDER
     */
    private suspend fun buildMessage(
        data: ArrayList<Sms.AppSmsShort>
    )
    {
        // Check if there are any values
        if (data.size > 0)
        {
            // shallow copy and local storage of threads
            sqlMessages.clear()
            data.map { sqlMessages.add(it.copy()) }

            // TODO:: The last has been changed for testing, should be 'it'.
            // TODO:: 'it' is the marker for last message checked, set to scan
            // TODO:: all messages currently.
            val msg = _contactKeysMap[data[0].thread_id]?.last_check?.let {
                KeyHelper().keyMessageFinder(
                    data,
                    1L) //TODO:: right here, got it!
            }

            val contact: KeyContent.AppKey

            // If -1 id, means no message with key found
            if (msg?.id != -1 && msg != null)
            {
                msg.let { smsShort ->
                    contact = KeyHelper().buildContactKey(
                        KeyHelper().trimMessageAndGenerateKey(smsShort),
                        smsShort,
                        _contactKeysMap
                    )

                    if (updateContactsKey(contact))
                    {
                        _contactKeysSecMap[contact.thread_id] =
                                contact.publicKey?.let {
                                    CryptoHelper().generateSecret(
                                        it,
                                        keyAgree)
                                }
                    }
                    else
                    {
                        l.e("MVM:: UPDATE OF CONTACT KEY FAILED")
                    }
                }
                alertHelper(
                    1,
                    "Key found in message: ${msg.let { data.indexOf(it) }}",
                    null)
            }
            else
            {
                // Inform of previous invite but no saved key
                if (checkInviteSent(tempSms!!.thread_id)
                    && !checkForEncryptionKey(tempSms!!.thread_id))
                {
                    alertHelper(
                        1,
                        "Invite previously sent. No keys received yet.",
                        null)
                }
            }

            // Decrypt the messages and post to live data
            _messages.postValue(
                if (encSwitch.value!!)
                {
                    CryptoHelper().deLoop(data, _contactKeysSecMap)
                }
                else
                {
                    data
                })
        }
    }

    /**
     * CLEAN UP THE MESSAGES
     */
    fun cleanUpMessages()
    {
        _messages.value?.clear()
        sqlMessages.clear()
        tempSms = Sms.AppSmsShort()
        draftSms = Sms.AppSmsShort()
    }

    /**
     * GET ALL THREADS
     */
    fun getAllThreads()
    {
        val data = ArrayList<Sms.AppSmsShort>()

        // Create a new coroutine to move execution off of UI thread
        viewModelScope.launch(Dispatchers.IO) {

            smsRep.getAllThreads()?.let { data.addAll(it) }

            // shallow copy and local storage of threads
            sqlThreads.clear()
            data.map { sqlThreads.add(it.copy()) }

            viewModelScope.launch(Dispatchers.Default) {
                var loop = true
                while (loop)
                {
//                    l.d("MVM:: GET ALL THREADS WHILE: $loop")
                    if (_contactKeysGood.value!!.first)
                    {
                        if (_contactKeysGood.value!!.second.toInt() != data.size)
                        {
                            l.d(
                                "MVM:: GET ALL THREADS GEN CONTACTS: " +
                                        "${_contactKeysGood.value!!} " +
                                        data.size)
                            genContacts(data)
                        }
                        loop = false
                    }
                    delay(200)
                }
            }
            // Decrypt the messages and post to live data
            if (encSwitch.value!! && _contactKeysGood.value!!.first)
            {
                _threads.postValue(CryptoHelper().deLoop(data, _contactKeysSecMap))
            }
            else
            {
                _threads.postValue(data)
            }

            // Some hardware handles IO operations different; data is missed outside of Main
            viewModelScope.launch(Dispatchers.Main) {
                _contactKeysGood.value = Triple(
                    _contactKeysGood.value!!.first,
                    _contactKeysGood.value!!.second,
                    data.size.toString()
                )
            }
        }
    }

    /**
     * FIND THREAD ID
     */
    fun findThreadId()
    {
        val data = ArrayList<Sms.AppSmsShort>()

        viewModelScope.launch(Dispatchers.IO) {

            tempSms?.address?.let { smsRep.find(it) }!!.let {
                data.addAll(it)
                buildMessage(data)
            }
        }
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
     * GET EC KEY
     */
    private suspend fun getDhKeys()
    {
        ecKey = ecRep.getKey()
//        dhKey = CryptoMagic.generateDHKeys()
//        saveDhKeys(dhKey!!)

        if (ecKey == null)
        {
            l.d("MVM:: $$\\NO EC KEYS YET$$")
            ecKey = CryptoMagic.generateDHKeys()
            saveDhKeys(ecKey!!)
        }
        // Init key agreement
        try
        {
            keyAgree.init(ecKey!!.private)

            // Once the DH key is returned and init, build secrets
            getContactsKeys()

            _dhKeyGood.postValue(true)
        }
        catch (e: InvalidKeyException)
        {
            l.e("MVM:: Error creating private key agreement: $e")
        }
    }

    /**
     * SET DH KEY
     */
    private suspend fun saveDhKeys(
        key: KeyPair
    )
    {
        if (!ecRep.saveKey(key)){l.e("MVM:: ERROR: Failed to save DH KEY")}
    }

    /**
     * GET CONTACTS KEYS
     */
    private suspend fun getContactsKeys()
    {
        // Get keys from database
        _contactKeys.addAll(contactKeyRep.getAll())

        mapContactKeys()

//        genContacts(sqlThreads)

        l.d("MVM:: GETTING CON KEYS, total: ${_contactKeys.size}")
    }

    /**
     * MAP CONTACT KEYS
     */
    private fun mapContactKeys()
    {
        // TODO:: This might be broken with the whole null key thing
        // Map the key ArrayList by thread_id to its secret key
        _contactKeys.forEach {
            _contactKeysSecMap[it.thread_id] = it.publicKey?.let { it1 ->
                CryptoHelper().generateSecret(it1, keyAgree) }
            _contactKeysMap[it.thread_id] = it
        }

//        _contactKeysSecMap = _contactKeys.associate {
//            it.thread_id to it.publicKey?.let { it1 ->
//                KeyHelper().generateSecret(it1, keyAgree) }
//        }
        // Map the key ArrayList by thread_id
//        _contactKeysMap = _contactKeys.associateBy { it.thread_id }

        // Some hardware handles IO operations different; data is missed outside of Main
        viewModelScope.launch(Dispatchers.Main) {
            _contactKeysGood.value = Triple(
                true,
                _contactKeysSecMap.size.toString(),
                _contactKeysGood.value!!.third
            )
        }
        l.d("MVM:: MAPPING CON KEYS, total: ${_contactKeysSecMap.size}")
    }

    /**
     * UPDATE CONTACTS KEY
     */
    private suspend fun updateContactsKey(
        contact: KeyContent.AppKey
    ):Boolean
    {
        return contactKeyRep.update(contact)
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
     * TODO:: Filters could be revamped for a more generic scope
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    private val conFlow: Flow<String> = flow {
        l.d("Flow Run-up")
        Runtime.getRuntime().exec("logcat -c")
        Runtime.getRuntime().exec("logcat")
            .inputStream
            .bufferedReader()
            .useLines { lines ->
                lines.forEach { line ->
                    emit(line.replaceBefore(":::", "", line))
                }
            }
    }
        .filterNot {
            it.startsWith("D/d", false) // KitKat memory cleanup
        }
        .filterNot {
            it.startsWith("D/V", false) // LollyPop screen touches
        }
        .filterNot {
            it.startsWith("I/V", false) // KitKat screen touches
        }
        .filterNot {
            it.startsWith("W/T", false) // KitKat screen touches
        }
        .filterNot {
            it.contains("ViewPost", false) // Samsung sucking screen stuff
        }
        .onStart {
            emit("FLOW START") }
        .flowOn(Dispatchers.Default)

//    private var _text = conFlow.collect()
//    private var _text = conFlow.asLiveData(viewModelScope.coroutineContext + Dispatchers.Default)
//    private var _text = consStream()

    private suspend fun genContacts(
        data: ArrayList<Sms.AppSmsShort>
    )
    {
        // No key storage created yet for contacts
        if (_contactKeys.size == 0)
        {
            for (d in data)
            {
                val key = KeyContent.AppKey()
                key.thread_id = d.thread_id          // thread id


                if (createContactsKey(key))
                {
                    l.d("MVM:: CREATE ALL NEW CONTACT KEYS: ${key.id} ${d.thread_id}")
                    _contactKeys.add(key)
                }
                else
                {
                    l.d("MVM:: ERROR CREATING ALL NEW CONTACT KEY: ${d.address}")
                }
            }
        }
        else // Find and add missing key storage
        {
            for (d in data)
            {
                if (!_contactKeysMap.containsKey(d.thread_id))
                {
                    val key = KeyContent.AppKey()
                    key.thread_id = d.thread_id          // thread id

                    if (createContactsKey(key))
                    {
                        l.d("MVM:: CREATE MISSING CONTACT KEYS: ${key.id} ${d.thread_id}")
                        _contactKeys.add(key)
                    }
                    else
                    {
                        l.d("MVM:: ERROR CREATING MISSING CONTACT KEY: ${d.address}")
                    }
                }
            }
        }
        mapContactKeys()
    }
}