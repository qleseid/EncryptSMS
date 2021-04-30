package com.lolson.encryptsms.utility

import android.util.Base64
import android.util.Base64.DEFAULT
import com.lolson.encryptsms.data.model.Sms
import java.nio.ByteBuffer
import java.security.KeyPair
import java.security.KeyPairGenerator
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoMagic
{
    //Logger
    private var l = LogMe()
    private var IV = 12

    init
    {
        l.d("CM:: CRYPTO MAGIC")
    }

    /**
     * ENCRYPT
     */
    fun encrypt(
        msg: Sms.AppSmsShort,
        key: SecretKeySpec?
    ): String
    {
        l.d("CM:: ENCRYPT: ${msg.body.length}")
        var result = msg.body.encodeToByteArray()
        lateinit var cipherText: ByteBuffer
        try
        {
//            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
//            cipher.init(Cipher.ENCRYPT_MODE, key)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
//            val resultG = cipherG.doFinal(result)
//            l.d("ENCRYPT RES: ${Base64.encodeToString(resultG, DEFAULT).length} ::")

            result = cipher.doFinal(result)

            // Add IV to message
            val iv = cipher.iv
            IV = iv.size
            cipherText = ByteBuffer.allocate(1 + IV + result.size)

            cipherText.put(iv)

            cipherText.put(IV.toByte())

            cipherText.put(result)

            result = cipherText.array()

//            l.d("ENCRYPT RESULTS: ${Base64.encodeToString(result, DEFAULT).length} ::" +
//                    " ${Base64.encodeToString(cipher.iv, DEFAULT)} : ${result[0]}")
        }
        catch (e: Exception)
        {
            l.d("CM:: ENCRYPT ERROR: $e")
        }
//        val temp: Sms.AppSmsShort = msg.copy()
//        temp.body = Base64.encodeToString(result, DEFAULT)
//        decrypt(temp, key)
        return Base64.encodeToString(result, DEFAULT)
    }

    /**
     * DECRYPT
     */
    fun decrypt(
        msg: Sms.AppSmsShort,
        key: SecretKeySpec?
    ):String
    {
        var result = msg.body
        try
        {

//            l.d("DECRYPT INT: ${result[IV].toByte().toInt()}")
//            l.d("DECRYPT BYTE: ${Base64.decode(result, DEFAULT)[IV].toByte()}")
//            l.d("DECRYPT STRING: ${result[IV]}")
            if(result.length > IV && Base64.decode(result, DEFAULT)[IV].toInt() == IV)
            {
                val dmsg = Base64.decode(msg.body, DEFAULT)

                // Might change this, putting the iv size in the middle might be a problem for
                // decoupling and cohesiveness
                val ivSize = dmsg[IV].toInt()

//                l.d("CM:: DECRYPT: ${msg.body.length}")
//            val cipherD = Cipher.getInstance("AES/CBC/PKCS5PADDING")
//            cipherD.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(dmsg, 0, ivSize))
                val cipherD = Cipher.getInstance("AES/GCM/NoPadding")

                // Pull IV out of message
                cipherD.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, dmsg, 0, ivSize))

                // Use only the message part
                result = String(cipherD.doFinal(dmsg, 1 + ivSize, dmsg.size - (1 + ivSize)))
            }
        }
        catch (e: Exception)
        {
//            l.d("CM:: DECRYPT ERROR: $e")
        }
//        l.d("DECRYPT RESULTS: $result")
        return result
    }

    /**
     * GENERATE DH KEYS
     */
    fun generateECKeys()
            :KeyPair
    {
        val keyGen = KeyPairGenerator.getInstance("EC")
        return keyGen.genKeyPair()

    }
}