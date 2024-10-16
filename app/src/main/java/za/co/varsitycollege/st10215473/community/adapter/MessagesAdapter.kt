package za.co.varsitycollege.st10215473.community.adapter

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10215473.community.R
import za.co.varsitycollege.st10215473.community.data.Message
import za.co.varsitycollege.st10215473.community.databinding.DeleteLayoutBinding
import za.co.varsitycollege.st10215473.community.databinding.ReceiveMsgBinding
import za.co.varsitycollege.st10215473.community.databinding.SendMsgBinding

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
        if(holder.javaClass == SentMsgHolder::class.java){
            val viewHolder = holder as SentMsgHolder
            if(message.message.equals("photo")){
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility= View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE

            }
            viewHolder.binding.message.text = message.message
            viewHolder.itemView.setOnLongClickListener{
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding:DeleteLayoutBinding = DeleteLayoutBinding.bind(view)

                val dialog= AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()
                binding.everyone.setOnClickListener{
                    message.message = "This message is removed"
                    message.messageId?.let{it1 ->
                        val firestore = FirebaseFirestore.getInstance()

                        firestore.collection("Chats")
                            .document(senderRoom)
                            .collection("Messages")
                            .document(it1)
                            .set(message)
                            .addOnSuccessListener {

                            }
                            .addOnFailureListener { e ->
                                // Handle failure
                                Log.e("FirestoreError", "Error updating message: ", e)
                            }
                    }
                    message.messageId?.let{it1 ->
                        val firestore = FirebaseFirestore.getInstance()

                        firestore.collection("Chats")
                            .document(receiverRoom)
                            .collection("Messages")
                            .document(it1)
                            .set(message)
                            .addOnSuccessListener {

                            }
                            .addOnFailureListener { e ->
                                // Handle failure
                                Log.e("FirestoreError", "Error updating message: ", e)
                            }
                    }
                    dialog.dismiss()
                }
                binding.delete.setOnClickListener{
                    message.messageId?.let{it1 ->
                        val firestore = FirebaseFirestore.getInstance()

                        firestore.collection("Chats")
                            .document(senderRoom)
                            .collection("Messages")
                            .document(it1)
                            .delete()
                            .addOnSuccessListener {

                            }
                            .addOnFailureListener { e ->
                                // Handle failure
                                Log.e("FirestoreError", "Error updating message: ", e)
                            }
                    }
                    dialog.dismiss()
                }
                binding.cancel.setOnClickListener{dialog.dismiss()}

                dialog.show()
                false
            }
        }
        else{
            val viewHolder = holder as ReceiveMsgHolder
            if(message.message.equals("photo")){
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility= View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
            }
            viewHolder.binding.message.text = message.message
            viewHolder.itemView.setOnLongClickListener{
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding:DeleteLayoutBinding = DeleteLayoutBinding.bind(view)

                val dialog= AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()
                binding.everyone.setOnClickListener{
                    message.message = "This message is removed"
                    message.messageId?.let{it1 ->
                        val firestore = FirebaseFirestore.getInstance()

                        firestore.collection("Chats")
                            .document(senderRoom)
                            .collection("Messages")
                            .document(it1)
                            .set(message)
                            .addOnSuccessListener {

                            }
                            .addOnFailureListener { e ->
                                // Handle failure
                                Log.e("FirestoreError", "Error updating message: ", e)
                            }
                    }
                    message.messageId?.let{it1 ->
                        val firestore = FirebaseFirestore.getInstance()

                        firestore.collection("Chats")
                            .document(receiverRoom)
                            .collection("Messages")
                            .document(it1)
                            .set(message)
                            .addOnSuccessListener {

                            }
                            .addOnFailureListener { e ->
                                // Handle failure
                                Log.e("FirestoreError", "Error updating message: ", e)
                            }
                    }
                    dialog.dismiss()
                }
                binding.delete.setOnClickListener{
                    message.messageId?.let{it1 ->
                        val firestore = FirebaseFirestore.getInstance()

                        firestore.collection("Chats")
                            .document(senderRoom)
                            .collection("Messages")
                            .document(it1)
                            .delete()
                            .addOnSuccessListener {

                            }
                            .addOnFailureListener { e ->
                                // Handle failure
                                Log.e("FirestoreError", "Error updating message: ", e)
                            }
                    }
                    dialog.dismiss()
                }
                binding.cancel.setOnClickListener{dialog.dismiss()}

                dialog.show()
                false
            }
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