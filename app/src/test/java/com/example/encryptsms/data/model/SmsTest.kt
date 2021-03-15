package com.example.encryptsms.data.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertSame
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class SmsTest
{
    private lateinit var msg: Sms.AppSmsShort

    @Before
    fun setUp()
    {
        msg = Sms.AppSmsShort()

//        var id: Int = -1,
//        var thread_id: Long = -1L,
//        var address: String ="",
//        var person: Int? = null,
//        var date: Long = 0L,
//        var date_sent: Long = 0L,
//        var protocol: Int? = null,
//        var read: Int = 0,
//        var status: Int = 0,
//        var type: Int = 0,
//        var reply_path_present: Int? = null,
//        var subject: String? = null,
//        var body: String = "",
//        var service_center: String? = null,
//        var locked: Int = 0,
//        var sub_id: Int = -1,
//        var error_code: Int = -1,
//        var creator: String = "",
//        var seen: Int = 0)
    }

    @After
    fun tearDown()
    {
        msg = Sms.AppSmsShort()
    }

    @Test
    fun `test initialization of sms with compare`()
    {
        assert(msg.comp(), { "Dates are the same" })
    }

    @Test
    fun `test changing data and compare`()
    {
        msg.date_sent = 1L
        assertFalse(msg.comp())
    }

    @Test
    fun `test cleaner`()
    {
        val stg = "Cleaner"
        assertSame("Cleaner",Sms.cleaner(stg), stg)
    }
}