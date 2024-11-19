package za.co.varsitycollege.st10215473.community

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import za.co.varsitycollege.st10215473.community.data.ServiceProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ServiceProviderRegisterActivity : AppCompatActivity() {

    private lateinit var emailEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var name: EditText
    private lateinit var surname: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var IdNumber: EditText
    private lateinit var age: EditText
    private lateinit var dob: EditText
    private lateinit var registerButton: MaterialButton
    private lateinit var authReg: FirebaseAuth
    private lateinit var firebaseRef: FirebaseFirestore
    private lateinit var confirmPassword: EditText
    private lateinit var openLog: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_service_provider_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        askNotificationPermission()

        firebaseRef = FirebaseFirestore.getInstance()
        authReg = FirebaseAuth.getInstance()

        // Initialize views
        name = findViewById(R.id.edtName)
        surname = findViewById(R.id.edtSurname)
        phoneNumber = findViewById(R.id.edtPhoneNumber)
        IdNumber = findViewById(R.id.edtIdNumber)
        age = findViewById(R.id.edtAge)
        dob = findViewById(R.id.edtDate)
        emailEdit = findViewById(R.id.edtEmail)
        passwordEdit = findViewById(R.id.edtPassword)
        confirmPassword = findViewById(R.id.edtConfirmPassword)
        registerButton = findViewById(R.id.btnSignUp)
        openLog = findViewById(R.id.btnLoginPage)

        dob.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    dob.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        registerButton.setOnClickListener {
            val name = name.text.toString()
            val surname = surname.text.toString()
            val number = phoneNumber.text.toString()
            val id = IdNumber.text.toString()
            val age = age.text.toString().toIntOrNull() ?: return@setOnClickListener
            val dob = parseDateOfBirth(dob.text.toString()) ?: return@setOnClickListener
            val email = emailEdit.text.toString()
            val password = passwordEdit.text.toString()
            val confirm = confirmPassword.text.toString()

            if (password != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(email, password, name, surname, number, age, dob, id)
        }

        openLog.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun registerUser(
        email: String, password: String, name: String, surname: String,
        number: String, age: Int, dob: Date, idNumber: String
    ) {
        authReg.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = authReg.currentUser
                    user?.let {
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                            val token = tokenTask.result
                            if (token != null) {
                                saveUserToFireStore(
                                    it.uid, name, email, surname, number, age, dob, idNumber, token
                                )
                            } else {
                                Toast.makeText(this, "FCM token is null", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToFireStore(
        uid: String,
        name: String, email: String, surname: String,
        number: String, age: Int, dob: Date, idNumber: String, fcmToken: String
    ) {
        val currentDate = Date()
        val user = ServiceProvider(
            id = uid,
            name = name,
            surname = surname,
            phoneNumber = number,
            email = email,
            age = age,
            dateOfBirth = dob,
            status = "PENDING",
            idNumber = idNumber,
            fcmToken = fcmToken,
            dateSubmitted = currentDate
        )

        firebaseRef.collection("ServiceProviders").document(uid).set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Service provider registered successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save service provider: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun parseDateOfBirth(dobString: String): Date? {
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dobString)
        } catch (e: Exception) {
            null
        }
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
