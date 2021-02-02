package com.example.encryptsms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.encryptsms.items.ItemContent
import com.example.encryptsms.repository.ItemRepository
import com.example.encryptsms.utility.LogMe
import kotlinx.coroutines.launch

/**
 * Shared view model for main activity fragments
 */
class MainSharedViewModel(application: Application): AndroidViewModel(application) {

    //Context of activity
    private val context = application.applicationContext

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

    var temp_item = ItemContent.AppItem("-1","","","",0.0f)

    //Logger
    private var l = LogMe()

    //Create instance to save items to file
    private val itemRep = ItemRepository(context)

    init {
        l.d("MAIN SHARED VIEW MODEL: init method")
        getAllItems()
        clearTemp()
    }

    /**
     * Change function
     */
    fun changeBool(){
        val temp = _change.value
        _change.value = _change.value.let { !it!! }

        //Get size of the items array
        val itemS = _items.value?.size?.plus(1)

        //New Item
        val newItem = ItemContent.AppItem(
            itemS.toString(),
            "Item $temp",
            "Button created item and this is the system nano time: ${System.nanoTime() * .00000001}",
            "handyman_black_24dp",
            12.57f
        )

        _items.value?.add(newItem)
        //Have to post to trigger the Observer
        _items.postValue(_items.value)

        l.d("Change: ${temp} ${_change.value}")
    }
    /**
     * GET ALL ITEMS
     */
    private fun getAllItems(){

        l.d("MAIN SHARED VIEW MODEL: Get All method")
        //Create a new coroutine to move execution off of UI thread
        viewModelScope.launch {

            l.d("MAIN SHARED VIEW MODEL: GetAll_Coroutine method")
            _items.postValue(itemRep.getAll())

            //Update hash map of items
            hashMap()
        }

    }

    /**
     * CREATE ITEM
     * Create a new item:: this is only a generic item for now
     */
    fun create(){

        //Get size of the items array
        val itemS = _items.value?.size?.plus(1)

        //Set next ID
        l.d("View Create: ${temp_item.content} ${temp_item.amount} ${temp_item.details} ${temp_item.icon} ${temp_item.hashCode()}")

        var newItem = temp_item
        newItem.id = itemS?.minus(1).toString()

        l.d("MAIN SHARED VIEW MODEL: Create Items size start: ${_items.value?.size}")

        var success = false

        //New Item
//        val newItem = ItemContent.AppItem(
//            itemS.toString(),
//            "Item $itemS",
//            "Button created item and this is the system nano time: ${System.nanoTime() * .00000001}",
//            "handyman_black_24dp",
//            12.57f
//        )

        //Create a new coroutine to move execution off of UI thread
        viewModelScope.launch {
            success = itemRep.create(newItem)

            //Add new item to data list
            if (success) {

                if (itemS != null) {
                    _items.value?.add(newItem)
                    //Have to post to trigger the Observer
                    _items.postValue(_items.value)
                }

                //Update hash map of items
                hashMap()
                l.d("Item creation was successful")
                l.d("MAIN SHARED VIEW MODEL: Create Items size end: ${_items.value?.size}")
            }
        }
    }

    /**
     * UPDATE ITEM
     * Update an item
     * @param item: ItemContent.AppItem
     */
    fun update(){

        var success = false

        //Set next ID
        l.d("View UPDATE: ${temp_item.content} ${temp_item.amount} ${temp_item.details} ${temp_item.icon} ${temp_item.hashCode()}")

        var upItem = temp_item
        val pos = upItem.id.toInt().minus(1)
        //Create a new coroutine to move execution off of UI thread
        viewModelScope.launch {
            success = itemRep.update(upItem)


            //Add new item to data list
            if (success) {

                //Find item and replace data with updated info
                _items.value?.set(pos, upItem)
                    l.d(
                        "View UPDATE item: ${
                            _items.value?.get(pos)?.content
                        }" +
                                " ${_items.value?.get(pos)?.amount} " +
                                "${_items.value?.get(pos)?.details} " +
                                "${_items.value?.get(pos)?.icon}"
                    )


                //Have to post to trigger the Observer
                _items.postValue(_items.value)

                //Update hash map of items
                hashMap()
                l.d("Item update was successful")
            }
        }
    }

    /**
     * DELETE ITEM
     * Delete an item
     * @param item: ItemContent.AppItem
     */
    fun delete(item: ItemContent.AppItem){

        var success = false

        //Create a new coroutine to move execution off of UI thread
        viewModelScope.launch {
            success = itemRep.delete(item)


            //Add new item to data list
            if (success) {

                //Find item and delete data
                _itemsMap.value?.get(item.id).let { _items.value?.removeAt(it!!.toInt()) }
                //Have to post to trigger the Observer
                _items.postValue(_items.value)

                //Update hash map of items
                hashMap()
                l.d("Item deletion was successful")
            }
        }
    }

    /**
     * SET ITEM HASH_MAP
     * Sets hashMap so fragments can reference the correct item when clicked on
     */
    private fun hashMap(){

        //Get size of the items array
        val itemS = _items.value?.size
        l.d("VIEWMODEL hashmap method and map size: ${_itemsMap.value?.size} Items: $itemS")


        if (itemS != null) {
            _itemsMap.value?.clear()
            for (i in 0 until itemS) {
                l.d("Items size is $itemS and map size: ${_itemsMap.value?.size}")
                _itemsMap.value?.set(_items.value?.get(i)?.id.toString(), i)
            }
        }
    }

    /**
     * Clear the temp item for new data
     */
    fun clearTemp(){
        temp_item = ItemContent.AppItem("-1","","","",0.0f)
    }
}