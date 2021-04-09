package com.lolson.encryptsms

import com.lolson.encryptsms.data.model.Sms
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

class MainSharedViewModelTest
{

    private var sms1 = Sms.AppSmsShort()
    private var sms2 = Sms.AppSmsShort()

    @Before
    fun setup()
    {
        // Give unique id
        sms1.id = 1
        sms2.id = 2
    }

    @After
    fun tearDown()
    {
        // Remove any data
        sms1 = Sms.AppSmsShort()
        sms2 = Sms.AppSmsShort()
    }

    @Test
    fun `proper copy of sms object`()
    {
        assertNotEquals("The ids should be different: 1, 2", sms1.id, sms2.id)
        assertEquals("The thread_id should be the same: -1, -1", sms1.thread_id, sms2.thread_id)

        // Reference copy
        sms1 = sms2

        // id sees same id reference address
        sms2.id = 3

        assertEquals("The id should be the same after copy: 3, 3", sms1.id, sms2.id)
    }
}