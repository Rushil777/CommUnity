package za.co.varsitycollege.st10215473.community.CustomerReg

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import za.co.varsitycollege.st10215473.community.LoginActivity
import za.co.varsitycollege.st10215473.community.R
import za.co.varsitycollege.st10215473.community.data.Customer
import za.co.varsitycollege.st10215473.community.data.RegistrationViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CustomerRegisterActivity : AppCompatActivity() {

    private lateinit var name: EditText
    private lateinit var surname: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var authReg: FirebaseAuth
    private lateinit var firebaseRef: FirebaseFirestore
    private lateinit var openLog: MaterialButton
    private lateinit var registrationViewModel: RegistrationViewModel
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customer_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        registrationViewModel = ViewModelProvider(this)[RegistrationViewModel::class.java]


        askNotificationPermission()

        backButton = findViewById(R.id.btnRegisterBack)
        name = findViewById(R.id.edtName)
        surname= findViewById(R.id.edtSurname)
        phoneNumber = findViewById(R.id.edtPhoneNumber)
        firebaseRef = FirebaseFirestore.getInstance()
        authReg = FirebaseAuth.getInstance()
        openLog = findViewById(R.id.btnLoginPage)

        backButton.setOnClickListener{
            onBackPressed()
        }

        openLog.setOnClickListener(View.OnClickListener {
            if (name.text.isBlank() || surname.text.isBlank() || phoneNumber.text.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }else{
                registrationViewModel.name = name.text.toString()
                registrationViewModel.surname = surname.text.toString()
                registrationViewModel.phoneNumber = phoneNumber.text.toString()

                navigateToNextStep()
            }
        })

    }

    private fun navigateToNextStep() {
        findViewById<MaterialButton>(R.id.btnLoginPage).visibility = View.GONE

        val fragment = CustomerStep2Fragment() // Replace with your next fragment

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment) // Ensure fragmentContainer is the ID of your container
            .addToBackStack(null) // Add to backstack for back navigation
            .commit()
    }

    override fun onBackPressed() {

        findViewById<MaterialButton>(R.id.btnLoginPage).visibility = View.VISIBLE
        super.onBackPressed()
    }

    private fun askNotificationPermission() {
        // Check if the device is running Android 13 (API level 33) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if the permission is already granted
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is granted, you can post notifications
                Log.d("Notification", "Permission already granted.")
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Show UI to explain why the app needs the permission (Optional)
                // For now, directly ask for the permission
                Log.d("Notification", "Showing permission rationale.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Directly request the permission
                Log.d("Notification", "Requesting permission directly.")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
            Log.d("Notification", "Permission granted: You can now post notifications.")
        } else {

            Log.d("Notification", "Permission denied: Your app will not show notifications.")
        }
    }



}