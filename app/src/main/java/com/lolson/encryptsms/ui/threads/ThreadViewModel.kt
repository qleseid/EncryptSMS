package com.lolson.encryptsms.ui.threads

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.lolson.encryptsms.contents.KeyContent
import com.lolson.encryptsms.utility.LogMe
import java.io.Serializable

//TODO:: working on persisting data here
class ThreadViewModel(application: Application
): AndroidViewModel(application), Serializable {

    //Context of activity
    private val context = application.applicationContext

    //Holds the keys
    private lateinit var _keys: MutableList<KeyContent.AppKey>

    //Logger
    private var l = LogMe()

    lateinit var keys: MutableList<KeyContent.AppKey>

    //Create a new conversation:: this is only a generic conversation for now
    fun createItem(){

        //Create instance to save keys to file
//        var contactKeyDoa: IContactKeySvc = ContactKeySvcImpl(context)
//        contactKeyDoa.create(KeyContent.AppKey(
//            "9",
//            (System.nanoTime() * .00000001).toString(),
//            "Button created conversation and this is the system nano time: ${System.nanoTime() * .00000001}",
//            "@drawable/handyman_black_24dp",
//            12.57f))
    }

    //Set activity context for local file path
    fun setContext(){
//        KeyContent.setData(context)
//        this._keys  = KeyContent.ITEMS
        this.keys = _keys
        l.d("In Folder View Model setting context ${context.filesDir}")
    }
}