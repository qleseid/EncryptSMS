package com.example.encryptsms.ui.threads

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.encryptsms.database.IItemSvc
import com.example.encryptsms.database.ItemSvcImpl
import com.example.encryptsms.items.ItemContent
import com.example.encryptsms.utility.LogMe
import java.io.Serializable

//TODO:: working on persisting data here
class ThreadViewModel(application: Application
): AndroidViewModel(application), Serializable {

    //Context of activity
    private val context = application.applicationContext

    //Holds the items
    private lateinit var _items: MutableList<ItemContent.AppItem>

    //Logger
    private var l = LogMe()

    lateinit var items: MutableList<ItemContent.AppItem>

    //Create a new conversation:: this is only a generic conversation for now
    fun createItem(){

        //Create instance to save items to file
        var itemDoa: IItemSvc = ItemSvcImpl(context)
        itemDoa.create(ItemContent.AppItem(
            "9",
            (System.nanoTime() * .00000001).toString(),
            "Button created conversation and this is the system nano time: ${System.nanoTime() * .00000001}",
            "@drawable/handyman_black_24dp",
            12.57f))
    }

    //Set activity context for local file path
    fun setContext(){
//        ItemContent.setData(context)
//        this._items  = ItemContent.ITEMS
        this.items = _items
        l.d("In Folder View Model setting context ${context.filesDir}")
    }
}