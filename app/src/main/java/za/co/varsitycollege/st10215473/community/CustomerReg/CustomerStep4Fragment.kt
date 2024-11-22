package za.co.varsitycollege.st10215473.community.CustomerReg

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import za.co.varsitycollege.st10215473.community.LoginActivity
import za.co.varsitycollege.st10215473.community.R
import za.co.varsitycollege.st10215473.community.data.Customer
import za.co.varsitycollege.st10215473.community.data.RegistrationViewModel
import java.util.Date

class CustomerStep4Fragment : Fragment() {
    private lateinit var registrationViewModel: RegistrationViewModel
    private lateinit var backButton: ImageView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var authReg: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_customer_step4, container, false)
        registrationViewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        firestore = FirebaseFirestore.getInstance()
        authReg = FirebaseAuth.getInstance()

        backButton = view.findViewById(R.id.btnStep4Back)
        val btnRegister = view.findViewById<MaterialButton>(R.id.btnRegister)
        val passwordEdit = view.findViewById<EditText>(R.id.edtPassword)
        val confirmPassword = view.findViewById<EditText>(R.id.edtConfirmPassword)

        backButton.setOnClickListener{
            activity?.onBackPressed()
        }

        btnRegister.setOnClickListener{
            val password = passwordEdit.text.toString()
            val confirm = confirmPassword.text.toString()

            if (password.length < 8) {
                passwordEdit.setText("")
                passwordEdit.error = "Password must be min 8 characters!"
                confirmPassword.setText("")
            } else if (password != confirm) {
                passwordEdit.setText("")
                confirmPassword.setText("")
                Toast.makeText(requireContext(), "Password does not match!", Toast.LENGTH_SHORT).show()
            }else{
                registrationViewModel.password = password
                val email = registrationViewModel.email.toString()
                val name = registrationViewModel.name.toString()
                val surname = registrationViewModel.surname.toString()
                val number = registrationViewModel.phoneNumber.toString()
                val age = registrationViewModel.age
                val dob = registrationViewModel.dateOfBirth
                val idNumber = registrationViewModel.idNumber.toString()

//                val ageToInt: Int = age.toIntOrNull() ?: run {
//                    Toast.makeText(requireContext(), "Invalid age input", Toast.LENGTH_SHORT).show()
//                    return@setOnClickListener
//                }

                if (dob != null) {
                    if (age != null) {
                        RegisterUser(email, password, name, surname, number, age, dob, idNumber)
                    }
                }
            }
        }

        return view
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
        firestore.collection("Consumer")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { emailQuerySnapshot ->
                if (!emailQuerySnapshot.isEmpty) {
                    Toast.makeText(requireContext(), "Email already exists!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }else{
                    firestore.collection("Consumer")
                        .whereEqualTo("idNumber", idNumber)
                        .get()
                        .addOnSuccessListener { idNumberQuerySnapshot ->
                            if (!idNumberQuerySnapshot.isEmpty) {
                                Toast.makeText(requireContext(), "ID Number already exists!", Toast.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }else{
                                // If both email and ID number are unique, proceed with registration
                                authReg.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(requireActivity()) { task ->
                                        if (task.isSuccessful) {
                                            val user = authReg.currentUser
                                            user?.let {
                                                FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                                                    if (tokenTask.isSuccessful) {
                                                        val token = tokenTask.result
                                                        if (token != null) {
                                                            saveUsertoFireStore(it.uid, name, email, surname, number, age, dob, idNumber, token)
                                                        } else {
                                                            Toast.makeText(requireContext(), "FCM token is null", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } else {
                                                        Toast.makeText(requireContext(), "Failed to get FCM token", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        } else {
                                            Toast.makeText(requireContext(), "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to check ID number: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to check email: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


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

        firestore.collection("Consumer").document(uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "User registered successfully", Toast.LENGTH_SHORT).show()

                val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", AppCompatActivity.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("userRole", "consumer")
                editor.apply()

                startActivity(Intent(requireContext(), LoginActivity::class.java))
                activity?.finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save user: $e", Toast.LENGTH_SHORT).show()
            }
    }

}