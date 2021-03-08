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

    private var key: SecretKey = SecretKeySpec(
        Base64.decode("o6D4VFKtqu3Gg0CnohMe9nzbbrI9IHPJgVFentvo5nE=", DEFAULT),
        "AES")

    init
    {
        generateKeys()
        l.i("KEY: ${Base64.encodeToString(key.encoded, DEFAULT)}")
    }

    fun encrypt(msg: String): String
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

    fun decrypt(msg: String):String
    {
        val dmsg = Base64.decode(msg, DEFAULT)
        l.d("DECRYPT: $dmsg")

        val cipherD = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipherD.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ByteArray(16)))

        return String(cipherD.doFinal(dmsg))
    }

    private fun generateKeys()
    {
        val keyGen = KeyPairGenerator.getInstance("DiffieHellman")
        val keyPair = keyGen.genKeyPair()
        pub_key = keyPair.public
        pvt_key = keyPair.private

        l.d("PUB GEN: $pub_key")
        l.d("PVT GEN: $pvt_key")

    }
}