package com.lolson.encryptsms.modelviewhelpers

import android.util.Base64
import android.util.Base64.DEFAULT
import com.lolson.encryptsms.data.model.Sms
import com.lolson.encryptsms.utility.CryptoMagic
import com.lolson.encryptsms.utility.LogMe
import java.security.PublicKey
import javax.crypto.KeyAgreement
import javax.crypto.spec.SecretKeySpec

class CryptoHelper
{
    // Logger
    private var l = LogMe()

    /**
     * MESSAGE DECRYPT LOOP
     */
    fun deLoop(
        data: ArrayList<Sms.AppSmsShort>,
        key_map: Map<Long, SecretKeySpec?>
    ):ArrayList<Sms.AppSmsShort>
    {
        val result = ArrayList<Sms.AppSmsShort>()
        data.map { result.add(it.copy()) }

        l.d("CH:: DECRYPTING START:")
        // con = count, d = individual data
        for ((con, d) in result.withIndex())
        {
            key_map[d.thread_id]?.let {

                result[con].body = CryptoMagic.decrypt( d, it)
            }
//                l.d("DECRYPTING END: ${data[con].body}")
        }
        return result
    }

    /**
     * GENERATE SECRET FOR CONTACT
     */
    fun generateSecret(
        key: PublicKey,
        keyAgree: KeyAgreement
    ):SecretKeySpec?
    {
        var result: SecretKeySpec? = null
        try
        {
            l.d("CH:: ^^^^^^^GENERATING SECRET^^^^^^ " +
                    Base64.encodeToString(key.encoded,DEFAULT))
            keyAgree.doPhase(key, true)
            result = SecretKeySpec(
                keyAgree.generateSecret(),
                0,
                32,
                "AES"
            )
        }
        catch (e: Exception)
        {
            l.e("KH:: GENERATE SECRET ERROR: $e")
        }
        return result
    }
}