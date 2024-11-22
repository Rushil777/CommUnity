package za.co.varsitycollege.st10215473.community.ServiceReg

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
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
import za.co.varsitycollege.st10215473.community.CustomerReg.CustomerStep2Fragment
import za.co.varsitycollege.st10215473.community.R
import za.co.varsitycollege.st10215473.community.data.RegistrationViewModel

class ServiceProviderRegisterActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_service_provider_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        registrationViewModel = ViewModelProvider(this)[RegistrationViewModel::class.java]

        askNotificationPermission()

        backButton = findViewById(R.id.btnRegisterBack)
        firebaseRef = FirebaseFirestore.getInstance()
        authReg = FirebaseAuth.getInstance()
        name = findViewById(R.id.edtName)
        surname = findViewById(R.id.edtSurname)
        phoneNumber = findViewById(R.id.edtPhoneNumber)
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

        val fragment = ServiceStep2Fragment() // Replace with your next fragment

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Log.d("Notification", "Permission denied for posting notifications.")
        }
    }


}
