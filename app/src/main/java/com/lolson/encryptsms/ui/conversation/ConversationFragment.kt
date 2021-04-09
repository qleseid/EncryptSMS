package com.lolson.encryptsms.ui.conversation

import android.content.Context
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lolson.encryptsms.MainActivity
import com.lolson.encryptsms.MainSharedViewModel
import com.lolson.encryptsms.R
import com.lolson.encryptsms.data.livedata.ReceiveNewSms
import com.lolson.encryptsms.data.model.Phone
import com.lolson.encryptsms.data.model.Sms
import com.lolson.encryptsms.databinding.ActivityConversationDetailBinding
import com.lolson.encryptsms.utility.LogMe
import com.lolson.encryptsms.utility.widget.TightTextView
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class ConversationFragment : Fragment() {
    /**
     * The conversation of a new message or selected conversation.
     */

    // Binding view
    private var _binding: ActivityConversationDetailBinding? = null
    private val binding get() = _binding!!

    // Keyboard management fun
    private lateinit var keyBoard: InputMethodManager

    // For data and recycler views
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SimpleConvoRecyclerViewAdapter
    private val convoSharedViewModel: MainSharedViewModel by activityViewModels()
    //Logger
    private var l = LogMe()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                val tThread = it.getSerializable(ARG_ITEM_ID) as Sms.AppSmsShort

                // Store thread info from clicked thread
                convoSharedViewModel.tempSms = tThread
                convoSharedViewModel.draftSms = tThread

                // Get all the messages attached to the tempSms address
                convoSharedViewModel.getAllMessages()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Bindings
        _binding = ActivityConversationDetailBinding.inflate(inflater, container, false)
        val rootView = binding.root

        // Instantiate keyboard
        keyBoard = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        //Allow fragment to hide toolbar items
        setHasOptionsMenu(true)

        // Hide the Floating Button in this Fragment
        activity?.findViewById<FloatingActionButton>(R.id.fab)?.hide()

        // Show the address content as the title text in a TextView.
        convoSharedViewModel.tempSms.let {
            if (it != null)
            {
                convoSharedViewModel.setTitle(PhoneNumberUtils.formatNumber(it.address))
            }
        }

        activity?.findViewById<Toolbar>(R.id.toolbar)?.setOnClickListener{
            (activity as MainActivity).showAlertWithTextInput()
        }

        binding.message.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            {
//                TODO("Nothing to implement")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                binding.send.isClickable = count > 0 && Phone.pho().isCellPhoneNumber(
                    convoSharedViewModel.tempSms?.address)!!

                // Check if contact has a encryption key by thread_id
                convoSharedViewModel.tempSms?.thread_id?.let{

                    convoSharedViewModel.checkForEncryptionKey(it)}.let{
                    if (it != null)
                    {
                        convoSharedViewModel.setEncryptedToggle(it)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?)
            {
//                TODO("Nothing to implement")
            }

        })

        binding.send.setOnClickListener{

            val msg = binding.message.text.toString()

            // Send the message
            convoSharedViewModel.sendSmsMessage(msg)

            // Cleanup the view
            binding.message.text.clear()
            keyBoard.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        }

        binding.send.isClickable = false

        // Sets the LiveData and triggers switch to return to set state in Main Activity Menu
        convoSharedViewModel.setEncryptedToggle(convoSharedViewModel.encSwitch.value!!)

        return rootView
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Bind layout to recycler
        recyclerView = binding.messageList

        // Observe data changes from Broadcast Receiver
        ReceiveNewSms.get().observe(viewLifecycleOwner, {
            if (it)
            {
                l.d("Conversation SMS RECEIVER OBSERVER: $it")
                convoSharedViewModel.refresh(1)
                convoSharedViewModel.refresh(0)
                ReceiveNewSms.set(false)
            }
        })

        //LiveData for RecyclerView
        convoSharedViewModel.messages.observe(viewLifecycleOwner, {

            l.d("CONVERSATION MESSAGE LIVE DATA")
            // Submit recycler the changed list keys
            adapter.submitList(it, kotlinx.coroutines.Runnable {
                kotlin.run {
                    recyclerView.scrollToPosition(adapter.itemCount - 1)
                }
            })
        })

        setupRecyclerView(recyclerView)

        // Triggers a response when data change is invoked
        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver()
        {
            override fun onChanged()
            {
                super.onChanged()
                recyclerView.scrollToPosition(adapter.itemCount - 1)
            }
        })
    }

    @Override
    override fun onPause() {
        super.onPause()
        // Remove listener when Fragment isn't active
        activity?.findViewById<Toolbar>(R.id.toolbar)?.setOnClickListener(null)
        // Hide keyboard if shown
        keyBoard.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }

    @Override
    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {
        super.onCreateOptionsMenu(menu, inflater)
        l.d("Conversation FRAGMENT: Option menu")
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView
    ) {
        adapter = SimpleConvoRecyclerViewAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    /**
     * Recycler view for the Conversation
     */
    class SimpleConvoRecyclerViewAdapter(
        private val parentActivity: ConversationFragment
    ) :
        ListAdapter<Sms.AppSmsShort, SimpleConvoRecyclerViewAdapter.ViewHolder>(ItemDiffCallback())
    {
        private val onLongClickListener: View.OnLongClickListener

        init {
            Log.d("ConvoRecycle init:","*****************************************")

            onLongClickListener = View.OnLongClickListener {v ->
                val item = "Delete:: ${v.tag as String}"

                //Set the icon when clicked
                setItemIcon(item)
            }
        }

        /**
         * INFLATES THE CORRECT LAYOUT
         */
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {

            // This is set to out initially
            var vh = ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.message_list_item_out, parent, false))

            // Changes if it's coming in
            when (viewType){
                1 -> vh = ViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.message_list_item_in, parent, false))
            }
            return vh
        }

        /**
         * THIS RETURNS THE MESSAGE TYPE
         * 1 = received message
         * 2 = sent message
         */
        override fun getItemViewType(
            position: Int
        ): Int
        {
            val msg = getItem(position) ?: return -1
            return msg.type ?: -1
        }

        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int
        ) {

            val msg = getItem(position)
            val c = parentActivity.context

//            holder.idView.text = PhoneNumberUtils.formatNumber(msg.address)
            holder.attachView.isVisible = false
            holder.simView.text = position.toString()
            holder.bodyView.text = msg.body
            holder.dateView.text = SimpleDateFormat("MMM/dd/yy HH:mm", Locale.getDefault()).format(msg.date)

            // Sets the message status
            when(msg.status){
                -1 -> {
                    holder.statusView.text = c?.getString(R.string.no_status)
                }
                0 -> {
                    holder.statusView.text = c?.getString(R.string.status_sent)
                }
                32 -> {
                    holder.statusView.text = c?.getString(R.string.status_pending)
                }
                64 -> {
                    holder.statusView.text = c?.getString(R.string.status_failed)
                }
            }

            with(holder.itemView) {
                tag = msg.address
                setOnLongClickListener(onLongClickListener)
            }
        }

        class ViewHolder(
            view: View
        ) : RecyclerView.ViewHolder(view)
        {
            val bodyView: TightTextView = view.findViewById(R.id.body)
            val dateView: TextView = view.findViewById(R.id.timestamp)
            val simView: TextView = view.findViewById(R.id.simIndex)
            val statusView: TextView = view.findViewById(R.id.status)
            val attachView: RecyclerView = view.findViewById(R.id.attachments)
        }

        private fun setItemIcon(
            icon: String
        ): Boolean
        {
            Toast.makeText(parentActivity.context, icon, Toast.LENGTH_SHORT).show()
            this.notifyDataSetChanged()
            return true
        }
    }

    // Class for ListAdapter to use with RecyclerView
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

    companion object {
        /**
         * The fragment argument representing the conversation ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"
    }
}
