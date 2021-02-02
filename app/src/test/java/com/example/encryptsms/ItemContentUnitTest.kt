package com.example.encryptsms

import com.example.encryptsms.items.ItemContent
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner

/**
 * Unit test Item Content object for proper function
 */
@RunWith(MockitoJUnitRunner::class)
class ItemContentUnitTest {

//    private lateinit var itemPool: MutableList<ItemContent.AppItem>
//    private lateinit var firstItem: ItemContent.AppItem
//    private lateinit var secondItem: ItemContent.AppItem

    private fun log(string: String){
        println(string)
    }




        private val firstItem = ItemContent.AppItem(
            "1",
            "First Item",
            "First item details"
        )

        private val secondItem = ItemContent.AppItem(
            "2",
            "Second Item",
            "Second item details"
        )

    @Test
    fun add_item_correct(){
        val itemPool = ArrayList<ItemContent.AppItem>()

        itemPool.add(0, firstItem)
        itemPool.add(1, secondItem)

        //Prints data during tests
        log("Items size: ${itemPool.size}")
        log(" Item: ${itemPool[0].content}")

        //Test item was added to itemPool
        assertTrue("Content should be First Item",
            "First Item" == itemPool[0].content)
        assertTrue("Content should be Second Item",
            "Second Item" == itemPool[1].content)

    }

    @Test
    fun update_item(){
        //Clear data from previous tests
        val itemPool = ArrayList<ItemContent.AppItem>()

        itemPool.add(0,firstItem)


        //Prints data during tests
        log("Items size: ${itemPool.size}")

        //Test item was added to itemPool
        assertTrue("Content should be First Item",
            "First Item" == itemPool[0].content)

        itemPool.add(0,secondItem)

        assertTrue("Content should be Second Item",
            "Second Item" == itemPool[0].content)

        log("Item content at position 0 after update: " +
                "${itemPool[0].content}")

    }
    @Test
    fun delete_item(){
        //Clear data from previous tests
        val itemPool = ArrayList<ItemContent.AppItem>()


        itemPool.add(0, firstItem)
        itemPool.add(1, secondItem)

        //Prints data during tests
        log("Items size: ${itemPool.size}")
        log(" Item: ${itemPool[0].content}")

        //Test item was added to itemPool
        assertTrue("Content should be First Item",
            "First Item" == itemPool[0].content)
        assertTrue("Content should be Second Item",
            "Second Item" == itemPool[1].content)

        itemPool.removeAt(0)

        assertTrue("Content should be Second Item",
            "Second Item" == itemPool[0].content)

        log("Item content at position 0 after update: " +
                "${itemPool[0].content}")

    }
}

