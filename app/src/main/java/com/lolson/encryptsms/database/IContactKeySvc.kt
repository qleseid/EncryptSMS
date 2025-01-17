package com.lolson.encryptsms.database

import com.lolson.encryptsms.contents.KeyContent

/**
 * Contact key service interface
 * Implement this interface with specific service
 */
interface IContactKeySvc {

    // Gets all the keys from the database
    fun getAllKeys(): ArrayList<KeyContent.AppKey>?

    // Create a new contact key store
    fun create(key: KeyContent.AppKey): Long

    // Update an existing contact key
    fun update(key: KeyContent.AppKey): Boolean

    // Delete a contact key
    fun delete(key: KeyContent.AppKey): Boolean
}