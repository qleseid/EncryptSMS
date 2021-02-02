package com.example.encryptsms.repository

import android.content.Context
import com.example.encryptsms.database.IItemSvc
import com.example.encryptsms.database.ItemSvcSQLiteImpl
import com.example.encryptsms.items.ItemContent
import com.example.encryptsms.utility.LogMe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemRepository(_context: Context) {

    //Context
    private val context = _context
    //DOA Interface
    private val itemDoa: IItemSvc = ItemSvcSQLiteImpl(context)
    //Logger
    private var l = LogMe()

    /**
     * GET ALL ITEMS
     */
    suspend fun getAll():
            ArrayList<ItemContent.AppItem> {
        val data = ArrayList<ItemContent.AppItem>()
        withContext(Dispatchers.IO){
            itemDoa.getAllItems()?.let { data.addAll(it) }
        }
        l.d("Repository GET ALL")
        return data
    }

    /**
     * CREATE ITEM
     */
    suspend fun create(_item: ItemContent.AppItem):
            Boolean {

        var success = false

        withContext(Dispatchers.IO){
            success = itemDoa.create(_item)
        }
        l.d("Repository CREATE: $success")
        return success
    }

    /**
     * UPDATE ITEM
     */
    suspend fun update(_item: ItemContent.AppItem):
            Boolean {
        var success = false

        withContext(Dispatchers.IO){
            success = itemDoa.update(_item)
        }
        l.d("Repository UPDATE: $success")
        return success
    }

    /**
     * DELETE ITEM
     */
    suspend fun delete(_item: ItemContent.AppItem):
            Boolean {
        var success = false

        withContext(Dispatchers.IO){
            success = itemDoa.delete(_item)
        }
        l.d("Repository DELETE: $success")
        return success
    }
}