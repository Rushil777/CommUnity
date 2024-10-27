package za.co.varsitycollege.st10215473.community

import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile)  // Use a similar layout as the selection screen

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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
                        "name" to provider.name,
                        "surname" to provider.surname,
                        "imageUrl" to provider.image1, // Take the first image for simplicity
                        "rating" to rating
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
                }
            }
            .addOnFailureListener {
                // Handle error (e.g., provider not found)
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
