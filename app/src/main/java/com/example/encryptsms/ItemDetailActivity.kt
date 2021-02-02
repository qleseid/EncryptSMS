package com.example.encryptsms

import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import com.example.encryptsms.items.ItemContent

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [ItemListActivity].
 */
class ItemDetailActivity : AppCompatActivity() {

    //Shared view model for main activity fragments: Used to delete item
    private val folderSharedViewModel: MainSharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)
        setSupportActionBar(findViewById(R.id.detail_toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Item deleted", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            folderSharedViewModel.delete(intent.getSerializableExtra(ItemDetailFragment.ARG_ITEM_ID) as ItemContent.AppItem)
        }

        // Show the Up button in the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            val fragment = ItemDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ItemDetailFragment.ARG_ITEM_ID,
                        intent.getSerializableExtra(ItemDetailFragment.ARG_ITEM_ID))
                }
            }

            supportFragmentManager.beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.d("Item Activity", "Trying to pop the stack with up button")
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.d("Item Activity","Override for on back press")
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown.
                android.R.id.home -> {
                    Toast.makeText(applicationContext, "Item Detail Up Clicked", Toast.LENGTH_SHORT).show()
                    onNavigateUp()
//                    onBackPressed()
//                    val intent = Intent(this, MainActivity::class.java).apply {
//                        putExtra("param1", "item_detail_activity")
//                    }
//                    startActivity(intent)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
}