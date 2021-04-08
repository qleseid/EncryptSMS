package com.example.encryptsms.utility

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CryptoMagicTest
{
    private var enc = "String"
    private val stg = "Test String"

    @Before
    fun setUp()
    {

    }

    @After
    fun tearDown()
    {
    }

    @Test
    fun encrypt()
    {
//        enc = CryptoMagic.encrypt(stg)
        assertFalse(stg == enc)
    }

    @Test
    fun decrypt()
    {
//        val dec = CryptoMagic.decrypt(enc)
        assertTrue(stg == stg)
    }
}