package za.co.varsitycollege.st10215473.community.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import za.co.varsitycollege.st10215473.community.ServiceChatActivity
import za.co.varsitycollege.st10215473.community.data.Customer
import za.co.varsitycollege.st10215473.community.data.ServiceProvider
import za.co.varsitycollege.st10215473.community.databinding.ChatListCardviewBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ChatListAdapter(private val userList:java.util.ArrayList<ServiceProvider>): RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ChatListCardviewBinding): RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListAdapter.ViewHolder {
        return ViewHolder(ChatListCardviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ChatListAdapter.ViewHolder, position: Int) {
        val currentItem = userList[position]
        holder.apply {
            binding.apply {
                txtFullName.text = currentItem.Name
                txtLastMessage.text = currentItem.lastMessage
                val timestamp = currentItem.lastMessageTime // This is a Timestamp object
                if (timestamp != null) {
                    val date = timestamp.toDate() // Convert Timestamp to Date
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()) // Set the desired time format
                    txtTime.text = timeFormat.format(date) // Format the date to get only the time
                } else {
                     // Handle case where time is null
                }
                holder.itemView.setOnClickListener{
                    val intent = Intent(holder.itemView.context, ServiceChatActivity::class.java)
                    intent.putExtra("name", currentItem.Name)
                    //intent.putExtra("image", currentItem.Name)
                    intent.putExtra("Id", currentItem.Id)
                    holder.itemView.context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

}