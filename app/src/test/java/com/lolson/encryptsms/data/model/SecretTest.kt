package com.lolson.encryptsms.data.model

import com.lolson.encryptsms.contents.KeyContent
import com.lolson.encryptsms.utility.LogMe
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList
import kotlin.experimental.and
import kotlin.experimental.xor

class SecretTest
{
    private val keyAgree = KeyAgreement.getInstance("ECDH")

    private val myKey = generateDHKeys()

    private val list = ArrayList<KeyContent.AppKey>()
    private val contactList = ArrayList<KeyPair>()
    private lateinit var map: Map<Long, SecretKeySpec?>
    private var contactMap = mutableMapOf<Int, SecretKeySpec>()


    //Logger
    private var l = LogMe()

    @Before
    fun setUp()
    {
        // Create some pseudo contacts
        for (con in 0..5)
        {
            val k = generateDHKeys()

            l.d("ST:: EC KEY PAIR PRIVATE: ${k.private.encoded.size} PUBLIC:${k.public.encoded.size}")
            contactList.add(k)
            contactMap[con] = generateSecret(k, myKey.public)
            list.add(
                KeyContent.AppKey(
                    con,
                    false,
                    1L,
                    con.toLong(),
                    k.public
                )
            )
        }
        // Map the ArrayList with secrets generated
        map = list.associate { it.thread_id to it.publicKey?.let { it1 ->
            generateSecret(myKey,
                it1)
        } }
    }

    /**
     * GENERATE SECRET FOR CONTACT
     */
    private fun generateSecret(
        pKey: KeyPair,
        key: PublicKey
    ):SecretKeySpec
    {
//        l.d("^^^^^^^GENERATING SECRET^^^^^^")
        keyAgree.init(pKey.private)
        keyAgree.doPhase(key, true)
        return SecretKeySpec(
            keyAgree.generateSecret(),
            0,
            32,
            "AES"
        )
    }

    /**
     * GENERATE DH KEYS
     */
    private fun generateDHKeys()
            : KeyPair
    {
        val keyGen = KeyPairGenerator.getInstance("EC")
//        l.d("ST:: EC KEY PAIR GENERATOR: ${Security.getProvider("AndroidOpenSSL version 1.0").info}")
        return keyGen.genKeyPair()
    }

    /**
     * ENCRYPT
     */
    private fun encrypt(
        msg: String,
        iv: ByteArray,
        key: SecretKeySpec
    ): String
    {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))

        val result = cipher.doFinal(msg.toByteArray())

        val res = Base64.getEncoder().encodeToString(result)
        l.d("ST:: ENCRYPT: $res")
        return res
    }

    /**
     * DECRYPT
     */
    private fun decrypt(
        msg: String,
        iv: ByteArray,
        key: SecretKeySpec
    ):String
    {
        var result = msg
        try
        {
            l.d("ST:: DECRYPT BEFORE: $msg SIZE:${msg.length}")
            val dmsg = Base64.getDecoder().decode(msg)

            val cipherD = Cipher.getInstance("AES/GCM/NoPadding")
            cipherD.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))

            result = String(cipherD.doFinal(dmsg))
        }
        catch (e: Exception)
        {
            l.d("ST:: DECRYPT ERROR: $e")
        }

        l.d("ST:: DECRYPT AFTER: $result SIZE:${result.length}")

        return result
    }

    @After
    fun tearDown()
    {
//        for ((con, m) in map)
//        {
//            l.d("MAP CONTENTS TEARDOWN: $con ${m.encoded.size}")
//        }

        list.clear()
    }

    @Test
    fun `check map functionality`()
    {
        assertNotSame("Map to second index but get first", 0, map[1])
    }

    @Test
    fun `compare list to ensure different`()
    {
        assertEquals("List should be same", list[0], list[0])
        assertNotEquals("List should be different", list[0], list[1])
    }

    @Test
    fun `compare list by thread to ensure different`()
    {
        assertEquals("List thread id should be same", list[0].thread_id, list[0].thread_id)
        assertNotEquals("List thread id should be different", list[0].thread_id, list[1].thread_id)
    }

    @Test
    fun `compare list by keys to ensure different`()
    {
        assertEquals("List thread id should be same", list[0].publicKey, list[0].publicKey)
        assertNotEquals("List thread id should be different", list[0].publicKey, list[1].publicKey)
    }

    @Test
    fun `test indexing of array on thread id`()
    {
        assertEquals("Index Int should return 1", map[0], map[list[0].thread_id])
        assertNotEquals("Index Int should be different from 1", map[0], map[list[1].thread_id])
    }

    @Test
    fun `test map return if no match`()
    {
        assertEquals("No Match of key in map", null, map[21])
    }

    @ExperimentalUnsignedTypes
    @Test
    fun `test encryption and decryption with same key`()
    {
        val msg = "Hello World"
//        var primer = System.currentTimeMillis()* "8014461212".toLong() / 73
//        var primer = 70750851580529911.toString().encodeToByteArray()
//        val iv = 70750851580529911.toString().encodeToByteArray()
        val primer = (System.currentTimeMillis()* "8014461212".toLong() / 73).toString()
        .encodeToByteArray()
        val iv = ByteArray(12)

        l.d("ST:: TIME: '${primer.toUByteArray()}' LENGTH: '${primer.size} IV: '${iv.toUByteArray
            ()}'")

        // Create the IV
        for (con in iv.indices)
        {
            val bite = ((primer[con].xor(((con + 1) * 123).toByte()))and 0b11111111.toByte())
            l.d("ST:: BYTE FILLER: '${bite}' position: '$con' IV: ${iv[con]}")

            iv[con] = bite
        }

        var msg1 = map[0]?.let { encrypt(msg, iv, it) }
        var msg2 = map[1]?.let { encrypt(msg, iv, it) }

        assertEquals("Message is: Hello World", msg, msg)
        assertNotEquals("Message should not be words", msg1, msg2)
        assertNotEquals("Message should not be words", msg, msg1)
        assertNotEquals("Message should not be words", msg, msg2)

        msg1 = map[0]?.let { msg1?.let { it1 -> decrypt(it1, iv, it) } }
        msg2 = map[1]?.let { msg2?.let { it2 -> decrypt(it2, iv, it) } }

        assertEquals(
            "Decrypted messages is: Hello World",
            msg1,
            msg2
        )
        assertEquals(
            "Decrypted message back to original Hello World",
            msg,
            msg2
        )
    }

    @ExperimentalUnsignedTypes
    @Test
    fun `test encryption with my key and decryption with theirs`()
    {
        val msg = "Hello World"
        val time = System.currentTimeMillis()
        val primer = ((time shl 6) + (time shl 3) + time).toString().encodeToByteArray()
        val iv = ByteArray(12)

        l.d("ST:: TIME: '${primer.toUByteArray()}' LENGTH: '${primer.size} IV: '${iv.toUByteArray
            ()}'")

        // Create the IV
//        for (con in 0..11)
        for (con in iv.indices)
        {
            val pri = primer[primer.size - (con + 1)]
            val bite = ((pri.xor(((con + 1) * 123).toByte())) and 0xFF
                .toByte())
            l.d("ST:: PRIMER: '${pri} 'BYTE FILLER: '${bite}' position: '$con' IV: ${iv[con]}")

            iv[con] = bite
        }

        // Encrypt with my generated secret from contacts public key
        var msg1 = map[0]?.let { encrypt(msg, iv, it) }
        var msg2 = map[1]?.let { encrypt(msg, iv, it) }

        assertEquals("Message is: Hello World", msg, msg)
        assertNotEquals("Message should not be words", msg1, msg2)
        assertNotEquals("Message should not be words", msg, msg1)
        assertNotEquals("Message should not be words", msg, msg2)

        // Decrypt with contacts generated secret key
        msg1 = contactMap[0]?.let { msg1?.let { it1 -> decrypt(it1, iv, it) } }
        msg2 = contactMap[1]?.let { msg2?.let { it2 -> decrypt(it2, iv, it) } }

        assertEquals(
            "Decrypted messages is: Hello World",
            msg1,
            msg2
        )
        assertEquals(
            "Decrypted message back to original Hello World",
            msg,
            msg2
        )
    }
}