package za.co.varsitycollege.st10215473.community

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var image1Button: ImageView
    private lateinit var image2Button: ImageView
    private lateinit var image3Button: ImageView
    private lateinit var image4Button: ImageView
    private lateinit var saveButton: ImageView
    private lateinit var nameText: EditText
    private lateinit var surnameText: EditText
    private lateinit var emailText: EditText
    private lateinit var bioText: EditText
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var subcategoryChipGroup: ChipGroup
    private lateinit var aboutme: TextView
    private lateinit var profileBio: EditText
    private lateinit var cat: TextView
    private lateinit var selectCat: TextView
    private lateinit var selectSubCat: TextView
    private lateinit var selectCategories: ChipGroup
    private lateinit var addCatalogue: TextView
    private lateinit var scrollViewImage: HorizontalScrollView

    private var profileUri: Uri? = null // Store profile picture URI
    private var uriList: Array<Uri?> = arrayOfNulls(4)  // To store URIs for the 4 images
    private var isImageUpdated: Array<Boolean> = arrayOf(false, false, false, false) // Track if images are updated
    private var currentImageIndex = -1  // To track which image is being updated

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    private lateinit var firebaseRef: FirebaseFirestore
    private lateinit var storageRef: FirebaseStorage

    private val cameraPermissionRequestCode = 101
    private val storagePermissionRequestCode = 102

    // Categories and Subcategories
    private val categories = listOf("Cleaning", "Handyman", "Gardening", "Electrical")
    private val subcategories = mapOf(
        "Cleaning" to listOf("Window Cleaning", "Carpet Cleaning", "Upholstery", "Laundry", "General"),
        "Handyman" to listOf("General Repairs", "Furniture Assembly", "Painting", "Plumbing"),
        "Gardening" to listOf("Lawn Mowing", "Maintenance", "Tree Trimming"),
        "Electrical" to listOf("Electrical Repairs", "Lighting Installation", "Appliance Installation", "Solar-Panel Installation")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        firebaseRef = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance()

        profileImage = findViewById(R.id.imgProfilePic)
        saveButton = findViewById(R.id.imgSave)
        nameText = findViewById(R.id.edtProfileName)
        surnameText = findViewById(R.id.edtProfileSurname)
        emailText = findViewById(R.id.edtProfileEmail)
        bioText = findViewById(R.id.edtProfileBio)
        image1Button = findViewById(R.id.imgProfileImage1)
        image2Button = findViewById(R.id.imgProfileImage2)
        image3Button = findViewById(R.id.imgProfileImage3)
        image4Button = findViewById(R.id.imgProfileImage4)
        categoryChipGroup = findViewById(R.id.category_chip_group)
        subcategoryChipGroup = findViewById(R.id.subcategory_chip_group)
        aboutme = findViewById(R.id.textView16)
        cat= findViewById(R.id.textView17)
        selectCat= findViewById(R.id.txtSelectCategory)
        selectSubCat= findViewById(R.id.txtSelectSubCategory)
        addCatalogue= findViewById(R.id.textView23)
        scrollViewImage= findViewById(R.id.horizontalScrollView)

        // Populate category chips
        populateCategoryChips()

        // Set listener for category selection
        categoryChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedCategories = checkedIds.map { id ->
                    group.findViewById<Chip>(id).text.toString()
                }
                // Populate subcategories based on selected categories
                populateSubcategoryChips(selectedCategories)
            } else {
                subcategoryChipGroup.removeAllViews() // Clear subcategories if no category is selected
            }
        }

        // Register activity result for taking a picture
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
                if (isSuccess) {
                    if (currentImageIndex == -1) {
                        Glide.with(this).load(profileUri).circleCrop().into(profileImage)
                    } else {
                        uriList[currentImageIndex]?.let { uri ->
                            when (currentImageIndex + 1) {
                                1 -> image1Button.setImageURI(uri)
                                2 -> image2Button.setImageURI(uri)
                                3 -> image3Button.setImageURI(uri)
                                4 -> image4Button.setImageURI(uri)
                            }
                            isImageUpdated[currentImageIndex] = true // Mark image as updated
                        }
                    }
                }
            }

        // Register activity result for picking an image from the gallery
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                if (currentImageIndex == -1) {
                    // Profile picture case
                    profileUri = it
                    Glide.with(this).load(profileUri).circleCrop().into(profileImage)
                } else {
                    // Handle regular image updates
                    uriList[currentImageIndex] = it
                    when (currentImageIndex + 1) {
                        1 -> image1Button.setImageURI(it)
                        2 -> image2Button.setImageURI(it)
                        3 -> image3Button.setImageURI(it)
                        4 -> image4Button.setImageURI(it)
                    }
                    isImageUpdated[currentImageIndex] = true // Mark image as updated
                }
            }
        }

        // Set click listeners for each image
        setupImageView(image1Button, 1)
        setupImageView(image2Button, 2)
        setupImageView(image3Button, 3)
        setupImageView(image4Button, 4)

        profileImage.setOnClickListener {
            currentImageIndex = -1 // Set to -1 to indicate profile picture
            showImageSelectionDialog()
        }
        currentImageIndex = -1

        // Load user profile
        loadUserProfile()

        // Save button listener
        saveButton.setOnClickListener {
            val name = nameText.text.toString()
            val surname = surnameText.text.toString()
            val email = emailText.text.toString()
            val bio = bioText.text.toString()

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            // Get selected categories and subcategories
            val selectedCategories = getSelectedChips(categoryChipGroup)
            val selectedSubcategories = getSelectedChips(subcategoryChipGroup)

            // Create a map to update user info
            val userUpdates = hashMapOf(
                "name" to name,
                "surname" to surname,
                "email" to email,
                "bio" to bio,
                "category" to selectedCategories,
                "subCategory" to selectedSubcategories
            )

            firebaseRef.collection("ServiceProviders").document(userId).update(userUpdates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    uploadProfilePicture()
                    uploadUpdatedImages()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun populateCategoryChips() {
        categories.forEach { category ->
            val chip = Chip(this).apply {
                text = category
                isCheckable = true
                setChipBackgroundColorResource(R.color.chip_background_default)
                setTextColor(resources.getColor(R.color.chip_text_color, null))

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        setChipBackgroundColorResource(R.color.chip_background_selected)
                    } else {
                        setChipBackgroundColorResource(R.color.chip_background_default)
                    }
                }
            }
            categoryChipGroup.addView(chip)
        }
    }

    private fun populateSubcategoryChips(selectedCategories: List<String>) {
        subcategoryChipGroup.removeAllViews()

        selectedCategories.forEach { selectedCategory ->
            subcategories[selectedCategory]?.forEach { subcategory ->
                val chip = Chip(this).apply {
                    text = subcategory
                    isCheckable = true
                    setChipBackgroundColorResource(R.color.chip_background_default)
                    setTextColor(resources.getColor(R.color.chip_text_color, null))

                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            setChipBackgroundColorResource(R.color.chip_background_selected)
                        } else {
                            setChipBackgroundColorResource(R.color.chip_background_default)
                        }
                    }
                }
                subcategoryChipGroup.addView(chip)
            }
        }
    }

    private fun getSelectedChips(chipGroup: ChipGroup): List<String> {
        val selectedChips = mutableListOf<String>()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (chip.isChecked) {
                selectedChips.add(chip.text.toString())
            }
        }
        return selectedChips
    }

    private fun setupImageView(imageView: ImageView, index: Int) {
        imageView.setOnClickListener {
            currentImageIndex = index - 1
            showImageSelectionDialog()
        }
    }

    private fun showImageSelectionDialog() {
        val options = arrayOf("Take a photo", "Choose from gallery")
        AlertDialog.Builder(this)
            .setTitle("Select image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkAndRequestCameraPermissions() // Check for camera permissions
                    1 -> checkAndRequestStoragePermissions() // Check for storage permissions
                }
            }.show()
    }

    private fun checkAndRequestCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), cameraPermissionRequestCode)
        } else {
            takePhoto() // Permission already granted
        }
    }

    private fun checkAndRequestStoragePermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), storagePermissionRequestCode)
        } else {
            pickImage() // Permission already granted
        }
    }

    // Handle permission request results
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            cameraPermissionRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto() // Permission granted
                } else {
                    Toast.makeText(this, "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show()
                }
            }
            storagePermissionRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImage() // Permission granted
                } else {
                    Toast.makeText(this, "Storage permission is required to choose a photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun takePhoto() {
        val fileUri = createImageUri() ?: return

        if (currentImageIndex == -1) {
            // Profile picture case
            profileUri = fileUri // Store the profile picture URI
        } else {
            uriList[currentImageIndex] = fileUri // Store the URI in uriList
        }

        takePictureLauncher.launch(fileUri)
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun createImageUri(): Uri {
        val imageFile = File(cacheDir, "image_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", imageFile)
    }

    private fun loadUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firebaseRef.collection("ServiceProviders").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Retrieve fields, defaulting to empty strings if null
                    val name = document.getString("name") ?: ""
                    val surname = document.getString("surname") ?: ""
                    val email = document.getString("email") ?: ""
                    val bio = document.getString("bio") ?: ""
                    val profileUrl = document.getString("profileUrl")

                    // Set EditText fields
                    nameText.setText(name)
                    surnameText.setText(surname)
                    emailText.setText(email)
                    bioText.setText(bio)

                    // Set profile image or default image
                    if (profileUrl.isNullOrEmpty()) {
                        profileImage.setImageResource(R.drawable.profile) // Default profile picture
                    } else {
                        Glide.with(this).load(profileUrl).circleCrop().into(profileImage)
                    }

                    // Retrieve categories and subcategories, defaulting to empty lists if null
                    val categories = document.get("category") as? List<String> ?: emptyList()
                    val subcategories = document.get("subCategory") as? List<String> ?: emptyList()

                    // Select the relevant category and subcategory chips
                    selectCategoryChips(categories)
                    selectSubcategoryChips(subcategories)

                    // Set default images for image1-4 if they are null
                    for (i in 1..4) {
                        val imageUrl = document.getString("image$i")
                        if (imageUrl.isNullOrEmpty()) {
                            when (i) {
                                1 -> image1Button.setImageResource(R.drawable.image_placeholder)
                                2 -> image2Button.setImageResource(R.drawable.image_placeholder)
                                3 -> image3Button.setImageResource(R.drawable.image_placeholder)
                                4 -> image4Button.setImageResource(R.drawable.image_placeholder)
                            }
                        } else {
                            // Load the existing image using Glide
                            Glide.with(this).load(imageUrl).into(
                                when (i) {
                                    1 -> image1Button
                                    2 -> image2Button
                                    3 -> image3Button
                                    4 -> image4Button
                                    else -> throw IllegalArgumentException("Invalid image index")
                                }
                            )
                        }
                    }
                }else{
                    firebaseRef.collection("Consumer").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                // Retrieve name, surname, bio
                                val name = document.getString("name") ?: ""
                                val surname = document.getString("surname") ?: ""
                                val email =
                                    document.getString("bio") ?: "Add a bio in the edit page"
                                val email = document.getString("email") ?: ""
                              
                                nameText.setText(name)
                                surnameText.setText(surname)
                                emailText.setText(email)

                                bioText.visibility = View.GONE
                                categoryChipGroup.visibility = View.GONE
                                subcategoryChipGroup.visibility = View.GONE
                                aboutme.visibility = View.GONE
                                cat.visibility = View.GONE
                                selectCat.visibility = View.GONE
                                selectSubCat.visibility = View.GONE
                                addCatalogue.visibility = View.GONE
                                scrollViewImage.visibility = View.GONE

                                // Set profile picture or default
                                val profileUrl = document.getString("profileUrl")
                                if (!profileUrl.isNullOrEmpty()) {
                                    Glide.with(this)
                                        .load(profileUrl)
                                        .circleCrop()
                                        .into(profileImage)
                                } else {
                                    profileImage.setImageResource(R.drawable.profile)
                                }
                            }
                        }
                }
            }
    }


    private fun selectCategoryChips(selectedCategories: List<String>) {
        for (i in 0 until categoryChipGroup.childCount) {
            val chip = categoryChipGroup.getChildAt(i) as Chip
            if (selectedCategories.contains(chip.text.toString())) {
                chip.isChecked = true
            }
        }
    }

    private fun selectSubcategoryChips(selectedSubcategories: List<String>) {
        for (i in 0 until subcategoryChipGroup.childCount) {
            val chip = subcategoryChipGroup.getChildAt(i) as Chip
            if (selectedSubcategories.contains(chip.text.toString())) {
                chip.isChecked = true
            }
        }
    }

    private fun uploadProfilePicture() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        profileUri?.let { uri ->
            val profilePicRef = storageRef.reference.child("profilePictures/$userId/profile.jpg")
            profilePicRef.putFile(uri).addOnSuccessListener {
                profilePicRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    firebaseRef.collection("ServiceProviders").document(userId)
                        .update("profileUrl", downloadUrl.toString())
                }
            }
        }
    }

    private fun uploadUpdatedImages() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        for (i in uriList.indices) {
            if (isImageUpdated[i]) {
                uriList[i]?.let { uri ->
                    val imageRef = storageRef.reference.child("serviceProviderImages/$userId/image${i + 1}.jpg")
                    imageRef.putFile(uri).addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            val field = "image${i+1}"
                            firebaseRef.collection("ServiceProviders").document(userId)
                                .update(field, downloadUrl.toString())
                        }
                    }
                }
            }
        }
    }
}
