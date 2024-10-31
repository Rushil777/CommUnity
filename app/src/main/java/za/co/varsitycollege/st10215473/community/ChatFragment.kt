import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        val currentUserId = auth.currentUser?.uid ?: return

        // Check if the user is a ServiceProvider
        firebaseRef.collection("ServiceProviders").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // User is a ServiceProvider, load Consumers
                    loadConsumersForProvider(currentUserId)
                } else {
                    // Check if the user is a Consumer
                    firebaseRef.collection("Consumer").document(currentUserId)
                        .get()
                        .addOnSuccessListener { consumerDocument ->
                            if (consumerDocument.exists()) {
                                // User is a Consumer, load ServiceProviders
                                loadSelectedServiceProviders(currentUserId)
                            }
                        }
                }
            }
    }

    private fun loadConsumersForProvider(providerId: String) {
        // Fetch chat documents based on providerId
        firebaseRef.collection("Chats")
            .get()
            .addOnSuccessListener { chatSnapshot ->
                consumerList.clear()
                val consumerIds = mutableSetOf<String>()

                for (chatDocument in chatSnapshot.documents) {
                    val documentId = chatDocument.id
                    // Show Toast for providerId
                    Toast.makeText(requireContext(), "hello $providerId", Toast.LENGTH_SHORT).show()

                    // Check if the documentId starts with providerId
                    if (documentId.startsWith(providerId)) {
                        // Extract consumerId from the documentId
                        val consumerId = documentId.removePrefix(providerId)
                        consumerIds.add(consumerId)

                        // Now, go into the messages subcollection
                        firebaseRef.collection("Chats")
                            .document(documentId) // documentId = providerId + consumerId
                            .collection("messages") // Access the messages subcollection
                            .get()
                            .addOnSuccessListener { messagesSnapshot ->
                                if (!messagesSnapshot.isEmpty) {
                                    // Messages exist, proceed to load the consumer details
                                    firebaseRef.collection("Consumer").document(consumerId)
                                        .get()
                                        .addOnSuccessListener { consumerDocument ->
                                            if (consumerDocument.exists()) {
                                                val consumer = consumerDocument.toObject(Customer::class.java)
                                                if (consumer != null) {
                                                    consumerList.add(consumer)
                                                }
                                                // Notify adapter after consumer is added
                                                consumerChatListAdapter.notifyDataSetChanged()
                                            } else {
                                                Toast.makeText(requireContext(), "Consumer not found for ID: $consumerId", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    // No messages found in the subcollection
                                    Toast.makeText(requireContext(), "No messages found for chat with $consumerId", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }

                if (consumerIds.isNotEmpty()) {
                    // Set adapter outside loop
                    consumerChatListAdapter = ConsumerChatListAdapter(consumerList)
                    binding.chatFragmentRV.layoutManager = LinearLayoutManager(requireContext())
                    binding.chatFragmentRV.adapter = consumerChatListAdapter
                } else {
                    Toast.makeText(requireContext(), "No consumers found for provider: $providerId", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch chats for provider", Toast.LENGTH_SHORT).show()
            }
    }






    private fun loadSelectedServiceProviders(consumerId: String) {
        firebaseRef.collection("Consumer").document(consumerId)
            .get()
            .addOnSuccessListener { consumerDocument ->
                if (consumerDocument.exists()) {
                    val selectedProviderIds = consumerDocument.get("selectedProviders") as? List<String> ?: return@addOnSuccessListener

                    // Load the selected service providers
                    for (providerId in selectedProviderIds) {
                        firebaseRef.collection("ServiceProviders").document(providerId)
                            .get()
                            .addOnSuccessListener { providerDocument ->
                                if (providerDocument.exists()) {
                                    val serviceProvider = providerDocument.toObject(ServiceProvider::class.java)
                                    if (serviceProvider != null) {
                                        serviceProviderList.add(serviceProvider)
                                    }
                                    chatListAdapter = ServiceChatListAdapter(serviceProviderList)
                                    binding.chatFragmentRV.layoutManager = LinearLayoutManager(requireContext())
                                    binding.chatFragmentRV.adapter = chatListAdapter
                                }
                            }
                    }
                }
            }
    }

    private fun loadConsumersList() {
        firebaseRef.collection("Consumer")
            .get()
            .addOnSuccessListener { documents ->
                consumerList.clear()
                for (document in documents) {
                    val consumer = document.toObject(Customer::class.java)
                    consumerList.add(consumer) // Include all consumers
                }
                consumerChatListAdapter = ConsumerChatListAdapter(consumerList)
                binding.chatFragmentRV.layoutManager = LinearLayoutManager(requireContext())
                binding.chatFragmentRV.adapter = consumerChatListAdapter
            }
    }

    private fun loadServiceProvidersList() {
        firebaseRef.collection("ServiceProviders")
            .get()
            .addOnSuccessListener { documents ->
                serviceProviderList.clear()
                for (document in documents) {
                    val serviceProvider = document.toObject(ServiceProvider::class.java)
                    serviceProviderList.add(serviceProvider) // Include all service providers
                }
                chatListAdapter = ServiceChatListAdapter(serviceProviderList)
                binding.chatFragmentRV.layoutManager = LinearLayoutManager(requireContext())
                binding.chatFragmentRV.adapter = chatListAdapter
            }
    }
}
