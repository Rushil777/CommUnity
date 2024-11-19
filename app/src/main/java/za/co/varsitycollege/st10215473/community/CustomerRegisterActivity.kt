package za.co.varsitycollege.st10215473.community

import android.Manifest
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import za.co.varsitycollege.st10215473.community.data.Customer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CustomerRegisterActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_customer_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        askNotificationPermission()
        // Inside onCreate or RegisterUser function
        name = findViewById(R.id.edtName)
        surname= findViewById(R.id.edtSurname)
        phoneNumber = findViewById(R.id.edtPhoneNumber)
        IdNumber = findViewById(R.id.edtIdNumber)
        age = findViewById(R.id.edtAge)
        dob = findViewById(R.id.edtDate)
        emailEdit = findViewById(R.id.edtEmail)
        passwordEdit = findViewById(R.id.edtPassword)
        confirmPassword = findViewById(R.id.edtConfirmPassword)
        registerButton = findViewById(R.id.btnSignUp)
        firebaseRef = FirebaseFirestore.getInstance()
        authReg = FirebaseAuth.getInstance()
        openLog = findViewById(R.id.btnLoginPage)



        dob.setOnClickListener {
            // Create a Calendar object to get the current date
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Create DatePickerDialog
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Update the EditText with the selected date
                    dob.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
                },
                year, month, day
            )

            datePickerDialog.datePicker.maxDate = System.currentTimeMillis() // Set max date to today
            datePickerDialog.show()
        }


        registerButton.setOnClickListener {
            val name = name.text.toString()
            val surname = surname.text.toString()
            val number = phoneNumber.text.toString()
            val id = IdNumber.text.toString()
            val ageString = age.text.toString() // Get the age as a string
            val dobString = dob.text.toString() // This will be used for date of birth
            val email = emailEdit.text.toString()
            val password = passwordEdit.text.toString()
            val confirm = confirmPassword.text.toString()

            if (password.length < 8) {
                passwordEdit.setText("")
                passwordEdit.error = "Password must be min 8 characters!"
                confirmPassword.setText("")
            } else if (password != confirm) {
                passwordEdit.setText("")
                confirmPassword.setText("")
                Toast.makeText(this, "Password does not match!", Toast.LENGTH_SHORT).show()
            } else {
                // Convert age to Int, with error handling
                val age: Int = ageString.toIntOrNull() ?: run {
                    Toast.makeText(this, "Invalid age input", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val dob: Date? = parseDateOfBirth(dobString)

                if (dob == null) {
                    Toast.makeText(this, "Invalid date of birth format", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                RegisterUser(email, password, name, surname, number, age, dob, id)
            }
        }

        openLog.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        })

    }
    // Update the saveUsertoFireStore function to include the token parameter
    private fun saveUsertoFireStore(
        uid: String,
        name: String,
        email: String,
        surname: String,
        number: String,
        age: Int,
        dob: Date,
        idNumber: String,
        fcmToken: String
    ) {
        val currentDate = Date()
        // Ensure fields are correctly ordered and explicitly named
        val user = Customer(
            id = uid,
            idNumber = idNumber,
            name = name,
            surname = surname,
            phoneNumber = number,
            email = email,
            age = age,
            dateOfBirth = dob,
            status = "PENDING",
            dateSubmitted = currentDate,
            isOnline = false,
            lastMessageSent = "",
            lastMessageTimeSent = null,
            fcmToken = fcmToken
        )

        firebaseRef.collection("Consumer").document(uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()

                val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("userRole", "consumer")
                editor.apply()

                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save user: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun parseDateOfBirth(dobString: String): Date? {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.parse(dobString)
        } catch (e: Exception) {
            null // Return null if parsing fails
        }
    }

    private fun RegisterUser(
        email: String,
        password: String,
        name: String,
        surname: String,
        number: String,
        age: Int,
        dob: Date,
        idNumber: String
    ) {
        authReg.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = authReg.currentUser
                    user?.let {
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                val token = tokenTask.result
                                if (token != null) {

                                    saveUsertoFireStore(it.uid, name, email, surname, number, age, dob, idNumber, token)
                                } else {
                                    Toast.makeText(this, "FCM token is null", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this, "Failed to get FCM token", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
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