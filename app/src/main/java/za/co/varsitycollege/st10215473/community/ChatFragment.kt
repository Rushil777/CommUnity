import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10215473.community.ServiceChatActivity
import za.co.varsitycollege.st10215473.community.adapter.ChatListAdapter
import za.co.varsitycollege.st10215473.community.data.Customer
import za.co.varsitycollege.st10215473.community.databinding.FragmentChatBinding

class ChatFragment : Fragment(), ChatListAdapter.OnChatClickListener {

    private lateinit var firebaseRef: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentChatBinding
    private lateinit var chatListAdapter: ChatListAdapter
    private lateinit var chatList: ArrayList<Customer>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        firebaseRef = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        chatList = ArrayList()
        chatListAdapter = ChatListAdapter(chatList, this)
        binding.chatFragmentRV.layoutManager = LinearLayoutManager(requireContext())
        binding.chatFragmentRV.adapter = chatListAdapter


        loadChatList()

        return binding.root
    }

    private fun loadChatList() {

        val currentUserId = auth.currentUser?.uid

        firebaseRef.collection("users")
            .whereEqualTo("role", "Customer")
            .get()
            .addOnSuccessListener { documents ->
                chatList.clear()
                for (document in documents) {
                    val customer = document.toObject(Customer::class.java)

                    if (customer.userId != currentUserId) {
                        chatList.add(customer)
                    }
                }
                chatListAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->

            }
    }

    override fun onChatClicked(userId: String?) {
        val intent = Intent(requireContext(), ServiceChatActivity::class.java)
        intent.putExtra("USER_ID", userId)
        startActivity(intent)
    }
}
