package za.co.varsitycollege.st10215473.community

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
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
    private var userId = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var chipGroupCategories: ChipGroup
    private lateinit var chipGroupSubcategories: ChipGroup
    private lateinit var rating: TextView
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_provider_selection)

        firebaseRef = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        backButton = findViewById(R.id.btnSelectionBackButton)
        rating = findViewById(R.id.txtProfileRating)
        chipGroupCategories = findViewById(R.id.chipGroupCategories)
        chipGroupSubcategories = findViewById(R.id.chipGroupSubcategories)
        prevProviderButton = findViewById(R.id.btnPrevious)
        selectButton = findViewById(R.id.btnSelect)
        image = findViewById(R.id.profile_image)
        bioTextView = findViewById(R.id.txtBio)
        nextProviderButton = findViewById(R.id.nextButton)
        nameText = findViewById(R.id.txtName)

        val selectedProviderId = intent.getStringExtra("selectedProviderId")

        if (!selectedProviderId.isNullOrEmpty()) {
            // Fetch and display the specific service provider's details
            firebaseRef.collection("ServiceProviders").document(selectedProviderId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val provider = document.toObject(ServiceProvider::class.java)
                        if (provider != null) {
                            serviceProviderList = listOf(provider) // Only show the selected provider
                            currentProviderIndex = 0
                            showServiceProvider(currentProviderIndex)
                        }
                    } else {
                        Toast.makeText(this, "Provider not found.", Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to load provider: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        val selectedSubcategory = intent.getStringExtra("selectedSubcategory")

        // Fetch only providers with the matching subcategory
        fetchServiceProviders(selectedSubcategory)

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

        backButton.setOnClickListener{
            onBackPressed()
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

            firebaseRef.collection("Consumer").document(currentUserId)
                .get()
                .addOnSuccessListener { consumerDoc ->
                    val selectedProviders = consumerDoc.get("selectedProviders") as? MutableList<String> ?: mutableListOf()

                    if (!selectedProviders.contains(provider.id)) {
                        selectedProviders.add(provider.id) // This should now work
                        firebaseRef.collection("Consumer").document(currentUserId)
                            .update("selectedProviders", selectedProviders)
                            .addOnSuccessListener {
                                showNextServiceProvider()
                                Toast.makeText(this, "You can now message the service provider", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener {
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle potential errors when fetching the document
                    Log.e("FirestoreError", "Error fetching consumer document: ", exception)
                }
        }
    }


    private fun fetchServiceProviders(selectedSubcategory: String?) {
        // Fetch the current consumer's location
        firebaseRef.collection("Consumer").document(userId ?: return)
            .get()
            .addOnSuccessListener { consumerDoc ->
                val consumerLocation = consumerDoc.getGeoPoint("location")

                FirebaseFirestore.getInstance().collection("ServiceProviders")
                    .whereArrayContains("subCategory", selectedSubcategory ?: "")
                    .get()
                    .addOnSuccessListener { result ->
                        val allProviders = result.toObjects(ServiceProvider::class.java)

                        serviceProviderList = if (consumerLocation != null) {
                            allProviders.filter { provider ->
                                provider.location?.let { providerLocation ->
                                    val distance = calculateDistance(
                                        consumerLocation.latitude,
                                        consumerLocation.longitude,
                                        providerLocation.latitude,
                                        providerLocation.longitude
                                    )
                                    distance <= 50
                                } ?: true
                            }
                        } else {
                            allProviders
                        }

                        if (serviceProviderList.isNotEmpty()) {
                            showServiceProvider(currentProviderIndex)
                            loadCategoriesAndSubcategories(serviceProviderList[currentProviderIndex].id)
                        } else {
                            val selectedProviderId = intent.getStringExtra("selectedProviderId")

                            if (selectedProviderId.isNullOrEmpty()) {
                                val alertDialog = AlertDialog.Builder(this)
                                    .setTitle("No Providers Found")
                                    .setMessage("There are no providers with the subcategory you chose near you.")
                                    .setPositiveButton("OK") { dialog, _ ->
                                        dialog.dismiss()
                                        onBackPressed()
                                    }
                                    .setCancelable(false)
                                    .create()
                                alertDialog.show()
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to load providers: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load consumer location: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371.0 // Radius of the earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }


    private fun loadCategoriesAndSubcategories(providerId: String) {
        firebaseRef.collection("ServiceProviders").document(providerId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Retrieve and cast the categories and subcategories
                    val categories = document.get("category") as? List<String> ?: emptyList()
                    val subcategories = document.get("subCategory") as? List<String> ?: emptyList()

                    // Display the chips for categories and subcategories
                    displayChips(categories, chipGroupCategories)
                    displayChips(subcategories, chipGroupSubcategories)
                } else {
                    Toast.makeText(this, "Document does not exist", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "${exception.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun displayChips(items: List<String>, chipGroup: ChipGroup) {
        chipGroup.removeAllViews()
        items.forEach { item ->
            val chip = Chip(this).apply {
                text = item
                isCheckable = false
                setChipBackgroundColorResource(R.color.chip_background_default)
                setTextColor(resources.getColor(R.color.chip_text_color, null))
            }
            chipGroup.addView(chip)
        }
    }

    // Show a specific service provider's images and bio
    private fun showServiceProvider(index: Int) {
        if (index in serviceProviderList.indices) {
            val provider = serviceProviderList[index]

            imageList = listOf(provider.image1, provider.image2, provider.image3, provider.image4)
                .filter { it.isNotEmpty() }

            if (imageList.isEmpty()) {
                showNextServiceProvider() // Move to the next service provider
                return
            }

            rating.text = if (provider.averageRating != null && provider.averageRating > 0) {
                provider.averageRating.toString()
            } else {
                "No Rating"
            }
            bioTextView.text = provider.bio
            nameText.text = "${provider.name} ${provider.surname}"

            loadCategoriesAndSubcategories(provider.id)

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
            currentProviderIndex = 0
        }
        showServiceProvider(currentProviderIndex)
    }

    // Load the selected image into the ImageView
    private fun showImage() {
        val imageUrl = imageList[imageIndex]
        Glide.with(this).load(imageUrl).into(image)
    }
}
