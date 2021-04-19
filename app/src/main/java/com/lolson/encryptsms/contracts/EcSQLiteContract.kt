package com.lolson.encryptsms.contracts

import android.provider.BaseColumns

object EcSQLiteContract {
    //Table contents are grouped in an anonymous object
    object KeyEntry: BaseColumns{
        const val TABLE_NAME = "dh_keys"
        const val COLUMN_NAME_ID = "dh_id"
        const val COLUMN_NAME_PUBLICKEY = "public_key"
        const val COLUMN_NAME_PRIVATEKEY = "private_key"
    }
}
