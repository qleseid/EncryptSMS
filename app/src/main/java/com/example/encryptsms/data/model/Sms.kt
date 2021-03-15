package com.example.encryptsms.data.model

import java.io.Serializable

object Sms: Serializable
{
    /**
     * A representation of an SMS message
     */
    data class AppSmsShort(
        var id: Int = -1,
        var thread_id: Long = -1L,
        var address: String ="",
        var person: Int? = null,
        var date: Long = 0L,
        var date_sent: Long = 0L,
        var protocol: Int? = null,
        var read: Int = 0,
        var status: Int = 0,
        var type: Int = 0,
        var reply_path_present: Int? = null,
        var subject: String? = null,
        var body: String = "",
        var service_center: String? = null,
        var locked: Int = 0,
        var sub_id: Int = -1,
        var error_code: Int = -1,
        var creator: String = "",
        var seen: Int = 0
    ): Serializable
    {
        override fun toString(): String {
            return "$id:: $thread_id:: $address:: $read"
        }

        fun comp():Boolean
        {
            return date == date_sent
        }
    }

    fun cleaner(number: String): String
    {
        return number
    }
}