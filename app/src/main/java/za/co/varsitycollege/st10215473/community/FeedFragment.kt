package za.co.varsitycollege.st10215473.community

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import za.co.varsitycollege.st10215473.community.adapter.FeedAdapter
import za.co.varsitycollege.st10215473.community.data.Feed
import java.io.File

class FeedFragment : Fragment(R.layout.fragment_feed) {

    private lateinit var imageViews: List<ImageView>
    private lateinit var descriptionEditText: EditText
    private lateinit var postButton: MaterialButton
    private lateinit var addFeedDialogButton: FloatingActionButton

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    private var imageUris = mutableListOf<Uri?>() // To store the image URIs for up to 4 images
    private var currentImageIndex = 0
    private val maxImages = 4
    private val cameraPermissionRequestCode = 101
    private val storagePermissionRequestCode = 102

    private lateinit var rvFeed: RecyclerView
    private lateinit var feedAdapter: FeedAdapter
    private var feedList = ArrayList<Feed>() // Initialize the feed list

    private lateinit var storageRef: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storageRef = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Register activity result for taking a picture
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                imageUris[currentImageIndex - 1]?.let {
                    Glide.with(this).load(it).into(imageViews[currentImageIndex - 1])
                }
            }
        }

        // Register activity result for picking an image from the gallery
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUris[currentImageIndex - 1] = it
                Glide.with(this).load(it).into(imageViews[currentImageIndex - 1])
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addFeedDialogButton = view.findViewById(R.id.btnAddFeed)
        rvFeed = view.findViewById(R.id.rvFeed)
        feedAdapter = FeedAdapter(feedList, requireContext())
        rvFeed.layoutManager = LinearLayoutManager(requireContext()) // Use LinearLayoutManager for vertical scrolling
        rvFeed.adapter = feedAdapter

        // Fetch feeds from Firestore
        setupFeedListener()

        checkUserRoleAndHandleButtonVisibility()

        addFeedDialogButton.setOnClickListener {
            showAddFeedDialog()
        }

    }

    private fun checkUserRoleAndHandleButtonVisibility() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val consumerRef = firestore.collection("Consumer").document(userId)
        val serviceProviderRef = firestore.collection("ServiceProviders").document(userId)

        consumerRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // User is a consumer, hide the button
                addFeedDialogButton.visibility = View.GONE
            } else {
                // Check if the user is a service provider
                serviceProviderRef.get().addOnSuccessListener { serviceProviderDoc ->
                    if (serviceProviderDoc.exists()) {
                        // User is a service provider, show the button
                        addFeedDialogButton.visibility = View.VISIBLE
                    } else {
                        // Handle case where user doesn't exist in either collection
                        Toast.makeText(requireContext(), "User role not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun showAddFeedDialog(){

        val view = layoutInflater.inflate(R.layout.add_feed, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        imageViews = listOf(
            view.findViewById(R.id.imgFeed1),
            view.findViewById(R.id.imgFeed2),
            view.findViewById(R.id.imgFeed3),
            view.findViewById(R.id.imgFeed4)
        )

        descriptionEditText = view.findViewById(R.id.edtFeedDescription)
        postButton = view.findViewById(R.id.btnPost)

        imageUris = mutableListOf<Uri?>(null, null, null, null) // Initialize the list with null for each image

        // Set click listeners on image views to select or take photos
        for (i in imageViews.indices) {
            imageViews[i].setOnClickListener {
                currentImageIndex = i + 1 // Adjusted to start at 1
                showImageSelectionDialog()
            }
        }

        // Set click listener for the upload button
        postButton.setOnClickListener {
            val description = descriptionEditText.text.toString().trim()

            if (description.isEmpty()) {
                Toast.makeText(requireContext(), "Description is required", Toast.LENGTH_SHORT).show()
            } else {
                // Fetch user details before uploading the feed
                fetchUserDetailsAndUploadFeed(description, dialog)
            }
        }

        dialog.show()
    }

    private fun fetchFeeds() {
        firestore.collection("Feed")
            .orderBy("timestamp") // You may choose DESCENDING for newest first
            .get()
            .addOnSuccessListener { documents ->
                feedList.clear() // Clear previous feeds
                for (document in documents) {
                    val feed = document.toObject(Feed::class.java)
                    feedList.add(feed) // Add each feed to the list
                }
                feedAdapter.notifyDataSetChanged() // Notify adapter of data change
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch feeds", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showImageSelectionDialog() {
        val options = arrayOf("Take a photo", "Choose from gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Select image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkAndRequestCameraPermissions()
                    1 -> checkAndRequestStoragePermissions()
                }
            }.show()
    }

    private fun checkAndRequestCameraPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), cameraPermissionRequestCode)
        } else {
            takePhoto()
        }
    }

    private fun checkAndRequestStoragePermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), storagePermissionRequestCode)
        } else {
            pickImage()
        }
    }

    private fun takePhoto() {
        val fileUri = createImageUri() ?: return
        imageUris[currentImageIndex - 1] = fileUri
        takePictureLauncher.launch(fileUri)
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun createImageUri(): Uri? {
        val imageFile = File(requireContext().cacheDir, "feed_image_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(requireContext(), "${BuildConfig.APPLICATION_ID}.fileprovider", imageFile)
    }

    // Fetch user name and surname before uploading the feed
    private fun fetchUserDetailsAndUploadFeed(description: String, dialog: AlertDialog) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("ServiceProviders").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Unknown"
                    val surname = document.getString("surname") ?: "Unknown"
                    val profileImageUrl = document.getString("profileUrl") ?: ""

                    // Proceed to upload feed with user's name and surname
                    uploadFeed(description, name, surname,profileImageUrl, dialog)
                } else {
                    Toast.makeText(requireContext(), "User details not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch user details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadFeed(description: String, name: String, surname: String, profileImageUrl: String, dialog: AlertDialog) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val uploadTasks = mutableListOf<Uri?>()
        imageUris.forEach { uri ->
            if (uri != null) {
                uploadTasks.add(uri)
            }
        }

        if (uploadTasks.isEmpty()) {
            // If no images are provided, just upload the description
            uploadFeedData(description, name, surname,  profileImageUrl, emptyList(), dialog)
        } else {
            // If images are provided, upload them first and then store their URLs
            uploadImagesToFirebase(userId, description, name, surname,  profileImageUrl, uploadTasks, dialog)
        }
    }

    private fun uploadImagesToFirebase(userId: String, description: String, name: String, surname: String,profileUrl: String, imageUris: List<Uri?>, dialog: AlertDialog) {
        val imageUrls = mutableListOf<String>()
        var imagesUploaded = 0

        for (uri in imageUris) {
            uri?.let {
                val feedImageRef = storageRef.reference.child("feedImages/$userId/${System.currentTimeMillis()}.jpg")
                feedImageRef.putFile(it).addOnSuccessListener {
                    feedImageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        imageUrls.add(downloadUrl.toString())
                        imagesUploaded++

                        // Check if all images are uploaded
                        if (imagesUploaded == imageUris.size) {
                            uploadFeedData(description, name, surname,profileUrl, imageUrls, dialog)
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadFeedData(description: String, name: String, surname: String,profileUrl: String, imageUrls: List<String>, dialog: AlertDialog) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val feedData = hashMapOf(
            "description" to description,
            "imageUrls" to imageUrls,
            "userId" to userId,
            "name" to name,
            "surname" to surname,
            "profileUrl" to profileUrl,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("Feed").add(feedData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Feed uploaded successfully", Toast.LENGTH_SHORT).show()
                // Clear the UI
                descriptionEditText.text.clear()
                for (imageView in imageViews) {
                    imageView.setImageResource(R.drawable.image_placeholder)
                }
                imageUris.clear()
                imageUris.addAll(List(maxImages) { null })

                dialog.dismiss()

                fetchFeeds()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to upload feed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupFeedListener() {
        firestore.collection("Feed")
            .orderBy("timestamp") // Order the feeds
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "Failed to fetch feeds", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                feedList.clear() // Clear previous feeds
                snapshot?.documents?.forEach { document ->
                    val feed = document.toObject(Feed::class.java)
                    if (feed != null) {
                        feedList.add(feed)
                    }
                }
                feedAdapter.notifyDataSetChanged() // Notify adapter of data change
            }
    }
}
