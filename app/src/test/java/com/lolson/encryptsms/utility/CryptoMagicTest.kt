package com.lolson.encryptsms.utility

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
    fun `generate ec pair`()
    {
        val ec = CryptoMagic.generateECKeys()
        assertNotNull("EC pair generated", ec)
        assertFalse(stg == enc)
    }
}