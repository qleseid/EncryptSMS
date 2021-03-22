package com.example.encryptsms.ui.threads

import android.graphics.Color
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.encryptsms.MainSharedViewModel
import com.example.encryptsms.R
import com.example.encryptsms.R.id
import com.example.encryptsms.data.livedata.ReceiveNewSms
import com.example.encryptsms.data.model.Sms
import com.example.encryptsms.databinding.FragmentThreadsBinding
import com.example.encryptsms.ui.conversation.ConversationFragment
import com.example.encryptsms.utility.LogMe
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class ThreadFragment : Fragment() {

    // Binding view
    private var _binding: FragmentThreadsBinding? = null
    private val binding get() = _binding!!

    // For data and recycler views
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SimpleItemRecyclerViewAdapter
    private val threadsSharedViewModel: MainSharedViewModel by activityViewModels()

    //Logger
    private var l = LogMe()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        // Bindings
        _binding = FragmentThreadsBinding.inflate(inflater, container, false)
        val rootView = binding.root

        // Debug
        l.d("Threads: On Create View")


        //Floating action button
        val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)

        fab?.setOnClickListener {

            // Nav to Conversation Fragment and adds in a blank SMS as arg
            val bundle = Bundle().apply {
                putSerializable(ConversationFragment.ARG_ITEM_ID, Sms.AppSmsShort())
            }
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id
                .nav_conversations, bundle)
        }
        return rootView
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.threadList.converList

        setupRecyclerView(recyclerView)

        // Observe data changes from Broadcast Receiver
        ReceiveNewSms.get().observe(viewLifecycleOwner, {

            l.d("Threads SMS OBSERVER: $it")
            if (it)
            {
                threadsSharedViewModel.refresh(0)
                ReceiveNewSms.set(false)
            }
        })

        //LiveData for RecyclerView
        threadsSharedViewModel.threads.observe(viewLifecycleOwner, {

            // Submit recycler the changed list keys
            // The runnable ensures the list is done so positioning works correct
            adapter.submitList(it, kotlinx.coroutines.Runnable {
                kotlin.run {
                    recyclerView.scrollToPosition(0)
                }
            })
        })
    }

    override fun onDestroyView()
    {
        super.onDestroyView()

        // Release bindings to prevent data leaks
        _binding = null
    }

    override fun onResume()
    {
        super.onResume()

        // FAB is removed in other fragments, this brings it back
        activity?.findViewById<FloatingActionButton>(R.id.fab)?.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        threadsSharedViewModel.encSwitch.observeForever {
            l.d("Threads: LiveData observer Forever!!!!!!!!!!!!!!!!!!!!!!!!! $it")
        }
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView
    ) {
        adapter = SimpleItemRecyclerViewAdapter(this)
        recyclerView.adapter = adapter
    }

    class SimpleItemRecyclerViewAdapter(
        private val parentActivity: ThreadFragment
    ) :
        ListAdapter<Sms.AppSmsShort, SimpleItemRecyclerViewAdapter.ViewHolder>(ItemDiffCallback())
    {

        private val onClickListener: View.OnClickListener
        private var selected = -1

        init
        {
            onClickListener = View.OnClickListener { v ->
                val msg = v.tag as Sms.AppSmsShort

                // Nav to Conversation Fragment and adds in the selected thread as arg
                val bundle = Bundle().apply {
                        putSerializable(ConversationFragment.ARG_ITEM_ID, msg)
                }
                parentActivity.findNavController().navigate(id.nav_conversations, bundle)
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder
        {
            return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.conversation_list_item, parent, false))
        }

        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int)
        {
            val msg = getItem(position)
            holder.idView.text = PhoneNumberUtils.formatNumber(msg.address)
            holder.snipView.text = msg.body
            holder.dateView.text = SimpleDateFormat("EEE MMM/dd/yy", Locale.getDefault()).format(msg.date)

            //Set the icons for user selection. Names come from assets folder and actual
            // drawables are in res/drawable
            //It was the only solution I could find at the moment
            val res = parentActivity.resources
            holder.icView.setImageDrawable(
                ResourcesCompat.getDrawable(
                res,
                res.getIdentifier("ic_launcher_round","mipmap", parentActivity.activity?.packageName),
                null))

            //Sets the background color if selected
            if (position == selected)
            {
                holder.itemView.setBackgroundColor(Color.LTGRAY)
            }
            else
            {
                holder.itemView.setBackgroundColor(Color.WHITE)
            }

            // Set the read/unread icon of the message
            if (msg.read == 0)
            {
                holder.readView.visibility = View.VISIBLE
            }
            else
            {
                holder.readView.visibility = View.GONE
            }

            with(holder.itemView)
            {
                tag = msg
                setOnClickListener(onClickListener)
            }
        }

        class ViewHolder(
            view: View
        ) : RecyclerView.ViewHolder(view)
        {
            val idView: TextView = view.findViewById(id.title)
            val snipView: TextView = view.findViewById(id.snippet)
            val dateView: TextView = view.findViewById(id.date)
            val icView: ImageView = view.findViewById(id.conver_list_icon)
            val readView: ImageView = view.findViewById(id.unread)
        }
    }

    //Class for ListAdapter to use with RecyclerView
    class ItemDiffCallback: DiffUtil.ItemCallback<Sms.AppSmsShort>(){
        override fun areItemsTheSame(
            oldItem: Sms.AppSmsShort,
            newItem: Sms.AppSmsShort
        ): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(
            oldItem: Sms.AppSmsShort,
            newItem: Sms.AppSmsShort
        ): Boolean {
            return oldItem == newItem
        }
    }
}
