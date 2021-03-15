package com.example.encryptsms.keys

import android.provider.BaseColumns

object KeySQLiteContract {
    //Table contents are grouped in an anonymous object
    object KeyEntry: BaseColumns{
        const val TABLE_NAME = "keys"
        const val COLUMN_NAME_ID = "thread_id"
        const val COLUMN_NAME_PUBLICKEY = "public_key"
    }
}
