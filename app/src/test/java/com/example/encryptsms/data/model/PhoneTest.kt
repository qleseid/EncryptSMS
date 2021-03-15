package com.example.encryptsms.data.model

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
        assertSame("Phone initialized correct", phn, phone.mCleanNumber)
        assertSame("Phone initialized correct", phone.mNumber, phone.mCleanNumber)
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
        phone.isCellPhoneNumber("6552441212")?.let {
            assertTrue("Phone number is cell", it) }
    }

    @Test
    fun `numbers match`()
    {
        phone = Phone.pho(id,phnDirty)
        phone.phoneMatch(phnDirty)?.let { assertTrue("Phone number matched", it) }
        phone.phoneMatch(phn)?.let { assertFalse("Phone number shouldn't matched", it) }
    }
}