package com.lolson.encryptsms.database

import java.security.KeyPair

/**
 * DH service interface
 * Implement this interface with specific service
 */
interface IDhKeySvc {

    //Gets the key from the database
    fun getKey(): KeyPair?

    // Save DH key
    fun saveKey(key: KeyPair): Boolean

}