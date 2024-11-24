package za.co.varsitycollege.st10215473.community.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10215473.community.R
import za.co.varsitycollege.st10215473.community.data.Message
import za.co.varsitycollege.st10215473.community.databinding.DeleteLayoutBinding
import za.co.varsitycollege.st10215473.community.databinding.ReceiveMsgBinding
import za.co.varsitycollege.st10215473.community.databinding.SendMsgBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessagesAdapter(var context: Context, messages:ArrayList<Message>?, senderRoom:String, receiverRoom: String):RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    lateinit var messages: ArrayList<Message>
    val ITEM_SENT = 1
    val ITEM_RECEIVED = 2
    val senderRoom:String
    var receiverRoom:String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT){
            val view = LayoutInflater.from(context).inflate(R.layout.send_msg, parent, false)
            SentMsgHolder(view)
        }
        else{
            val view = LayoutInflater.from(context).inflate(R.layout.receive_msg, parent, false)
            ReceiveMsgHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val messages = messages[position]
        return if (FirebaseAuth.getInstance().uid == messages.senderId){
            ITEM_SENT
        }
        else{
            ITEM_RECEIVED
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val currentUserId = FirebaseAuth.getInstance().uid
        val formattedTimestamp = formatTimestamp(message.timeStamp)

        if(holder.javaClass == SentMsgHolder::class.java){
            val viewHolder = holder as SentMsgHolder

            if (message.message.equals("photo")) {
                Glide.with(holder.itemView.context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(viewHolder.binding.image)
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
            }

            viewHolder.binding.message.text = message.message
            viewHolder.binding.timestamp.text = formattedTimestamp

            // Long click listener for sent messages
            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)

                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()

                // Show "Delete for Everyone" only if the message belongs to the current user
                if (message.senderId == currentUserId) {
                    binding.everyone.visibility = View.VISIBLE
                    binding.everyone.setOnClickListener {
                        deleteMessageForEveryone(message)
                        dialog.dismiss()
                    }
                } else {
                    binding.everyone.visibility = View.GONE
                }

                binding.delete.setOnClickListener {
                    deleteMessageForSender(message)
                    dialog.dismiss()
                }

                binding.cancel.setOnClickListener { dialog.dismiss() }

                dialog.show()
                false
            }
        } else {
            val viewHolder = holder as ReceiveMsgHolder

            if (message.message.equals("photo")) {
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
            }

            viewHolder.binding.message.text = message.message
            viewHolder.binding.timestamp.text = formattedTimestamp

            // Long click listener for received messages (no "Delete for Everyone")
            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)

                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()

                // "Delete for Everyone" is hidden since it's a received message
                binding.everyone.visibility = View.GONE

                binding.delete.setOnClickListener {
                    deleteMessageForReceiver(message)
                    dialog.dismiss()
                }

                binding.cancel.setOnClickListener { dialog.dismiss() }

                dialog.show()
                false
            }
        }
    }

    private fun formatTimestamp(timestamp: Long?): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timestamp?.let { sdf.format(Date(it)) } ?: ""
    }

    private fun deleteMessageForEveryone(message: Message) {
        val messageText = "\uD83D\uDEABYou deleted this message."
        val spannableString = SpannableString(messageText)
        spannableString.setSpan(ForegroundColorSpan(Color.GRAY), 0, messageText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(StyleSpan(Typeface.ITALIC), 0, messageText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        message.message = spannableString.toString()

        message.messageId?.let { it1 ->
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection("Chats")
                .document(senderRoom)
                .collection("messages")
                .document(it1)
                .set(message)

            firestore.collection("Chats")
                .document(receiverRoom)
                .collection("messages")
                .document(it1)
                .set(message)
        }
    }

    private fun deleteMessageForSender(message: Message) {
        message.messageId?.let { it1 ->
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection("Chats")
                .document(senderRoom)
                .collection("messages")
                .document(it1)
                .delete()
        }
    }

    private fun deleteMessageForReceiver(message: Message) {
        message.messageId?.let { it1 ->
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection("Chats")
                .document(receiverRoom)
                .collection("messages")
                .document(it1)
                .delete()
        }
    }
    inner class SentMsgHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var binding: SendMsgBinding = SendMsgBinding.bind(itemView)
    }

    inner class ReceiveMsgHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var binding: ReceiveMsgBinding = ReceiveMsgBinding.bind(itemView)
    }
    init {
        if(messages != null){
            this.messages = messages
        }
        this.senderRoom = senderRoom
        this.receiverRoom = receiverRoom
    }
}