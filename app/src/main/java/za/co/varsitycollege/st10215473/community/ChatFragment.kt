import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10215473.community.adapter.ConsumerChatListAdapter
import za.co.varsitycollege.st10215473.community.adapter.ServiceChatListAdapter
import za.co.varsitycollege.st10215473.community.data.Customer
import za.co.varsitycollege.st10215473.community.data.ServiceProvider
import za.co.varsitycollege.st10215473.community.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    private lateinit var firebaseRef: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentChatBinding
    private lateinit var chatListAdapter: ServiceChatListAdapter
    private lateinit var consumerChatListAdapter: ConsumerChatListAdapter
    private lateinit var serviceProviderList: ArrayList<ServiceProvider>
    private lateinit var consumerList: ArrayList<Customer>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        firebaseRef = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        serviceProviderList = ArrayList()
        consumerList = ArrayList()

        checkUserTypeAndLoadChatList()

        return binding.root
    }

    private fun checkUserTypeAndLoadChatList() {
        val currentUserId = auth.currentUser?.uid

        // First, check if the user is a ServiceProvider
        firebaseRef.collection("ServiceProviders").document(currentUserId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // User is a ServiceProvider, load Consumers and use ConsumerChatListAdapter
                    loadConsumersList(currentUserId)
                } else {
                    // Check if the user is a Consumer
                    firebaseRef.collection("Consumer").document(currentUserId)
                        .get()
                        .addOnSuccessListener { consumerDocument ->
                            if (consumerDocument.exists()) {
                                // User is a Consumer, load ServiceProviders and use ChatListAdapter
                                loadServiceProvidersList(currentUserId)
                            }
                        }
                }
            }
    }

    private fun loadConsumersList(currentUserId: String) {
        firebaseRef.collection("Consumer")
            .get()
            .addOnSuccessListener { documents ->
                consumerList.clear()
                for (document in documents) {
                    val consumer = document.toObject(Customer::class.java)
                    if (consumer.id != currentUserId) {
                        consumerList.add(consumer)
                    }
                }
                consumerChatListAdapter = ConsumerChatListAdapter(consumerList)
                binding.chatFragmentRV.layoutManager = LinearLayoutManager(requireContext())
                binding.chatFragmentRV.adapter = consumerChatListAdapter
            }
    }

    private fun loadServiceProvidersList(currentUserId: String) {
        firebaseRef.collection("ServiceProviders")
            .get()
            .addOnSuccessListener { documents ->
                serviceProviderList.clear()
                for (document in documents) {
                    val serviceProvider = document.toObject(ServiceProvider::class.java)
                    if (serviceProvider.id != currentUserId) {
                        serviceProviderList.add(serviceProvider)
                    }
                }
                chatListAdapter = ServiceChatListAdapter(serviceProviderList)
                binding.chatFragmentRV.layoutManager = LinearLayoutManager(requireContext())
                binding.chatFragmentRV.adapter = chatListAdapter
            }
    }
}
