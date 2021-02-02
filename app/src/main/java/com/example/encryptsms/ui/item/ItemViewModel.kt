package com.example.encryptsms.ui.item

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ItemViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to Balance Book \n\n"
    }
    val text: LiveData<String> = _text
}