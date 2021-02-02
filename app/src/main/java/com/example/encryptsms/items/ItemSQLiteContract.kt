package com.example.encryptsms.items

import android.provider.BaseColumns

object ItemSQLiteContract {
    //Table contents are grouped in an anonymous object
    object ItemEntry: BaseColumns{
        const val TABLE_NAME = "items"
        const val COLUMN_NAME_CONTENT = "content"
        const val COLUMN_NAME_DETAILS = "details"
        const val COLUMN_NAME_ICON = "icon"
        const val COLUMN_NAME_AMOUNT = "amount"
    }
}