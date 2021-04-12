@file:Suppress("unused", "unused")

package com.lolson.encryptsms.database

import android.content.Context
import com.lolson.encryptsms.contents.KeyContent
import com.lolson.encryptsms.utility.LogMe
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.security.KeyPairGenerator

/**
 * Service to store key data to a local file
 */
class ContactKeySvcImpl(_context: Context): IContactKeySvc
{
    //Filename
    private val filename: String = "keys.sio"
    private var file: File
    private var context: Context = _context

    //Logger
    private var l = LogMe()

    init
    {
        file = File(_context.filesDir, filename)

        l.d("In Contact Key Service Implement Constructor")
    }

    /**
     * GET ALL KEYS
     */
    override fun getAllKeys(): ArrayList<KeyContent.AppKey>?
    {
        //Create Mutable list to populate with read
        var tempList = readFile()

        // Add some sample keys.
        if(tempList?.size == 0)
        {
            for (i in 0..10)
            {
                tempList.add(i, createAppKey(i))
            }

            //Create an conversation to throw away during list creation
            val ti = tempList[0]
            ti.id = "delete"
            tempList = addItem(ti, tempList)
        }

        l.d("GET ALL KEYS:: size is: ${tempList?.size}")
        return tempList
    }

    /**
     * STORE NEWLY CREATED KEY
     */
    override fun create(
        key: KeyContent.AppKey
    ): Long
    {

        //Return boolean if operation was successful
        var br = -1L

        //read in list from file to add to
        var tempList = readFile()

        if (tempList != null)
        {
            //Add the key and return the list
            tempList = addItem(key, tempList)
        }
        else
        {
            l.e("KEYSVC::CREATE failed to add key")
        }


        if (tempList != null)
        {
            //write list to file
            if(writeFile(tempList))
            {
                br = (tempList.size - 1).toLong()
            }
        }
        else
        {
            l.e("KEYSVC::CREATE failed to write keys")
        }
        return br
    }

    /**
     * UPDATE KEY
     */
    override fun update(
        key: KeyContent.AppKey
    ): Boolean
    {

        //Return boolean if operation was successful
        var br = false

        //read in list from file to update
        val tempList = readFile()

        if (tempList != null)
        {
            //update the key at it's position
            tempList[(key.id.toInt() - 1)] = key
        }
        else
        {
            l.e("KEYSVC::UPDATE failed to update key")
        }
        if (tempList != null)
        {
            //write list to file
            br = writeFile(tempList)
        }
        else
        {
            l.e("KEYSVC::UPDATE failed to write keys")
        }
        return br
    }

    /**
     * DELETE KEY
     */
    override fun delete(
        key: KeyContent.AppKey
    ): Boolean
    {
        //Return boolean if operation was successful
        var br = false

        //read in list from file to delete key
        var tempList = readFile()

        if (tempList != null)
        {
            //Delete the key and return the list
            tempList.removeAt(key.id.toInt())

            //Change ID to delete so add key knows to skip it
            key.id = "delete"

            //Add Items back to list to restructure ID
            tempList = addItem(key, tempList)
        }
        else
        {
            l.e("KEYSVC::DELETE failed to delete key")
        }


        if (tempList != null)
        {
            //write list to file
            br = writeFile(tempList)
        }
        else
        {
            l.e("KEYSVC::DELETE failed to write keys")
        }
        return br
    }

    /**
     * Local file read method
     */
    @Suppress("UNCHECKED_CAST")
    private fun readFile(): ArrayList<KeyContent.AppKey>?
    {
        l.d("Key Service:: ReadFile")

        var tempList: ArrayList<KeyContent.AppKey>? = null
        var ois: ObjectInputStream? = null
        val tag = "Key Service RD"

        //Make sure a file exists to avoid read errors
        file.createNewFile()

        try
        {
            ois = ObjectInputStream(context.openFileInput(filename))
            tempList = ois.readObject() as ArrayList<KeyContent.AppKey>?
        }
        catch (e: Exception)
        {
            e.message?.let { l.e( it) }
        }
        finally
        {
            if (ois != null)
            {
                try
                {
                    ois.close()
                }
                catch (e: java.lang.Exception)
                {
                    l.w( e.toString())
                }
            }
        }
        if (tempList != null)
        {
            l.d( "$tag: At read file return temp size: ${tempList.size}")
        }
        return tempList
    }

    /**
     * Local file write method
     */
    private fun writeFile(
        _keys: ArrayList<KeyContent.AppKey>
    ): Boolean
    {
        val oos: ObjectOutputStream
        val tag = "Key Service WR"
        var boolResult = false

        try
        {
            oos = ObjectOutputStream(context.openFileOutput(filename, Context.MODE_PRIVATE))
            oos.writeObject(_keys)
            boolResult = true
            oos.flush()
            oos.close()
        }
        catch (e: Exception)
        {
            l.e(e.toString())
        }
        l.d( "$tag: File was wrote to at $filename Boolean: $boolResult")
        return boolResult
    }

    /**
     * Adds an conversation to the ArrayList
     */
    private fun addItem(
        key: KeyContent.AppKey,
        list: ArrayList<KeyContent.AppKey>
    ):ArrayList<KeyContent.AppKey>
    {
        //So the id doesn't start at zero
        val tempList: ArrayList<KeyContent.AppKey> = ArrayList()

        //Recreate the list to fix any ID issues
        for(i in 0 until list.size)
        {
            l.d("FOR LOOP i: $i")

            //Correct id if needed
            val tempItem = KeyContent.AppKey(
                i.toString(),
                list[i].sent,
                list[i].thread_id,
                list[i].publicKey
            )

            tempList.add(i, tempItem)
        }

        if (key.id != "delete")
        {
            val ts = tempList.size
            val temp = ts + 1

            //Add conversation to end
            key.id = temp.toString()
            tempList.add(ts, key)
        }
        l.d( "Key array size ${key.id} ${tempList.size}")

        return tempList
    }

    /**
     * Creates a sample key data if needed
     */
    private fun createAppKey(position: Int): KeyContent.AppKey
    {
        val keyGen = KeyPairGenerator.getInstance("DiffieHellman")
        val keyPair = keyGen.genKeyPair()
        return KeyContent.AppKey(
            position.toString(),
            false,
            position.toString(),
            keyPair.public)
    }
}