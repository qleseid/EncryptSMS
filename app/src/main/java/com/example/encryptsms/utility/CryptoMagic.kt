package com.example.encryptsms.utility

import android.util.Base64
import android.util.Base64.DEFAULT
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoMagic
{
    //Logger
    private var l = LogMe()

    private lateinit var pvt_key: PrivateKey
    private lateinit var pub_key: PublicKey

    private var key: SecretKey

    init
    {
        key = SecretKeySpec(
        Base64.decode("o6D4VFKtqu3Gg0CnohMe9nzbbrI9IHPJgVFentvo5nE=", DEFAULT),
        "AES")
        generateKeys()
        l.i("KEY: ${Base64.encodeToString(key.encoded, DEFAULT)}")
    }

    fun encrypt(
        msg: String
    ): String
    {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(ByteArray(16)))

        val cipherD = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipherD.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ByteArray(16)))

        val result = cipher.doFinal(msg.toByteArray())
        l.d("ENCRYPT: $result")
        l.d("ENCRYPT DECRYPT: ${String(cipherD.doFinal(result))}")

        return Base64.encodeToString(result, DEFAULT)
    }

    fun decrypt(
        msg: String
    ):String
    {
        var result = msg
        try
        {
            val dmsg = Base64.decode(msg, DEFAULT)
            l.d("DECRYPT: $dmsg")

            val cipherD = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipherD.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ByteArray(16)))

            result = String(cipherD.doFinal(dmsg))
        }
        catch (e: Exception)
        {
            l.d("DECRYPT ERROR: $e")
        }
        return result
    }

    private fun generateKeys()
    {
        val keyGen = KeyPairGenerator.getInstance("DH")
        val keyPair = keyGen.genKeyPair()
        pub_key = keyPair.public
        pvt_key = keyPair.private
//        val pub_encode = pub_key.encoded

        l.d("PUB GEN: ${Base64.encodeToString(pub_key.encoded, DEFAULT)}")
//        l.d("PVT GEN: ${Base64.encodeToString(pvt_key.encoded, DEFAULT)}")
//        l.d("PUB ENCODE: ${pub_key.encoded == pvt_key.encoded}")
//
//        val revKeyFac = KeyFactory.getInstance("DH")
//        val x509 = X509EncodedKeySpec(pub_encode)
//
//        l.d("PUB RECEIVED: ${Base64.encodeToString(revKeyFac.generatePublic(x509).encoded,
//            DEFAULT)}")

    }
}