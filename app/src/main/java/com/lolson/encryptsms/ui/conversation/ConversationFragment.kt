package com.lolson.encryptsms.ui.conversation

import android.content.Context
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lolson.encryptsms.MainSharedViewModel
import com.lolson.encryptsms.R
import com.lolson.encryptsms.data.livedata.ReceiveNewSms
import com.lolson.encryptsms.data.model.Phone
import com.lolson.encryptsms.data.model.Sms
import com.lolson.encryptsms.databinding.ActivityConversationDetailBinding
import com.lolson.encryptsms.ui.threads.ThreadFragment
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
    )
    {
        super.onCreate(savedInstanceState)

        // Fall from fullscreen
        activity?.window?.clearFlags(1024)

        if (savedInstanceState?.isEmpty == null)
        {
            arguments?.let {
                if (it.containsKey(ARG_ITEM_ID))
                {
                    val tThread = it.getSerializable(ARG_ITEM_ID) as Sms.AppSmsShort
                    l.d(
                        "CF:: ON CREATE ARGS: ${tThread.thread_id} #:#" +
                                " ${savedInstanceState?.isEmpty}")

                    // Cleanup old messages from previous thread click
                    convoSharedViewModel.cleanUpMessages()

                    // Store thread info from clicked thread
                    convoSharedViewModel.tempSms = tThread
                    convoSharedViewModel.draftSms = tThread

                    if (tThread.thread_id != -1L)
                    {
                        // Launch alert dialog for invite helper
//                    convoSharedViewModel.alertHelper(0)
                        // Get all the messages attached to the tempSms address
                        convoSharedViewModel.getAllMessages()
                    }
                }
                // This runs when the system notification is clicked
                if (it.containsKey("notify"))
                {
                    val address = it.getSerializable("notify") as String
                    l.d(
                        "CF:: ON CREATE ARGS NOTIFY: $address #:#" +
                                " ${savedInstanceState?.isEmpty}")

                    // Cleanup old messages from previous thread click
                    convoSharedViewModel.cleanUpMessages()

                    // Create a temp sms and set address to get thread_id
                    convoSharedViewModel.tempSms = Sms.AppSmsShort()
                    convoSharedViewModel.tempSms!!.address = address

                    convoSharedViewModel.findThreadId()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        l.d("CF:: ON CREATE VIEW")

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
                // TODO:: Fix to use contact name from phone contact provider
                convoSharedViewModel.setTitle(PhoneNumberUtils.formatNumber(it.address))
            }
        }

        // Shows alert box to input a number
        activity?.findViewById<Toolbar>(R.id.toolbar)?.setOnClickListener{
            convoSharedViewModel.alertHelper(2, null, null)
        }

        // Message can't be sent unless there is a valid number and some text
        binding.message.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
            {
//                TODO("Nothing to implement")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                binding.send.isClickable = count > 0 && Phone.pho().isCellPhoneNumber(
                    convoSharedViewModel.tempSms?.address)!!
            }

            override fun afterTextChanged(s: Editable?)
            {
//                TODO("Nothing to implement")
            }

        })

        // Send button with view cleanup logic
        binding.send.setOnClickListener{

            val msg = binding.message.text.toString()

            // Send the message
            convoSharedViewModel.sendSmsMessage(msg)

            // Cleanup the view
            binding.message.text.clear()
            keyBoard.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        }

        // Send button is turned off at initialization
        binding.send.isClickable = false

        // Sets the LiveData and triggers switch to return to set state in Main Activity Menu
        convoSharedViewModel.setEncryptedToggle(convoSharedViewModel.encSwitch.value!!)

        return rootView
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    )
    {
        super.onViewCreated(view, savedInstanceState)

        l.d("CF:: ON VIEW CREATE")

        // Bind layout to recycler
        recyclerView = binding.messageList

        // Observe data changes from Broadcast Receiver
        ReceiveNewSms.get().observe(viewLifecycleOwner, {
            if (it)
            {
                l.d("CF:: SMS RECEIVER OBSERVER: $it")
                convoSharedViewModel.refresh(1)
                ReceiveNewSms.set(false)
            }
        })

        // LiveData for RecyclerView
        convoSharedViewModel.messages.observe(viewLifecycleOwner, {

            // Submit recycler the changed list and position to end
            adapter.submitList(it, kotlinx.coroutines.Runnable {
                kotlin.run {
                    recyclerView.scrollToPosition(adapter.itemCount - 1)
                }
            })
        })

        setupRecyclerView(recyclerView)

        // Triggers a response when data change is invoked by long hold on text
//        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver()
//        {
//            override fun onChanged()
//            {
//                super.onChanged()
//
//                // TODO:: Works but isn't very useful; consider deleting
//                convoSharedViewModel.draftSms.run {
//                    this.sub_id?.let { recyclerView
//                        .layoutManager
//                        ?.findViewByPosition(it)
//                        ?.setBackgroundColor(
//                            Color.LTGRAY)
//                    }
//                }
//                recyclerView.scrollToPosition(adapter.itemCount - 1)
//            }
//        })
    }

    @Override
    override fun onPause() {
        super.onPause()
        // Remove listener when Fragment isn't active
        activity?.findViewById<Toolbar>(R.id.toolbar)?.setOnClickListener(null)
        // Hide keyboard if shown
        keyBoard.hideSoftInputFromWindow(view?.windowToken, 0)
        l.d("CF:: ON PAUSE")
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
        l.d("CF:: ON DESTROY VIEW")
//        convoSharedViewModel.cleanUpMessages()
    }

    @Override
    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.action_invite).isVisible = true
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
    inner class SimpleConvoRecyclerViewAdapter(
        private val parentActivity: ConversationFragment
    ) :
        ListAdapter<Sms.AppSmsShort, SimpleConvoRecyclerViewAdapter.ViewHolder>(ThreadFragment.ItemDiffCallback())
    {
        private val onLongClickListener: View.OnLongClickListener

        init
        {
            onLongClickListener = View.OnLongClickListener{v ->
                val msg = v.tag as Sms.AppSmsShort
//                val item = "Delete:: ${msg.address} msg: ${msg.id}"

                // Set draft to message to delete
                convoSharedViewModel.draftSms = msg

                // Send the delete snack bar message
                convoSharedViewModel.alertHelper(
                    1,
                    "Delete message ${msg.sub_id}?",
                "Delete")
                // Set the icon when clicked
//                setItemIcon(item)
//                this.notifyDataSetChanged()
                true
            }
        }

        /**
         * INFLATES THE CORRECT LAYOUT
         */
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder
        {
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
        )
        {
//            l.d("CF:: ON BIND VIEW HOLDER POSITION: $position")

            val msg = getItem(position)
            val c = parentActivity.context

//            holder.idView.text = PhoneNumberUtils.formatNumber(msg.address)
            holder.attachView.isVisible = false
            holder.simView.text = position.plus(1).toString()
            holder.bodyView.text = msg.body

            // First date gets full format
            if (position > 0)
            {
                val current = SimpleDateFormat(
                    "MMM/dd/yy",
                    Locale.getDefault()
                ).format(getItem(position).date)

                val previous = SimpleDateFormat(
                    "MMM/dd/yy",
                    Locale.getDefault()
                ).format(getItem(position - 1).date)

                // Print date only once per day, time only otherwise
                if (current == previous)
                {
                    holder.dateView.text = SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault()
                    ).format(msg.date)
                }
                else
                {
                    holder.dateView.text = SimpleDateFormat(
                        "MMM/dd/yy HH:mm",
                        Locale.getDefault()
                    ).format(msg.date)
                }
            }
            else
            {
                holder.dateView.text = SimpleDateFormat(
                    "MMM/dd/yy HH:mm",
                    Locale.getDefault()
                ).format(msg.date)
            }

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

            // Update when message is seen
            if (msg.read == 0)
            {
                msg.read = 1
                convoSharedViewModel.draftSms = msg
                convoSharedViewModel.updateSmsMessage()
            }

            with(holder.itemView) {
                // TODO:: This might work to highlight message, but could be a dumb idea
                // Used to show the message position
                msg.sub_id = position + 1

                tag = msg
                setOnLongClickListener(onLongClickListener)
            }
        }

        inner class ViewHolder(
            view: View
        ) : RecyclerView.ViewHolder(view)
        {
            val bodyView: TightTextView = view.findViewById(R.id.body)
            val dateView: TextView = view.findViewById(R.id.timestamp)
            val simView: TextView = view.findViewById(R.id.simIndex)
            val statusView: TextView = view.findViewById(R.id.status)
            val attachView: RecyclerView = view.findViewById(R.id.attachments)
        }

//        private fun setItemIcon(
//            icon: String
//        ): Boolean
//        {
//            Toast.makeText(parentActivity.context, icon, Toast.LENGTH_SHORT).show()
//            this.notifyDataSetChanged()
//            return true
//        }
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
