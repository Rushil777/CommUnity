package za.co.varsitycollege.st10215473.community

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
    ): View {
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
        consumerList.clear()

        if (isAdded) {
            consumerChatListAdapter = ConsumerChatListAdapter(consumerList)
            binding.chatFragmentRV.layoutManager = LinearLayoutManager(requireContext())
            binding.chatFragmentRV.adapter = consumerChatListAdapter
        }

        firebaseRef.collection("Consumer")
            .get()
            .addOnSuccessListener { consumerSnapshot ->
                if (consumerSnapshot.isEmpty) {
                    if(isAdded){
                        Toast.makeText(requireContext(), "No consumers found in Consumer collection", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }
                }

                val consumerIds = consumerSnapshot.documents.mapNotNull { it.id }

                for (consumerId in consumerIds) {
                    val chatDocumentId = providerId + consumerId  // Construct the document ID

                    firebaseRef.collection("Chats")
                        .document(chatDocumentId)
                        .collection("messages")
                        .get()
                        .addOnSuccessListener { messagesSnapshot ->
                            if (!messagesSnapshot.isEmpty) {
                                firebaseRef.collection("Consumer").document(consumerId)
                                    .get()
                                    .addOnSuccessListener { consumerDocument ->
                                        if (consumerDocument.exists()) {
                                            val consumer = consumerDocument.toObject(Customer::class.java)
                                            if (consumer != null) {
                                                consumerList.add(consumer)
                                            }
                                            if(isAdded){
                                                consumerChatListAdapter.notifyDataSetChanged()
                                            }
                                        }
                                    }
                            }
                        }

                }
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
                                    if(isAdded){
                                        chatListAdapter = ServiceChatListAdapter(serviceProviderList)
                                        binding.chatFragmentRV.layoutManager = LinearLayoutManager(requireContext())
                                        binding.chatFragmentRV.adapter = chatListAdapter
                                    }
                                }
                            }
                    }
                }
            }
    }
}
