package za.co.varsitycollege.st10215473.community

import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import za.co.varsitycollege.st10215473.community.data.ServiceProvider

class ViewProfileActivity : AppCompatActivity() {

    private lateinit var image: ImageView
    private lateinit var bioTextView: TextView
    private lateinit var nameText: TextView
    private var imageIndex = 0
    private var imageList: List<String> = emptyList()
    private lateinit var serviceProviderId: String
    private lateinit var giveRatingText: TextView
    private lateinit var favouritesButton: FloatingActionButton
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var ratingBar: RatingBar
    private lateinit var chipGroupCategories: ChipGroup
    private lateinit var chipGroupSubcategories: ChipGroup
    private lateinit var rating: TextView
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile)  // Use a similar layout as the selection screen

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        backButton = findViewById(R.id.btnViewBackButton)
        rating = findViewById(R.id.txtProfileRating)
        chipGroupCategories = findViewById(R.id.chipGroupCategories)
        chipGroupSubcategories = findViewById(R.id.chipGroupSubcategories)
        ratingBar = findViewById(R.id.ratingBar)
        favouritesButton = findViewById(R.id.nextButton)
        image = findViewById(R.id.profile_image)
        giveRatingText = findViewById(R.id.txtGiveRating)
        bioTextView = findViewById(R.id.txtBio)
        nameText = findViewById(R.id.txtName)

        // Get the service provider ID passed from ServiceChatActivity
        serviceProviderId = intent.getStringExtra("id") ?: ""

        // Fetch and display the service provider's data
        loadServiceProviderProfile(serviceProviderId)

        backButton.setOnClickListener{
            onBackPressed()
        }

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

        favouritesButton.setOnClickListener{
            addToFavourites()
        }
    }

    private fun addToFavourites() {
        val currentUserId = auth.currentUser?.uid ?: return

        // Get the selected rating
        val rating = ratingBar.rating

        firestore.collection("ServiceProviders")
            .document(serviceProviderId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val provider = documentSnapshot.toObject(ServiceProvider::class.java)
                if (provider != null) {
                    // Create a favourites map
                    val favouritesData = hashMapOf(
                        "id" to provider.id,
                        "name" to provider.name,
                        "surname" to provider.surname,
                        "imageUrl" to provider.profileUrl,
                        "rating" to rating,
                        "bio" to provider.bio
                    )

                    // Save to Firestore under the user's favourites collection
                    firestore.collection("Consumer").document(currentUserId)
                        .collection("Favourites")
                        .document(serviceProviderId)
                        .set(favouritesData, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Added to Favourites!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to add to Favourites.", Toast.LENGTH_SHORT).show()
                        }

                    updateProviderAverageRating(rating)
                }
            }
    }

    private fun updateProviderAverageRating(newRating: Float) {
        val providerRef = firestore.collection("ServiceProviders").document(serviceProviderId)

        providerRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Retrieve the current rating count and total rating
                val currentTotalRating = document.getDouble("totalRating") ?: 0.0
                val currentRatingCount = document.getLong("ratingCount") ?: 0

                // Calculate the new average rating
                val newTotalRating = currentTotalRating + newRating
                val newRatingCount = currentRatingCount + 1
                val newAverageRating = newTotalRating / newRatingCount

                // Update fields in Firestore
                val ratingData = mapOf(
                    "totalRating" to newTotalRating,
                    "ratingCount" to newRatingCount,
                    "averageRating" to newAverageRating
                )

                providerRef.set(ratingData, SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Average rating updated!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to update average rating.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun loadServiceProviderProfile(providerId: String) {
        // Fetch the service provider details from Firestore
        FirebaseFirestore.getInstance().collection("ServiceProviders")
            .document(providerId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val provider = documentSnapshot.toObject(ServiceProvider::class.java)
                if (provider != null) {
                    // Display bio and full name

                    rating.text = if (provider.averageRating != null && provider.averageRating > 0) {
                        provider.averageRating.toString()
                    } else {
                        "No Rating"
                    }
                    bioTextView.text = provider.bio
                    nameText.text = "${provider.name} ${provider.surname}"
                    giveRatingText.text = "Give ${provider.name} ${provider.surname} a rating!"

                    // Prepare the image list (filter out empty image URLs)
                    imageList = listOf(provider.image1, provider.image2, provider.image3, provider.image4)
                        .filter { it.isNotEmpty() }

                    // Display the first image if available
                    if (imageList.isNotEmpty()) {
                        imageIndex = 0
                        showImage()
                    }

                    val categories = provider.category as? List<String> ?: emptyList()
                    val subcategories = provider.subCategory as? List<String> ?: emptyList()
                    displayChips(categories, chipGroupCategories)
                    displayChips(subcategories, chipGroupSubcategories)
                }
            }
            .addOnFailureListener {
                // Handle error (e.g., provider not found)
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

    private fun showImage() {
        val imageUrl = imageList[imageIndex]
        Glide.with(this).load(imageUrl).into(image)
    }
}
