package com.example.encryptsms

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lolson.encryptsms.MainSharedViewModel
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainSharedViewModelTest
{
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var taskViewModel: MainSharedViewModel

    @Before
    fun setup()
    {
        taskViewModel = MainSharedViewModel(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown()
    {
    }

    @Test
    fun getApplication()
    {
        val enc = taskViewModel.encSwitch.value!!
        assertFalse("should be false ", enc)
    }

    @Test
    fun `set the encrypted toggle`()
    {
        taskViewModel.setEncryptedToggle(true)
        val enc = taskViewModel.encSwitch.value!!
        assertTrue("should be true", enc)
    }
}