package com.lolson.encryptsms.modelviewhelpers

import android.util.Base64
import android.util.Base64.DEFAULT
import com.lolson.encryptsms.contents.KeyContent
import com.lolson.encryptsms.data.model.Sms
import com.lolson.encryptsms.utility.LogMe
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.spec.SecretKeySpec

class KeyHelper
{
    // Logger
    private var l = LogMe()

    /**
     * CHECK IF INVITE WAS SENT
     */
    fun checkInviteSent(
        thread_id: Long,
        list: ArrayList<KeyContent.AppKey>
    ):Boolean
    {
        l.d("KH:: CHECK INVITE SENT: $thread_id")

        // -1 thread id indicates a new object with zero data
        // so no need to check
        var result = (thread_id != -1L)

        // Find the result of selected thread_id
        if (result)
        {
            for (k in list)
            {
                if (k.thread_id == thread_id)
                {
                    result = k.sent
                }
            }
        }
        l.d("KH:: FOUND INVITE RESULT: $result")
        return result
    }

    /**
     * CHECK FOR ENCRYPTION KEY
     */
    fun checkForEncryptionKey(
        thread_id: Long,
        key_map: Map<Long, SecretKeySpec?>
    ):Boolean
    {
        var result = false

        // Null means no keys
        if(key_map[thread_id] != null)
        {
            result = key_map.containsKey(thread_id)
            l.d("KH:: CHECK FOR ENCRYPTION KEY: $result")
        }
        return result
    }

    /**
     * CHECK FOR KEY IN MESSAGE
     */
    fun keyMessageFinder(
        msgs: ArrayList<Sms.AppSmsShort>,
        last: Long
    ):Sms.AppSmsShort
    {
        l.d("KH:: CHECK FOR KEY IN MESSAGE")

        var result = Sms.AppSmsShort()

        for (msg in msgs)
        {
            // No need to check message that have already been looked at
            if (msg.date > last)
            {
                // Find key and look at received only (type = 1)
                if (msg.body.startsWith("$" + "CILBUP") && msg.type == 1)
                {
                    result = msg

                    l.d("KH:: KEY FOUND AT: ${msg.id}")
                }
            }
        }
        return result
    }

    /**
     * TRIM AND GENERATE PUBLIC KEY FROM MESSAGE
     */
    fun trimMessageAndGenerateKey(
        msg: Sms.AppSmsShort
    ):PublicKey?
    {
        // Trim '$CILBUP' and 'DNE$' from the message
        val temp = msg.body.substring(7, msg.body.lastIndex - 4)
        var key : PublicKey? = null

        l.d("KH:: TRIMMED MSG TO: $temp")

        try
        {
            // Decode string back to ByteArray
            val decoded = Base64.decode(temp, DEFAULT)

            // Create public key from the ByteArray
            key = KeyFactory.getInstance("EC").generatePublic(X509EncodedKeySpec(decoded))
        }
        catch (e: Exception)
        {
            l.e("KH:: TRIM AND KEY GENERATION ERROR: $e")
        }
        return key
    }

    /**
     * BUILD SECRET FROM MESSAGE
     */
    fun buildContactKey(
        pubKey: PublicKey?,
        msg: Sms.AppSmsShort,
        _contactKeysMap:
        MutableMap<Long, KeyContent.AppKey>
    ):KeyContent.AppKey
    {
        var result = KeyContent.AppKey()

        if (pubKey != null)
        {
            try
            {
                // Update the maps and stores
                result = _contactKeysMap.getValue(msg.thread_id)
                result.publicKey = pubKey
                result.last_check = msg.date
            }
            catch (e: Exception)
            {
                l.e("KH:: BUILD SECRET ERROR: $e")
            }
        }
        return result
    }
}