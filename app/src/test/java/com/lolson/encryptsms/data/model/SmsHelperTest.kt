package com.lolson.encryptsms.data.model

import com.lolson.encryptsms.modelviewhelpers.SmsHelper
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SmsHelperTest
{
    private var sms1: Sms.AppSmsShort? = null

    @Before
    fun setUp()
    {
        sms1 = Sms.AppSmsShort()
    }

    @After
    fun tearDown()
    {
        sms1 = null
    }

    @Test
    fun `build an sms and test thread_id is correct`()
    {
        Assert.assertEquals("SMS is initialized", -1L, sms1?.thread_id)
    }

    @Test
    fun `change thread_id and confirm`()
    {
        sms1?.thread_id = 15L
        Assert.assertEquals("Thread_id changed and correctly 15", 15L, sms1?.thread_id)
    }

    @Test
    fun `change read and confirm`()
    {
        Assert.assertEquals("Read is initialized", 0, sms1?.read)
        sms1?.read = 1
        Assert.assertEquals("Read changed and correctly 1", 1, sms1?.read)
    }

    @Test
    fun `build an sms`()
    {
        Assert.assertEquals("Read is initialized", 0, sms1?.read)
        sms1?.let { SmsHelper().buildSmsMessage("Hello", it) }
        Assert.assertEquals("Read changed and correctly 1", 1, sms1?.read)
        Assert.assertEquals("Body changed and correctly is Hello", "Hello", sms1?.body)
    }
}