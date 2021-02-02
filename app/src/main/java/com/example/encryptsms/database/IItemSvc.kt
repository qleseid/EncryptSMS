package com.example.encryptsms.database

import com.example.encryptsms.items.ItemContent

/**
 * Item service interface
 * Implement this interface with specific service
 */
interface IItemSvc {

    //Gets all the items from the database
    fun getAllItems(): ArrayList<ItemContent.AppItem>?

    //Create a new item
    fun create(item: ItemContent.AppItem): Boolean

    //Update an existing item
    fun update(item: ItemContent.AppItem): Boolean

    //Delete an item
    fun delete(item: ItemContent.AppItem): Boolean
}