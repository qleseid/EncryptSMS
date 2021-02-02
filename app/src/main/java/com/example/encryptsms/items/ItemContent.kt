package com.example.encryptsms.items

import java.io.Serializable

object ItemContent: Serializable {
    /**
     * A representation of an items content.
     */
    data class AppItem(var id: String, var content: String, var details: String, var icon: String, var amount: Float): Serializable {
        override fun toString(): String = "$id $content $details $icon"
    }
}