package com.example.encryptsms.ui.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * The view model is used to retain data between rebuilds.
 */
class AboutViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Encrypt SMS 2021 \nCreated for class project MSSE 692 \n\nCreated by: Lucas Olson"
    }
    val text: LiveData<String> = _text
}