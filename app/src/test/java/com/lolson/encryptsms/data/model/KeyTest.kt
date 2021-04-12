package com.lolson.encryptsms.data.model

import com.lolson.encryptsms.contents.KeyContent
import com.lolson.encryptsms.utility.LogMe
import org.junit.After
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import java.security.KeyPairGenerator
import java.util.*
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import kotlin.collections.ArrayList

class KeyTest
{
    private val keyGen = KeyPairGenerator.getInstance("DH")
    private val keyPair = keyGen.genKeyPair()

    private val keygen = KeyGenerator.getInstance("AES")
    private lateinit var key: SecretKey

    private val list = ArrayList<KeyContent.AppKey>()
    private lateinit var map: Map<String, Int>
    private  var key1 = KeyContent.AppKey(
        "1",
        false,
        "21",
        keyPair.public
    )
    private  var key2 = KeyContent.AppKey(
        "2",
        false,
        "32",
        keyPair.public
    )

    //Logger
    private var l = LogMe()

    @Before
    fun setUp()
    {
        list.add(key1)
        list.add(key2)
        map = list.associate { it.thread_id to list.indexOf(it) }

        keygen.init(256)
        key = keygen.generateKey()
    }

    @After
    fun tearDown()
    {
        list.clear()
    }

    @Test
    fun `check map functionality`()
    {
        assertSame("Map to first index", 0, map["21"])
        assertNotSame("Map to second index but get first", 0, map["32"])
    }

    @Test
    fun `compare keys to ensure different`()
    {
        assertSame("List should be same", list[0], list[0])
        assertNotSame("List should be different", list[0], list[1])
    }

    @Test
    fun `compare key thread to ensure different`()
    {
        assertSame("List thread id should be same", list[0].thread_id, list[0].thread_id)
        assertNotSame("List thread id should be different", list[0].thread_id, list[1].thread_id)
    }

    @Test
    fun `test indexing of array on thread id`()
    {
        assertSame("Index Int should return 1", "1", list[map["21"] ?: error("")].id)
        assertNotSame("Index Int should be different from 1", "-1", list[map["21"] ?: error("")].id)
    }

    @Test
    fun `test map return if no match`()
    {
        assertSame("Match in map for key 1", 0, map["21"])
        assertSame("No Match of key in map", null, map["1"])
    }

    @Test
    fun `generate AES security key`()
    {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val aesKey = keyGen.generateKey()

        l.d("AES key: ${Base64.getEncoder().encodeToString(aesKey.encoded)}")
        l.d("AES key from init: ${Base64.getEncoder().encodeToString(key.encoded)}")
        l.d("AES key: ${aesKey.isDestroyed}")

        assertSame("Key should be AES algorithm", "AES", aesKey.algorithm)
    }

    @Test
    fun `generate RSA security key`()
    {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(4096)
        val rsaKey = keyGen.generateKeyPair()

        l.d("RSA private key: ${rsaKey.private.encoded.size}")
        l.d("RSA public key: ${rsaKey.public}")

        assertSame("Key should be RSA algorithm", "X.509", rsaKey.public.format)
        assertSame("Key should be RSA algorithm", "RSA", rsaKey.public.algorithm)
    }
}