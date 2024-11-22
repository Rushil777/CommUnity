package za.co.varsitycollege.st10215473.community.ServiceReg

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10215473.community.CustomerReg.CustomerStep3Fragment
import za.co.varsitycollege.st10215473.community.R
import za.co.varsitycollege.st10215473.community.data.RegistrationViewModel

class ServiceStep2Fragment : Fragment() {
    private lateinit var registrationViewModel: RegistrationViewModel
    private lateinit var backButton: ImageView
    private lateinit var firestore: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_service_step2, container, false)
        registrationViewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]

        firestore = FirebaseFirestore.getInstance()
        backButton = view.findViewById(R.id.btnStep2Back)
        val btnNext2 = view.findViewById<MaterialButton>(R.id.cusNext2)
        val idNumber = view.findViewById<EditText>(R.id.edtIdNumber)
        val email = view.findViewById<EditText>(R.id.edtEmail)

        backButton.setOnClickListener{
            activity?.onBackPressed()
        }

        btnNext2.setOnClickListener {
            val idNumberText = idNumber.text.toString().trim()
            val emailText = email.text.toString().trim()

            if (idNumberText.isBlank() || emailText.isBlank()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firestore.collection("ServiceProviders")
                .whereEqualTo("email", emailText)
                .get()
                .addOnSuccessListener { emailQuerySnapshot ->
                    if (!emailQuerySnapshot.isEmpty) {
                        // Email already exists
                        Toast.makeText(context, "Email already exists!", Toast.LENGTH_SHORT).show()
                    } else {
                        // Check if the ID number already exists
                        firestore.collection("ServiceProviders")
                            .whereEqualTo("idNumber", idNumberText)
                            .get()
                            .addOnSuccessListener { idNumberQuerySnapshot ->
                                if (!idNumberQuerySnapshot.isEmpty) {
                                    // ID number already exists
                                    Toast.makeText(context, "ID Number already exists!", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Save data to ViewModel
                                    registrationViewModel.idNumber = idNumberText
                                    registrationViewModel.email = emailText

                                    // Navigate to the next step
                                    navigateToNextStep()
                                }
                            }
                            .addOnFailureListener { e ->
                                // Handle Firestore error during ID number check
                                Toast.makeText(context, "Error checking ID: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    // Handle Firestore error during email check
                    Toast.makeText(context, "Error checking email: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }

    private fun navigateToNextStep() {
        val fragment = ServiceStep3Fragment() // Replace with your next fragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment) // Ensure fragmentContainer is the ID of your container
            .addToBackStack(null) // Add to backstack for back navigation
            .commit()
    }

}