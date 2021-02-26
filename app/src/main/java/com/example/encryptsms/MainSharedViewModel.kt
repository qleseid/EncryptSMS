package com.example.encryptsms

import android.app.Application
import android.provider.Telephony
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.encryptsms.data.model.Phone
import com.example.encryptsms.data.model.Sms
import com.example.encryptsms.items.ItemContent
import com.example.encryptsms.repository.ItemRepository
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

    // SMS permission request
    private val READ_SMS_PERMISSIONS_REQUEST = 1

    // Threads
    private var _threads: MutableLiveData<ArrayList<Sms.AppSmsShort>?> = MutableLiveData()
    val threads: LiveData<ArrayList<Sms.AppSmsShort>?>
        get() = _threads

    // Messages
    private var _messages: MutableLiveData<ArrayList<Sms.AppSmsShort>?> = MutableLiveData()
    val messages: LiveData<ArrayList<Sms.AppSmsShort>?>
        get() = _messages


    //Items and map
    private var _items: MutableLiveData<ArrayList<ItemContent.AppItem>> = MutableLiveData()
    val items: LiveData<ArrayList<ItemContent.AppItem>>
        get() = _items

    private var _itemsMap: MutableLiveData<MutableMap<String, Int>> = MutableLiveData()
    val itemsMap: LiveData<MutableMap<String, Int>>
            get() = _itemsMap

    private var _change: MutableLiveData<Boolean> = MutableLiveData(false)
    val change: LiveData<Boolean>
        get() = _change

    lateinit var tempSms: Sms.AppSmsShort
    lateinit var draftSms: Sms.AppSmsShort

    lateinit var draft: Telephony.Mms.Draft
    //Logger
    private var l = LogMe()

    //Create instance to save items to file
    private val itemRep = ItemRepository(context)

    init
    {
        l.d("MAIN SHARED VIEW MODEL: init method")

        //Get all messages
        getAllThreads()
        clearTemp()
    }

    /**
     * REFRESH THREADS
     */
    fun refresh()
    {
        l.d("MAIN SHARED VIEW MODEL: REFRESH")

        getAllThreads()
        getAllMessages()
    }

    /**
     * ADD MESSAGE
     */
    fun addMsgToConvo(msg: String)
    {
        var data = ArrayList<Sms.AppSmsShort>()
        // Creates shallow copy of objects
        _messages.value?.let { data = it.clone() as ArrayList<Sms.AppSmsShort> }

        // Debug
        if (data != null)
        {
            for (i in data)
            {
                l.d("LOOP IN MESSAGE: ${i.body}")
            }
        }

        // Build the message
        tempSms.body = msg
        tempSms.status = 0
        tempSms.type = 2
        tempSms.date = System.currentTimeMillis()
        tempSms.date_sent = tempSms.date
        tempSms.creator = "com.example.encryptsms"

        // Copy to make new object: fixes reference copy issues
        draftSms = tempSms.copy()

        // Add to end of list
        data.add(data.lastIndex+1, draftSms)

        // Populate messages live data to refresh recycler
        _messages.postValue(data)
    }

    /**
     * GET ALL MESSAGES FOR CONTACT STORED IN TEMP SMS
     */
    fun getAllMessages()
    {

        l.d("MAIN SHARED VIEW MODEL: Get All Messages")
        val data = ArrayList<Sms.AppSmsShort>()

        // Create a new coroutine to move execution off of UI thread
        viewModelScope.launch(Dispatchers.IO) {

            l.d("MAIN SHARED VIEW MODEL: Get All Messages_coroutine method")
            var smsM = com.example.encryptsms.data.manager.SmsManager(context)

            // Use thread ID and address to get all the messages
            smsM.getSms(arrayListOf(
                Phone(tempSms.thread_id.toString(), tempSms.address)))?.let { data.addAll(it) }
//            smsM.getConvoThreads()?.let { data.addAll(it) }

            l.d("GET ALL MESSAGES: DATA SIZE:: ${data.size}")

            _messages.postValue(data)
        }


//        val temp = _change.value
//        _change.value = _change.value.let { !it!! }
//
//        //Get size of the items array
//        val itemS = _items.value?.size?.plus(1)
//
//        //New Item
//        val newItem = ItemContent.AppItem(
//            itemS.toString(),
//            "Item $temp",
//            "Button created conversation and this is the system nano time: ${System.nanoTime() * .00000001}",
//            "handyman_black_24dp",
//            12.57f
//        )
//
//        _items.value?.add(newItem)
//        //Have to post to trigger the Observer
//        _items.postValue(_items.value)
//
//        l.d("Change: ${temp} ${_change.value}")
    }

    /**
     * GET ALL THREADS
     */
    private fun getAllThreads()
    {
        l.d("MAIN SHARED VIEW MODEL: Get All THREADS")
        val data = ArrayList<Sms.AppSmsShort>()

        //Create a new coroutine to move execution off of UI thread
        viewModelScope.launch(Dispatchers.IO) {

            l.d("MAIN SHARED VIEW MODEL: Get All Threads_coroutine method")
            var smsM = com.example.encryptsms.data.manager.SmsManager(context)

//            smsM.getLastSms()?.let { data.addAll(it) }
            smsM.getConvoThreads()?.let { data.addAll(it) }

            l.d("GET ALL THREADS: DATA SIZE:: ${data.size}")

            _threads.postValue(data)
        }
    }

    /**
     * GET ALL ITEMS
     */
    private fun getAllItems()
    {

//        l.d("MAIN SHARED VIEW MODEL: Get All ITEMS method")
//        //Create a new coroutine to move execution off of UI thread
//        viewModelScope.launch {
//
//            l.d("MAIN SHARED VIEW MODEL: GetAllITEMS_Coroutine method")
//            _items.postValue(itemRep.getAll())
//
//            l.d("GET ALL ITEMS: SIZE:: ${items.value?.size}")
//            l.d("GET ALL _ITEMS: SIZE:: ${_items.value?.size}")
//            //Update hash map of items
//            hashMap()
//        }

    }

    /**
     * CREATE ITEM
     * Create a new conversation:: this is only a generic conversation for now
     */
    fun create(){

//        //Get size of the items array
//        val itemS = _items.value?.size?.plus(1)
//
//        //Set next ID
//        l.d("View Create: ${tempSms.content} ${tempSms.amount} ${tempSms.details} ${tempSms.icon} ${tempSms.hashCode()}")
//
//        var newItem = tempSms
//        newItem.id = itemS?.minus(1).toString()
//
//        l.d("MAIN SHARED VIEW MODEL: Create Items size start: ${_items.value?.size}")
//
//        var success = false

        //New Item
//        val newItem = ItemContent.AppItem(
//            itemS.toString(),
//            "Item $itemS",
//            "Button created conversation and this is the system nano time: ${System.nanoTime() * .00000001}",
//            "handyman_black_24dp",
//            12.57f
//        )

//        //Create a new coroutine to move execution off of UI thread
//        viewModelScope.launch {
//            success = itemRep.create(newItem)
//
//            //Add new conversation to data list
//            if (success) {
//
//                if (itemS != null) {
//                    _items.value?.add(newItem)
//                    //Have to post to trigger the Observer
//                    _items.postValue(_items.value)
//                }
//
//                //Update hash map of items
//                hashMap()
//                l.d("Item creation was successful")
//                l.d("MAIN SHARED VIEW MODEL: Create Items size end: ${_items.value?.size}")
//            }
//        }
    }

    /**
     * UPDATE ITEM
     * Update an conversation
     * @param conversation: ItemContent.AppItem
     */
    fun update(){

//        var success = false
//
//        //Set next ID
//        l.d("View UPDATE: ${tempSms.content} ${tempSms.amount} ${tempSms.details} ${tempSms.icon} ${tempSms.hashCode()}")
//
//        var upItem = tempSms
//        val pos = upItem.id.toInt().minus(1)
//        //Create a new coroutine to move execution off of UI thread
//        viewModelScope.launch {
//            success = itemRep.update(upItem)
//
//
//            //Add new conversation to data list
//            if (success) {
//
//                //Find conversation and replace data with updated info
//                _items.value?.set(pos, upItem)
//                    l.d(
//                        "View UPDATE conversation: ${
//                            _items.value?.get(pos)?.content
//                        }" +
//                                " ${_items.value?.get(pos)?.amount} " +
//                                "${_items.value?.get(pos)?.details} " +
//                                "${_items.value?.get(pos)?.icon}"
//                    )
//
//
//                //Have to post to trigger the Observer
//                _items.postValue(_items.value)
//
//                //Update hash map of items
//                hashMap()
//                l.d("Item update was successful")
//            }
//        }
    }

    /**
     * DELETE ITEM
     * Delete an conversation
     * @param item: ItemContent.AppItem
     */
    fun delete(item: ItemContent.AppItem){

//        var success = false
//
//        //Create a new coroutine to move execution off of UI thread
//        viewModelScope.launch {
//            success = itemRep.delete(item)
//
//
//            //Add new conversation to data list
//            if (success) {
//
//                //Find conversation and delete data
//                _itemsMap.value?.get(item.id).let { _items.value?.removeAt(it!!.toInt()) }
//                //Have to post to trigger the Observer
//                _items.postValue(_items.value)
//
//                //Update hash map of items
//                hashMap()
//                l.d("Item deletion was successful")
//            }
//        }
    }

    /**
     * SET ITEM HASH_MAP
     * Sets hashMap so fragments can reference the correct conversation when clicked on
     */
    private fun hashMap(){

        //Get size of the items array
//        val itemS = _items.value?.size
//        l.d("Main VIEWMODEL hashmap method and map size: ${_itemsMap.value?.size} Items: $itemS")
//
//
//        if (itemS != null) {
//            _itemsMap.value?.clear()
//            for (i in 0 until itemS) {
//                l.d("Items size is $itemS and map size: ${_itemsMap.value?.size}")
//                _itemsMap.value?.set(_items.value?.get(i)?.id.toString(), i)
//            }
//        }
    }

    /**
     * Clear the temp conversation for new data
     */
    fun clearTemp(){
//        tempSms = ItemContent.AppItem("-1", "", "", "", 0.0f)
    }
}