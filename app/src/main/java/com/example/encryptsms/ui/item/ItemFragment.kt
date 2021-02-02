package com.example.encryptsms.ui.item

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.encryptsms.*
import com.example.encryptsms.items.ItemContent
import com.example.encryptsms.utility.LogMe
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.app_bar_main.*

class ItemFragment : Fragment() {
    /**
     * The items content this fragment is presenting.
     */
    private var twoPane: Boolean = false
    //Get drawables from resource
    private var _icon = ""
    private lateinit var asset: MutableList<String>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SimpleIconRecyclerViewAdapter
    private val folderSharedViewModel: MainSharedViewModel by activityViewModels()
    //Logger
    private var l = LogMe()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.findViewById<Toolbar>(R.id.toolbar)?.title = ("New Item")

        //Instantiate
//        item = ItemContent.AppItem("-1","","","",0.0f)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                // Load the items content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                var tItem = it.getSerializable(ARG_ITEM_ID) as ItemContent.AppItem
                folderSharedViewModel.temp_item.icon = tItem.icon
                folderSharedViewModel.temp_item.details = tItem.details
                folderSharedViewModel.temp_item.content = tItem.content
                folderSharedViewModel.temp_item.id = tItem.id
                folderSharedViewModel.temp_item.amount = tItem.amount
                l.d("Item Frag: ${folderSharedViewModel.temp_item.content}")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (view.findViewById<FrameLayout>(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

        asset = activity?.assets?.list("icons")?.toMutableList()!!

        recyclerView = view.findViewById(R.id.icon_list)
        setupRecyclerView(recyclerView)
        adapter.submitList(asset)

        l.d("View lifecycle Create: ${folderSharedViewModel.temp_item.hashCode()}")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_item, container, false)

        //Instantiate
//        item = ItemContent.AppItem("-1","","","",0.0f)

        //Allow fragment to hide toolbar items
        setHasOptionsMenu(true)


        //Floating action button
        val fab: FloatingActionButton = activity!!.fab

        //TODO:: This button still need full functionality; add new item with details.
        fab.setOnClickListener {

            //Button function
            actionButton()
        }

        if(folderSharedViewModel.temp_item.id != "-1") {
            val res = resources
            // Show the items content as text in a TextView.
            folderSharedViewModel.temp_item.let {
                rootView.findViewById<EditText>(R.id.editTextTextPersonName).setText(it.content)
                rootView.findViewById<EditText>(R.id.editTextNumberDecimal)
                    .setText(it.amount.toString())
                rootView.findViewById<EditText>(R.id.item_detail_multitext).setText(it.details)
            }
        }
        l.d("ITEM FRAGMENT ${folderSharedViewModel.temp_item.hashCode()}")
        return rootView
    }

    @Override
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        l.d("ITEM FRAGMENT: Option menu")
        activity?.findViewById<FloatingActionButton>(R.id.fab)?.show()
        menu.findItem(R.id.action_home).isVisible = false
        menu.findItem(R.id.action_about).isVisible = false
        menu.findItem(R.id.action_no_vis).isVisible = false
    }

    //Floating action button method
    private fun actionButton(){

        // Collect data from fields
        folderSharedViewModel.temp_item.content = activity?.findViewById<EditText>(R.id.editTextTextPersonName)?.text.toString()
        folderSharedViewModel.temp_item.amount = activity?.findViewById<EditText>(R.id.editTextNumberDecimal)?.text.toString().toFloat()
        folderSharedViewModel.temp_item.details = activity?.findViewById<EditText>(R.id.item_detail_multitext)?.text.toString()
//        folderSharedViewModel.temp_item.icon

        l.d("FAB: ${folderSharedViewModel.temp_item.content} ${folderSharedViewModel.temp_item.amount} ${folderSharedViewModel.temp_item.details} ${folderSharedViewModel.temp_item.icon} ${folderSharedViewModel.temp_item.hashCode()}")

        //New item has -1 id, update item has different id than -1
        if(folderSharedViewModel.temp_item.id == "-1") {
            if(folderSharedViewModel.temp_item.content != "" && folderSharedViewModel.temp_item.icon != "") {
                folderSharedViewModel.create()
                folderSharedViewModel.clearTemp()
                activity!!.findNavController(R.id.nav_host_fragment).navigate(R.id.nav_folder)
            }else{
                Toast.makeText(activity?.applicationContext, "Enter name and select icon first", Toast.LENGTH_LONG).show()
            }
        }else{
            folderSharedViewModel.update()
            folderSharedViewModel.clearTemp()
            activity!!.findNavController(R.id.nav_host_fragment).navigate(R.id.nav_folder)
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        adapter = SimpleIconRecyclerViewAdapter(this, folderSharedViewModel.temp_item)
        recyclerView.layoutManager = LinearLayoutManager(activity?.applicationContext, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
    }

    /**
     * Recycler view for the clickable icons
     */
    class SimpleIconRecyclerViewAdapter(
        private val parentActivity: ItemFragment,
        private var selItem: ItemContent.AppItem) :
        ListAdapter<String, SimpleIconRecyclerViewAdapter.ViewHolder>(ItemDiffCallback()) {
        private val onClickListener: View.OnClickListener

        private val folderSharedViewModel: MainSharedViewModel by parentActivity.activityViewModels()
        private var selected = selItem.id.toInt()
        init {
            Log.d("ItemRecycle init:", "Item ${selItem.id} ")

            onClickListener = View.OnClickListener { v ->
                val item = v.tag as String

                //Set the icon when clicked
                setItemIcon(item)

                Log.d("ItemRecycle:", "Item clicked: $selected ${v.id} ")

                //This is what sets the selected var to change background
                v.setBackgroundColor(Color.LTGRAY)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_view_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            var bitem = getItem(position)

            bitem = bitem.removeSuffix(".xml",)
            val temp = parentActivity.activity?.resources!!.getIdentifier(bitem, "drawable", parentActivity.activity?.packageName)
            Log.d("ItemRecycle:", "Item data: $bitem $temp ${selItem.icon} ")

            //Set the icons for user selection. Names come from assets folder and actual drawables are in res/drawable
            //It was the only solution I could find at the moment
            val res = parentActivity.resources
            holder.imgView.setImageDrawable(ResourcesCompat.getDrawable(
                res,
                res.getIdentifier(bitem,"drawable", parentActivity.activity?.packageName),
                null))

            //Sets the background color if selected
            if (bitem == selItem.icon){
                holder.itemView.setBackgroundColor(Color.LTGRAY)
            } else {
                holder.itemView.setBackgroundColor(Color.WHITE)
            }

            with(holder.itemView) {
                tag = bitem
                setOnClickListener(onClickListener)
            }
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imgView: ImageView = view.findViewById(R.id.imageView1)
        }

        private fun setItemIcon(icon: String){
            var _icon = icon
            folderSharedViewModel.temp_item.icon = _icon
            Log.d("SET ICON RECYCLER","Set Icon Item Fragment: $_icon ${folderSharedViewModel.temp_item.hashCode()}")
        }

    }

    //Class for ListAdapter to use with RecyclerView
    class ItemDiffCallback: DiffUtil.ItemCallback<String>(){
        override fun areItemsTheSame(
            oldItem: String,
            newItem: String
        ): Boolean {
            return oldItem == newItem
        }
        override fun areContentsTheSame(
            oldItem: String,
            newItem: String
        ): Boolean {
            return oldItem == newItem
        }

    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"
    }
}
