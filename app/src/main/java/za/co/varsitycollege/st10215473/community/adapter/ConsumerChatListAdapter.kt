package za.co.varsitycollege.st10215473.community.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import za.co.varsitycollege.st10215473.community.R
import za.co.varsitycollege.st10215473.community.ServiceChatActivity
import za.co.varsitycollege.st10215473.community.data.Customer
import za.co.varsitycollege.st10215473.community.databinding.ChatListCardviewBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ConsumerChatListAdapter(private val customerList: ArrayList<Customer>) : RecyclerView.Adapter<ConsumerChatListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ChatListCardviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ChatListCardviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = customerList[position]
        holder.apply {
            binding.apply {
                txtFullName.text = currentItem.name
                txtLastMessage.text = currentItem.lastMessage

                Glide.with(holder.itemView.context)
                    .load(currentItem.profileUrl)
                    .placeholder(R.drawable.profile_circle)
                    .error(R.drawable.profile_circle)
                    .circleCrop()
                    .into(imageView2) // ImageView ID in your layout

                val timestamp = currentItem.lastMessageTime
                if (timestamp != null) {
                    val date = timestamp.toDate()
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    txtTime.text = timeFormat.format(date)
                }
                holder.itemView.setOnClickListener {
                    val intent = Intent(holder.itemView.context, ServiceChatActivity::class.java)
                    intent.putExtra("name", currentItem.name)
                    intent.putExtra("profileUrl", currentItem.profileUrl)
                    intent.putExtra("id", currentItem.id)
                    holder.itemView.context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return customerList.size
    }
}
