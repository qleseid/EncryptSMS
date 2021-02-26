package com.example.encryptsms.database

import android.content.Context
import com.example.encryptsms.items.ItemContent
import com.example.encryptsms.utility.LogMe
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Service to store conversation data to a local file
 */
class ItemSvcImpl(_context: Context): IItemSvc {

    //Filename
    private val filename: String = "Items.sio"
    private var file: File
    private var context: Context = _context

    //Logger
    private var l = LogMe()

    init {
        file = File(_context.filesDir, filename)

        l.d("In Item Service Implement Constructor")
    }

    //Gets all the items from the database
    override fun getAllItems(): ArrayList<ItemContent.AppItem>? {

        //Create Mutable list to populate with read
        var tempList = readFile()

        // Add some sample items.
        if(tempList?.size == 0) {
            for (i in 0..10) {
                tempList.add(i, createAppItem(i))
            }

            //Create an conversation to throw away during list creation
            val ti = tempList[0]
            ti.id = "delete"
            tempList = addItem(ti, tempList)
        }

        l.d("GET ALL ITEMS:: size is: ${tempList?.size}")
        return tempList
    }

    //Create a new conversation
    override fun create(item: ItemContent.AppItem): Boolean {

        //Return boolean if operation was successful
        var br = false

        //read in list from file to add to
        var tempList = readFile()

        if (tempList != null) {
            //Add the conversation and return the list
            tempList = addItem(item, tempList)
        } else{
            l.e("ITEMSVC::CREATE failed to add conversation")
        }


        if (tempList != null) {
            //write list to file
            br = writeFile(tempList)
        } else{
            l.e("ITEMSVC::CREATE failed to write items")
        }
        return br
    }

    //Update an existing conversation
    override fun update(item: ItemContent.AppItem): Boolean {

        //Return boolean if operation was successful
        var br = false

        //read in list from file to update
        val tempList = readFile()

        if (tempList != null) {
            //update the conversation at it's position
            tempList[(item.id.toInt() - 1)] = item
        } else{
            l.e("ITEMSVC::UPDATE failed to update conversation")
        }
        if (tempList != null) {
            //write list to file
            br = writeFile(tempList)
        } else{
            l.e("ITEMSVC::UPDATE failed to write items")
        }
        return br
    }

    //Delete an conversation
    override fun delete(item: ItemContent.AppItem): Boolean {
        //Return boolean if operation was successful
        var br = false

        //read in list from file to delete conversation
        var tempList = readFile()

        if (tempList != null) {
            //Delete the conversation and return the list
            tempList.removeAt(item.id.toInt())

            //Change ID to delete so add conversation knows to skip it
            item.id = "delete"

            //Add Items back to list to restructure ID
            tempList = addItem(item, tempList)
        } else{
            l.e("ITEMSVC::DELETE failed to delete conversation")
        }


        if (tempList != null) {
            //write list to file
            br = writeFile(tempList)
        } else{
            l.e("ITEMSVC::DELETE failed to write items")
        }
        return br
    }

    //Local file read method
    private fun readFile(): ArrayList<ItemContent.AppItem>? {
        l.d("Item Service:: ReadFile")

        var tempList: ArrayList<ItemContent.AppItem>? = null
        var ois: ObjectInputStream? = null
        val tag = "Item Service RD"

        //Make sure a file exists to avoid read errors
        file.createNewFile()

        try {
            ois = ObjectInputStream(context.openFileInput(filename))
            tempList = ois.readObject() as ArrayList<ItemContent.AppItem>?
        } catch (e: Exception){
            e.message?.let { l.e( it) }
        } finally {
            if (ois != null){
                try {
                    ois.close()
                } catch (e: java.lang.Exception){
                    l.w( e.toString())
                }
            }
        }
        if (tempList != null) {
            l.d( "$tag: At read file return temp size: ${tempList.size}")
        }
        return tempList
    }

    //Local file write method
    private fun writeFile(_items: ArrayList<ItemContent.AppItem>): Boolean {
        val oos: ObjectOutputStream
        val tag = "Item Service WR"
        var boolResult = false

        try {
            oos = ObjectOutputStream(context.openFileOutput(filename, Context.MODE_PRIVATE))
            oos.writeObject(_items)
            boolResult = true
            oos.flush()
            oos.close()
        } catch (e: Exception) {
            l.e(e.toString())
        }
        l.d( "$tag: File was wrote to at $filename Boolean: $boolResult")
        return boolResult
    }

    //Adds an conversation to the ArrayList
    private fun addItem(item: ItemContent.AppItem,
                        list: ArrayList<ItemContent.AppItem>):
            ArrayList<ItemContent.AppItem>{

        //So the id doesn't start at zero
        val tempList: ArrayList<ItemContent.AppItem> = ArrayList()

        //Recreate the list to fix any ID issues
        for(i in 0 until list.size){
            l.d("FOR LOOP i: $i")

            //Correct id if needed
            var tempItem = ItemContent.AppItem(
                i.toString(),
                "Item ${(i + 1)}",
                list[i].details,
                list[i].icon,
                list[i].amount
            )

            tempList.add(i, tempItem)
        }

        if (item.id != "delete") {
            val ts = tempList.size
            val temp = ts + 1

            //Add conversation to end
            item.id = temp.toString()
            item.content = "Item $temp"
            tempList.add(ts, item)
        }
        l.d( "Item array size ${item.id} ${tempList.size}")

        return tempList
    }


    //Creates a sample conversation if needed
    private fun createAppItem(position: Int): ItemContent.AppItem {
        return ItemContent.AppItem(
            position.toString(),
            "Item $position",
            makeDetails(position),
            "@drawable/handyman_black_24dp",
            12.57f)
    }

    //Creates the sample details if needed; repeats string for conversation position #
    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0 until position) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }
}