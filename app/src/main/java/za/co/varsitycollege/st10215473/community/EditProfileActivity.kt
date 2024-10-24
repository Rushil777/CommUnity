package za.co.varsitycollege.st10215473.community

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    private lateinit var image1Button: ImageView
    private lateinit var image2Button: ImageView
    private lateinit var image3Button: ImageView
    private lateinit var image4Button: ImageView

    private var uriList: Array<Uri?> = arrayOfNulls(4)  // To store URIs for the 4 images
    private var currentImageIndex = -1  // To track which image is being updated

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private lateinit var firebaseRef: FirebaseFirestore
    private lateinit var storageRef: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        firebaseRef = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance()

        image1Button = findViewById(R.id.image1)
        image2Button = findViewById(R.id.image2)
        image3Button = findViewById(R.id.image3)
        image4Button = findViewById(R.id.image4)

        // Register activity result for taking a picture
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
                if (isSuccess) {
                    uriList[currentImageIndex]?.let { uri ->
                        when (currentImageIndex) {
                            0 -> image1Button.setImageURI(uri)
                            1 -> image2Button.setImageURI(uri)
                            2 -> image3Button.setImageURI(uri)
                            3 -> image4Button.setImageURI(uri)
                        }
                        uploadImageToFirebase(uri, currentImageIndex)
                    }
                }
            }

        // Register activity result for picking an image from gallery
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                uriList[currentImageIndex] = it
                when (currentImageIndex) {
                    0 -> image1Button.setImageURI(it)
                    1 -> image2Button.setImageURI(it)
                    2 -> image3Button.setImageURI(it)
                    3 -> image4Button.setImageURI(it)
                }
                uploadImageToFirebase(it, currentImageIndex)
            }
        }

        // Set click listeners for each image
        setupImageView(image1Button, 0)
        setupImageView(image2Button, 1)
        setupImageView(image3Button, 2)
        setupImageView(image4Button, 3)
    }

    private fun setupImageView(imageView: ImageView, index: Int) {
        imageView.setOnClickListener {
            currentImageIndex = index
            val options = arrayOf("Take Photo", "Choose from Gallery")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select Option")
            builder.setItems(options) { dialogInterface: DialogInterface, which: Int ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> pickImageLauncher.launch("image/*")
                }
                dialogInterface.dismiss()
            }
            builder.show()
        }
    }

    private fun checkCameraPermissionAndOpen() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            uriList[currentImageIndex] = createUri(currentImageIndex)
            takePictureLauncher.launch(uriList[currentImageIndex])
        }
    }
//
    private fun createUri(index: Int): Uri {
        val imageFile = File(this.application.filesDir, "camera_photo_${index + 1}.jpg")
        return FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            imageFile
        )
    }

    private fun uploadImageToFirebase(imageUri: Uri, index: Int) {
        val fileRef = storageRef.reference.child("ServiceProviders/${FirebaseAuth.getInstance().currentUser?.uid}/image${index + 1}.jpg")
        fileRef.putFile(imageUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    saveImageUrlToFirestore(uri.toString(), index + 1)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveImageUrlToFirestore(downloadUrl: String, index: Int) {
        val imageField = "image$index"
        firebaseRef.collection("ServiceProviders")
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
            .update(imageField, downloadUrl)
            .addOnSuccessListener {
                Toast.makeText(this, "Image $index uploaded successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save image URL", Toast.LENGTH_SHORT).show()
            }
    }
}
