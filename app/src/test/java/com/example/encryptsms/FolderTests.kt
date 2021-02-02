package com.example.encryptsms

import android.app.Application
import com.example.encryptsms.utility.LogMe
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

class FolderTests {

    private val l = LogMe()

    @Mock
    private val application = Application()
    private lateinit var viewModel: MainSharedViewModel

    @Before
    fun setup() {
//        viewModel = Mockito.spy(MainSharedViewModel(application))
    }

    @Test
    fun call_get_all(){
//        viewModel.create()

//        l.d("GET ALL: ${viewModel.getAllItems()}")

    }
}