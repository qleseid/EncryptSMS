package com.example.encryptsms

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainSharedViewModelTest
{
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    @Test
    fun getApplication()
    {
        val taskViewModel = MainSharedViewModel(ApplicationProvider.getApplicationContext())

        var enc = taskViewModel.encSwitch.value!!
        assertFalse("should be false", enc)
        taskViewModel.setEncryptedToggle(true)
        enc = taskViewModel.encSwitch.value!!
        assertTrue("should be true", enc)
    }

    @Test
    fun getTag()
    {
    }

    @Test
    fun getEncSwitch()
    {
    }

    @Test
    fun getDraft()
    {
    }

    @Test
    fun setDraft()
    {
    }
}