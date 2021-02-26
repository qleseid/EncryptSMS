package com.example.encryptsms.database

import com.example.encryptsms.items.ItemContent

/**
 * Item service interface
 * Implement this interface with specific service
 */
interface IItemSvc {

    //Gets all the items from the database
    fun getAllItems(): ArrayList<ItemContent.AppItem>?

    //Create a new conversation
    fun create(item: ItemContent.AppItem): Boolean

    //Update an existing conversation
    fun update(item: ItemContent.AppItem): Boolean

    //Delete an conversation
    fun delete(item: ItemContent.AppItem): Boolean
}