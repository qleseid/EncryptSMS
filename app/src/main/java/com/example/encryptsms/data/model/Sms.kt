package com.example.encryptsms.data.model

import java.io.Serializable

object Sms: Serializable
{
    /**
     * A representation of an SMS message
     */
    data class AppSmsShort(var id: Int,
                      var thread_id: Int,
                      var address: String,
                      var person: Int?,
                      var date: Long,
                      var date_sent: Long,
                      var protocol: Int?,
                      var read: Int,
                      var status: Int,
                      var type: Int,
                      var reply_path_present: Int?,
                      var subject: String?,
                      var body: String,
                      var service_center: String?,
                      var locked: Int,
                      var sub_id: Int,
                      var error_code: Int,
                      var creator: String,
                      var seen: Int): Serializable
    {
        override fun toString(): String {
            return "$id:: $thread_id:: $address:: $read"
        }
    }

    /**
     * A representation of a short SMS message
     */
    data class AppSms(var message: String,
                      var number: String,
                      var receiver: String,
                      var date: String): Serializable
    {
        override fun toString(): String {
            return "$date:: $number:: $message"
        }
    }
}



/*
class Sms: Comparable<Sms>
{

    //SMS message model data
    private lateinit var message: String
    private lateinit var shorterMessage: String
    private lateinit var number: String
    private lateinit var sender: String
    private lateinit var to: String
    private lateinit var answerTo: String
    private lateinit var receiver: String
    private lateinit var date: String
    private var resSentIntent by Delegates.notNull<Int>()
    private var resDelIntent by Delegates.notNull<Int>()
    private lateinit var sentIntents: BooleanArray
    private lateinit var delIntents: BooleanArray
    private var id by Delegates.notNull<Int>()

    /**
     * CONSTRUCTOR FOR QUERIED SMS
     */
    constructor(
        phoneNumber: String,
        message: String,
        date: String,
        receiver: String)
    {
        this.number = phoneNumber
        this.message = message
        this.date = date
        this.receiver = receiver
    }

    /**
     * CONSTRUCTOR FOR SENDING SMS
     */
    constructor(
        phoneNumber: String,
        shortMessage: String,
        toName: String,
        answerTo: String,
        numParts: Int,
        id: Int)
    {
        this.number = phoneNumber
        this.shorterMessage = shortMessage
        this.to = toName
        this.answerTo = answerTo
        this.sentIntents = BooleanArray(numParts)
        this.delIntents = BooleanArray(numParts)
        this.id = id
        this.resDelIntent = -1
        this.resSentIntent = -1
        this.date = "Date()"
    }

    /**
     * CONSTRUCTOR FOR MAPPING SMS
     */
    constructor(
        smsId: Int,
        phoneNumber: String,
        name: String,
        shortMessage: String,
        answerTo: String,
        dIntents: String,
        sIntents: String,
        numParts: Int,
        resSIntent: Int,
        resDIntent: Int,
        date: Long)
    {
        this.number = phoneNumber
        this.shorterMessage = shortMessage
        this.to = name
        this.answerTo = answerTo

        //TODO: Might need to fix this array setup
        this.sentIntents = toBoolArray(sIntents)
        this.delIntents = toBoolArray(dIntents)
        this.id = smsId
        this.resDelIntent = resDIntent
        this.resSentIntent = resSIntent
        this.date = date.toString()
    }

    /**
     * GET ID
     */
    fun id(): Int
    {
        return id
    }

    /**
     * BOOLEAN ARRAY BUILDER
     */
    private fun toBoolArray(string: String): BooleanArray
    {
        var temp = BooleanArray(string.length){false}
        var i = 0
        for (ch in string)
        {
            temp[i++] = ch == 'X'
        }
        return temp
    }

    /**
     * SET SENDER
     */
    fun setSender(sender: String)
    {
        this.sender = sender
    }

    /**
     * SMS COMPARE TO
     */
    override fun compareTo(other: Sms): Int {
        return date.compareTo(other.date)
    }

}

 */