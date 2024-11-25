package za.co.varsitycollege.st10215473.community

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Source

class ProfileFragment : Fragment() {
    private lateinit var editButton: ImageView
    private lateinit var profilePicture: ImageView
    private lateinit var fullName: TextView
    private lateinit var bioText: TextView
    private lateinit var chipGroupCategories: ChipGroup
    private lateinit var chipGroupSubcategories: ChipGroup
    private lateinit var image1: ImageView
    private lateinit var image2: ImageView
    private lateinit var image3: ImageView
    private lateinit var image4: ImageView
    private lateinit var logoutButton: ImageView
    private lateinit var aboutmeoremail: TextView
    private lateinit var cardViewCategories: CardView
    private lateinit var cardViewCatalogue: CardView
    private lateinit var profileRating: TextView
    private lateinit var ratingStar: ImageView



    private var firebaseRef = FirebaseFirestore.getInstance()
    private var userId = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val editProfileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == AppCompatActivity.RESULT_OK) {
            loadUserProfile()
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload the user profile data when the fragment becomes visible again
        loadUserProfile()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        profileRating = view.findViewById(R.id.txtProfileRating)
        ratingStar = view.findViewById(R.id.imgRatingStar)
        cardViewCatalogue = view.findViewById(R.id.CardViewCatalogue)
        cardViewCategories = view.findViewById(R.id.CardViewCategories)
        aboutmeoremail = view.findViewById(R.id.txtAboutMeorEmail)
        editButton = view.findViewById(R.id.imgEdit)
        profilePicture = view.findViewById(R.id.imgProfilePic)
        fullName = view.findViewById(R.id.txtDisplayFullName)
        bioText = view.findViewById(R.id.txtProfileBio)
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories)
        chipGroupSubcategories = view.findViewById(R.id.chipGroupSubcategories)
        image1 = view.findViewById(R.id.imgProfileImage1)
        image2 = view.findViewById(R.id.imgProfileImage2)
        image3 = view.findViewById(R.id.imgProfileImage3)
        image4 = view.findViewById(R.id.imgProfileImage4)
        logoutButton = view.findViewById(R.id.imgLogOut)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load user profile data after the view is created
        loadUserProfile()

        // Set up the logout button and edit button click listeners
        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        editButton.setOnClickListener {
            // Launch EditProfileActivity with the ActivityResultLauncher
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            editProfileLauncher.launch(intent)
        }
    }


    private fun showLogoutConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.show()
    }

    fun loadUserProfile() {
        userId?.let { uid ->
            firebaseRef.collection("ServiceProviders").document(uid).get(Source.SERVER)
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Update UI with the latest data
                        updateProfileData(document)
                        loadImages(document)
                    } else {
                        // If no data is found for service provider, check consumer
                        firebaseRef.collection("Consumer").document(uid).get()
                            .addOnSuccessListener { consumerDocument ->
                                if (consumerDocument.exists()) {
                                    // Update UI with consumer data
                                    val name = consumerDocument.getString("name") ?: ""
                                    val surname = consumerDocument.getString("surname") ?: ""
                                    val email = consumerDocument.getString("email") ?: ""

                                    fullName.text = "$name $surname"
                                    bioText.text = email
                                    aboutmeoremail.text = "Email"

                                    cardViewCategories.visibility = View.GONE
                                    cardViewCatalogue.visibility = View.GONE
                                    ratingStar.visibility = View.GONE
                                    profileRating.visibility = View.GONE

                                    // Set profile picture or default for consumer
                                    val profileUrl = consumerDocument.getString("profileUrl")
                                    if (!profileUrl.isNullOrEmpty()) {
                                        Glide.with(this)
                                            .load(profileUrl)
                                            .circleCrop()
                                            .into(profilePicture)
                                    } else {
                                        profilePicture.setImageResource(R.drawable.profile)
                                    }
                                }
                            }
                    }
                }
                .addOnFailureListener {
                    // Handle failure if needed (e.g., network issue)
                }
        }
    }

    private fun updateProfileData(document: DocumentSnapshot) {
        // Retrieve name, surname, bio, rating, etc.
        val name = document.getString("name") ?: ""
        val surname = document.getString("surname") ?: ""
        val bio = document.getString("bio") ?: "Add a bio in the edit page"
        val averageRating = document.getDouble("averageRating")

        profileRating.text = if (averageRating != null && averageRating > 0) {
            averageRating.toString()
        } else {
            "No Rating"
        }
        fullName.text = "$name $surname"
        bioText.text = bio

        // Set profile picture or default for service provider
        val profileUrl = document.getString("profileUrl")
        if (!profileUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(profileUrl)
                .circleCrop()
                .into(profilePicture)
        } else {
            profilePicture.setImageResource(R.drawable.profile)
        }

        // Set default images for image1-4 or load from Firestore
        loadImages(document)

        // Load categories and subcategories for service provider
        val categories = document.get("category") as? List<String> ?: emptyList()
        val subcategories = document.get("subCategory") as? List<String> ?: emptyList()
        displayChips(categories, chipGroupCategories)
        displayChips(subcategories, chipGroupSubcategories)
    }


    private fun loadImages(document: DocumentSnapshot) {
        val imageUrls = listOf(
            document.getString("image1"),
            document.getString("image2"),
            document.getString("image3"),
            document.getString("image4")
        )

        val imageViews = listOf(image1, image2, image3, image4)
        imageUrls.forEachIndexed { index, url ->
            val imageView = imageViews[index]
            if (!url.isNullOrEmpty()) {
                Glide.with(this).load(url).into(imageView)
            } else {
                imageView.setImageResource(R.drawable.image_placeholder)
            }
        }
    }

    private fun displayChips(items: List<String>, chipGroup: ChipGroup) {
        chipGroup.removeAllViews()
        items.forEach { item ->
            val chip = Chip(requireContext()).apply {
                text = item
                isCheckable = false
                setChipBackgroundColorResource(R.color.chip_background_default)
                setTextColor(resources.getColor(R.color.chip_text_color, null))
            }
            chipGroup.addView(chip)
        }
    }
}
