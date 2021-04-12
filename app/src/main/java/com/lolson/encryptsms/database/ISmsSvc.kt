package com.lolson.encryptsms.database

import com.lolson.encryptsms.data.model.Phone
import com.lolson.encryptsms.data.model.Sms

/**
 * SMS service interface
 * Implement this interface with specific service
 */
interface ISmsSvc
{
    // Gets all the messages matching id from the database
    fun getAllMessages(phones: ArrayList<Phone.pho?>): ArrayList<Sms.AppSmsShort>?

    // Gets all the threads from the database
    fun getAllThreads(): ArrayList<Sms.AppSmsShort>?

    // Send an SMS
    fun send(msg: Sms.AppSmsShort): Boolean

    // Find an SMS by its address, return thread id
    fun find(address: String): Long

    // Update SMS in database
    fun update(msg: Sms.AppSmsShort): Boolean

    // Delete a SMS
    fun delete(msg: Sms.AppSmsShort): Boolean
}