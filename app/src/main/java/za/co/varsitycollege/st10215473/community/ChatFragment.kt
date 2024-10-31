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
        // Fetch messages from the provider's chat document
        firebaseRef.collection("Chats")
            .document(providerId) // Assuming this is the document for the provider
            .collection("messages") // Access the messages subcollection
            .get()
            .addOnSuccessListener { messagesSnapshot ->
                consumerList.clear() // Clear the current list
                val consumerIds = mutableSetOf<String>() // Use a set to avoid duplicates

                for (messageDocument in messagesSnapshot.documents) {
                    val senderId = messageDocument.getString("senderId")
                    if (senderId != null) {
                        // Add senderId to the set (assuming it corresponds to a consumer)
                        consumerIds.add(senderId)
                    }
                }

                // Fetch each consumer's data based on the unique IDs collected
                for (consumerId in consumerIds) {
                    firebaseRef.collection("Consumer").document(consumerId)
                        .get()
                        .addOnSuccessListener { consumerDocument ->
                            if (consumerDocument.exists()) {
                                val consumer = consumerDocument.toObject(Customer::class.java)
                                if (consumer != null) {
                                    consumerList.add(consumer)
                                }
                                // Notify the adapter after each consumer is loaded
                                consumerChatListAdapter.notifyDataSetChanged()
                            }
                        }
                }

                // Set the adapter outside the loop to avoid multiple reassignments
                consumerChatListAdapter = ConsumerChatListAdapter(consumerList)
                binding.chatFragmentRV.layoutManager = LinearLayoutManager(requireContext())
                binding.chatFragmentRV.adapter = consumerChatListAdapter
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
