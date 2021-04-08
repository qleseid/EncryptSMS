package com.example.encryptsms.data.model

import com.lolson.encryptsms.data.model.Phone
import junit.framework.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class PhoneTest
{
    private lateinit var phone: Phone.pho
    private val id = "1"
    private val phn = "6552441212"
    private val phnDirty ="+1 800 244 2324"

    @Before
    fun setUp()
    {
        phone = Phone.pho()
    }

    @After
    fun tearDown()
    {
        phone = Phone.pho()
    }

    @Test
    fun `compare input phone numbers`()
    {
        phone.mNumber = phn
        assertSame("Phone Number is", phn, phone.mNumber)
        assertNotSame("Phone Clean Number is", phn, phone.mCleanNumber)
    }

    @Test
    fun `check number is initialization`()
    {
        phone = Phone.pho(id,phn)
        assertSame("Phone initialized correct", phn, phone.mNumber)
        assertSame("Phone initialized correct", phone.mNumber, phone.mNumber)
    }

    @Test
    fun `check number is cleaned during initialization`()
    {
        phone = Phone.pho(id,phnDirty)
        assertNotSame("Phone number cleaned", phnDirty, phone.mCleanNumber)
        assertNotSame("Phone number different from cleaned", phone.mNumber, phone.mCleanNumber)
    }

    @Test
    fun `is cell number`()
    {
        phone.isCellPhoneNumber("6552441212").let {
            if (it != null)
            {
                assertTrue("Phone number is cell", it)
            }
        }
    }

    @Test
    fun `is not a cell number`()
    {
        phone.isCellPhoneNumber("655244121").let {
            if (it != null)
            {
                assertFalse("Phone number is not cell", it)
            }
        }
    }

    @Test
    fun `numbers match`()
    {
        phone = Phone.pho(id,phnDirty)
        phone.phoneMatch(phnDirty).let {
            if (it != null)
            {
                assertTrue("Phone number matched", it)
            }
        }
        phone.phoneMatch(phn).let {
            if (it != null)
            {
                assertFalse("Phone number shouldn't matched", it)
            }
        }
    }
}