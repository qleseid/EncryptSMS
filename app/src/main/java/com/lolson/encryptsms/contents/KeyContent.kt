package com.lolson.encryptsms.contents

import java.io.Serializable
import java.security.PublicKey

object KeyContent: Serializable {
    /**
     * A representation of an keys content.
     */
    data class AppKey(
        var id: String,
        var sent: Boolean,
        var thread_id: String,
        var publicKey: PublicKey
    ): Serializable {
        override fun toString(): String = "Key id: $id"
    }
}