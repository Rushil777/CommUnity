package za.co.varsitycollege.st10215473.community

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import za.co.varsitycollege.st10215473.community.data.ServiceProvider

class ServiceProviderSelection : AppCompatActivity() {

    private lateinit var image: ImageView
    private lateinit var bioTextView: TextView
    private lateinit var nextProviderButton: FloatingActionButton
    private lateinit var prevProviderButton: FloatingActionButton
    private var imageIndex = 0
    private var currentProviderIndex = 0
    private lateinit var serviceProviderList: List<ServiceProvider>
    private var imageList: List<String> = emptyList()
    private lateinit var nameText: TextView
    private lateinit var selectButton: FloatingActionButton
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseRef: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_provider_selection)

        firebaseRef = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        prevProviderButton = findViewById(R.id.btnPrevious)
        selectButton = findViewById(R.id.btnSelect)
        image = findViewById(R.id.profile_image)
        bioTextView = findViewById(R.id.txtBio)
        nextProviderButton = findViewById(R.id.nextButton)
        nameText = findViewById(R.id.txtName)

        // Fetch service providers data from Firestore
        fetchServiceProviders()

        // Image tap handling
        image.setOnTouchListener { v, event ->
            val width = v.width
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    if (event.x < width / 2) {
                        // Left side tap - previous image
                        showPreviousImage()
                    } else {
                        // Right side tap - next image
                        showNextImage()
                    }
                }
            }
            true
        }

        // Move to next service provider
        nextProviderButton.setOnClickListener {
            showNextServiceProvider()
        }

        selectButton.setOnClickListener {
            selectServiceProvider()
        }

        prevProviderButton.setOnClickListener{
            showPreviousServiceProvider()
        }
    }

    private fun showPreviousServiceProvider() {
        if (currentProviderIndex > 0) {
            currentProviderIndex--
        } else {
            currentProviderIndex = serviceProviderList.size - 1
        }
        showServiceProvider(currentProviderIndex)
    }

    private fun selectServiceProvider() {
        if (currentProviderIndex in serviceProviderList.indices) {
            val provider = serviceProviderList[currentProviderIndex]
            val currentUserId = auth.currentUser?.uid ?: return

            // Get the list of currently selected service providers
            firebaseRef.collection("Consumer").document(currentUserId)
                .get()
                .addOnSuccessListener { consumerDoc ->
                    // Change this to MutableList to allow modifications
                    val selectedProviders = consumerDoc.get("selectedProviders") as? MutableList<String> ?: mutableListOf()

                    // Add the selected provider's ID if not already present
                    if (!selectedProviders.contains(provider.id)) {
                        selectedProviders.add(provider.id) // This should now work
                        firebaseRef.collection("Consumer").document(currentUserId)
                            .update("selectedProviders", selectedProviders)
                            .addOnSuccessListener {
                                Toast.makeText(this, "You can now message the service provider", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener {
                                // Handle errors
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle potential errors when fetching the document
                    Log.e("FirestoreError", "Error fetching consumer document: ", exception)
                }
        }
    }


    // Fetch service providers from Firestore
    private fun fetchServiceProviders() {
        FirebaseFirestore.getInstance().collection("ServiceProviders")
            .get()
            .addOnSuccessListener { result ->
                serviceProviderList = result.toObjects(ServiceProvider::class.java)
                if (serviceProviderList.isNotEmpty()) {
                    showServiceProvider(currentProviderIndex)
                }
            }
            .addOnFailureListener {
                // Handle errors
            }
    }

    // Show a specific service provider's images and bio
    private fun showServiceProvider(index: Int) {
        if (index in serviceProviderList.indices) {
            val provider = serviceProviderList[index]

            // Filter out any empty image URLs
            imageList = listOf(provider.image1, provider.image2, provider.image3, provider.image4)
                .filter { it.isNotEmpty() }

            // If the service provider has no images, skip to the next provider
            if (imageList.isEmpty()) {
                showNextServiceProvider() // Move to the next service provider
                return
            }

            // Display bio and full name
            bioTextView.text = provider.bio
            nameText.text = "${provider.name} ${provider.surname}"

            // Start with the first image
            imageIndex = 0
            showImage()
        }
    }


    // Show previous image
    private fun showPreviousImage() {
        if (imageList.isNotEmpty()) {
            imageIndex = if (imageIndex > 0) {
                imageIndex - 1
            } else {
                imageList.size - 1 // Loop to the last image
            }
            showImage()
        }
    }

    // Show next image
    private fun showNextImage() {
        if (imageList.isNotEmpty()) {
            imageIndex = if (imageIndex < imageList.size - 1) {
                imageIndex + 1
            } else {
                0 // Loop back to the first image
            }
            showImage()
        }
    }

    // Show next service provider
    private fun showNextServiceProvider() {
        if (currentProviderIndex < serviceProviderList.size - 1) {
            currentProviderIndex++
        } else {
            currentProviderIndex = 0 // Loop back to the first provider
        }
        showServiceProvider(currentProviderIndex)
    }

    // Load the selected image into the ImageView
    private fun showImage() {
        val imageUrl = imageList[imageIndex]
        Glide.with(this).load(imageUrl).into(image)
    }
}
