package com.lolson.encryptsms.contents

import java.io.Serializable
import java.security.PublicKey

object KeyContent: Serializable {
    /**
     * A representation of an keys content.
     */
    data class AppKey(
        var id: Int = -1,
        var sent: Boolean = false,
        var last_check: Long = -1L,
        var thread_id: Long = -1L,
        var publicKey: PublicKey? = null
    ): Serializable {
        override fun toString(): String = "Key id: $id"
    }
}