package za.co.varsitycollege.st10215473.community

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10215473.community.data.Customer
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
    private lateinit var registerButton: Button
    private lateinit var authReg: FirebaseAuth
    private lateinit var firebaseRef: FirebaseFirestore
    private lateinit var confirmPassword: EditText
    private lateinit var openLog: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_service_provider_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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

    private fun parseDateOfBirth(dobString: String): Date? {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.parse(dobString)
        } catch (e: Exception) {
            null // Return null if parsing fails
        }
    }

    private fun RegisterUser(email: String, password: String, name: String, surname: String, number: String, age: Int, dob: Date, idNumber: String) {
        authReg.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){task ->
                if(task.isSuccessful){
                    val user = authReg.currentUser
                    user?.let{
                        saveUsertoFireStore(it.uid, name, email, surname, number, age, dob, idNumber)
                    }
                }else {
                    // Registration failed
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUsertoFireStore(uid: String, name: String, email: String, surname: String, number: String, age: Int, dob: Date, idNumber: String) {
        val currentDate = Date()
        val user = ServiceProvider(uid, idNumber, name, surname, number,email, age, dob, "PENDING", currentDate, false, "", null, "", "", "", "", "")

        firebaseRef.collection("ServiceProviders").document(uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save user: $e", Toast.LENGTH_SHORT).show()
            }
    }
}