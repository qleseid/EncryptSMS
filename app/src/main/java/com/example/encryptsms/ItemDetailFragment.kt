package com.example.encryptsms

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import com.example.encryptsms.items.ItemContent
import com.example.encryptsms.utility.LogMe

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a [ItemListActivity]
 * in two-pane mode (on tablets) or a [ItemDetailActivity]
 * on handsets.
 */
class ItemDetailFragment : Fragment() {

    /**
     * The items content this fragment is presenting.
     */
    private var item: ItemContent.AppItem? = null
    private val folderSharedViewModel: MainSharedViewModel by activityViewModels()

    //Logger
    private var l = LogMe()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                // Load the items content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                item = it.getSerializable(ARG_ITEM_ID) as ItemContent.AppItem?
                l.d("Item: ${item?.content}")
                activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title = item?.content
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.item_detail, container, false)
        activity?.findViewById<Toolbar>(R.id.detail_toolbar)?.title = item?.content


        // Show the items content as text in a TextView.
        item?.let {
            rootView.findViewById<TextView>(R.id.item_detail).text = it.details
        }

        return rootView
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"
    }
}