package com.example.encryptsms.ui.folder

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.encryptsms.ItemDetailActivity
import com.example.encryptsms.ItemDetailFragment
import com.example.encryptsms.MainSharedViewModel
import com.example.encryptsms.R
import com.example.encryptsms.R.id
import com.example.encryptsms.items.ItemContent
import com.example.encryptsms.ui.item.ItemFragment
import com.example.encryptsms.utility.LogMe
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.app_bar_main.*

class FolderFragment : Fragment() {
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SimpleItemRecyclerViewAdapter
    private val folderSharedViewModel: MainSharedViewModel by activityViewModels()

    //Logger
    private var l = LogMe()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        //Floating action button
        val fab: FloatingActionButton = activity!!.fab

        //TODO:: This button still need full functionality; add new item with details.
        fab.setOnClickListener {
//            Snackbar.make(view, "New item created", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//            folderSharedViewModel.create()
            activity!!.findNavController(R.id.nav_host_fragment).navigate(R.id.nav_item)
        }

        return inflater.inflate(R.layout.fragment_folder, container, false)
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

        recyclerView = view.findViewById<RecyclerView>(R.id.conver_list)

        setupRecyclerView(recyclerView)
        //LiveData for view
        folderSharedViewModel.items.observe(this, {
            l.d("RecycleView and LiveData observer")
            adapter.submitList(ArrayList(it))

            var total = 0.00f
            for (i in 0 until it.size){
                total += it[i].amount
            }

            activity?.findViewById<TextView>(R.id.text_total_content)?.text = total.toString()
        })

        l.d("View lifecycle Create: ${this.lifecycle.currentState}")
    }

    override fun onResume() {
        super.onResume()
        //remove bottom total
        activity?.findViewById<TextView>(R.id.text_total_content)?.visibility  = View.VISIBLE
        activity?.findViewById<TextView>(R.id.label_text_content)?.visibility = View.VISIBLE
        activity?.findViewById<FloatingActionButton>(R.id.fab)?.show()
    }

    @Override
    override fun onPause() {
        super.onPause()
        //remove bottom total
        activity?.findViewById<TextView>(R.id.text_total_content)?.visibility  = View.INVISIBLE
        activity?.findViewById<TextView>(R.id.label_text_content)?.visibility = View.INVISIBLE
        activity?.findViewById<FloatingActionButton>(R.id.fab)?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        l.d("View lifecycle Destroy: ${this.lifecycle.currentState}")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        folderSharedViewModel.change.observeForever {
            l.d("LiveData observer Forever!!!!!!!!!!!!!!!!!!!!!!!!! $it")
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        adapter = SimpleItemRecyclerViewAdapter(this, twoPane)
//        recyclerView.layoutManager = LinearLayoutManager(activity?.applicationContext, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
    }

    class SimpleItemRecyclerViewAdapter(private val parentActivity: FolderFragment,
                                        private val twoPane: Boolean) :
        ListAdapter<ItemContent.AppItem, SimpleItemRecyclerViewAdapter.ViewHolder>(ItemDiffCallback()) {

        private val onClickListener: View.OnClickListener
        private var selected = -1

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as ItemContent.AppItem

                //This is what sets the selected var to change background
                if (selected != -1){notifyItemChanged(selected)}
                selected = item.id.toInt().minus(1)
                notifyItemChanged(selected)
                
                if (twoPane) {
                    val fragment = ItemFragment().apply {
                        arguments = Bundle().apply {
                            putSerializable(ItemFragment.ARG_ITEM_ID, item)
                        }
                    }
                    parentActivity
                        .fragmentManager
                        ?.beginTransaction()
                        ?.replace(id.item_detail_container, fragment)
                        ?.commit()
                } else {
                    val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
                        putExtra(ItemDetailFragment.ARG_ITEM_ID, item)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_content, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)
            holder.idView.text = position.plus(1).toString()
            holder.contentView.text = item.content
            holder.amView.text = item.amount.toString()

            //Set the icons for user selection. Names come from assets folder and actual drawables are in res/drawable
            //It was the only solution I could find at the moment
            val res = parentActivity.resources
            holder.icView.setImageDrawable(
                ResourcesCompat.getDrawable(
                res,
                res.getIdentifier(item.icon,"drawable", parentActivity.activity?.packageName),
                null))

            //Sets the background color if selected
            if (position == selected){
                holder.itemView.setBackgroundColor(Color.LTGRAY)
            } else {
                holder.itemView.setBackgroundColor(Color.WHITE)
            }

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.findViewById(id.id_text)
            val contentView: TextView = view.findViewById(id.content)
            val amView: TextView = view.findViewById(id.item_amount)
            val icView: ImageView = view.findViewById(id.folder_list_icon)
        }
    }

    //Class for ListAdapter to use with RecyclerView
    class ItemDiffCallback: DiffUtil.ItemCallback<ItemContent.AppItem>(){
        override fun areItemsTheSame(
            oldItem: ItemContent.AppItem,
            newItem: ItemContent.AppItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ItemContent.AppItem,
            newItem: ItemContent.AppItem
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }
}
