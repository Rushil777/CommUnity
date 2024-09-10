package za.co.varsitycollege.st10215473.community.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import za.co.varsitycollege.st10215473.community.data.Customer
import za.co.varsitycollege.st10215473.community.databinding.ChatListCardviewBinding

class ChatListAdapter(private val userList:java.util.ArrayList<Customer>, private val clickListener: OnChatClickListener): RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ChatListCardviewBinding): RecyclerView.ViewHolder(binding.root), View.OnClickListener{

        init{
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val userId = userList[adapterPosition].userId
            clickListener.onChatClicked(userId)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListAdapter.ViewHolder {
        return ViewHolder(ChatListCardviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ChatListAdapter.ViewHolder, position: Int) {
        val currentItem = userList[position]
        holder.apply {
            binding.apply {
                txtFullName.text = currentItem.name
                txtTime.text = "12:00"
                txtLastMessage.text = "Hello"
            }
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    interface OnChatClickListener{
        fun onChatClicked(userId: String?)
    }

}