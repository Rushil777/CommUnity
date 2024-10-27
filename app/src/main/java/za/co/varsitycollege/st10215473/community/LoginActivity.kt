package za.co.varsitycollege.st10215473.community

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var signUpButton: MaterialButton
    private lateinit var signInButton: MaterialButton
    private lateinit var emailEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        signInButton = findViewById(R.id.btnSignInLogin)
        signUpButton = findViewById(R.id.btnSignInRegister)
        emailEdit = findViewById(R.id.edtSignInEmail)
        passwordEdit = findViewById(R.id.edtSignInPassword)

        signUpButton.setOnClickListener {
            showSignUpDialog()
        }

        signInButton.setOnClickListener(View.OnClickListener {
            val password = passwordEdit.text.toString()
            val email = emailEdit.text.toString()

            if(password.isEmpty()) {
                passwordEdit.error = "Type a password"
                return@OnClickListener  // Return to prevent further execution
            }

            if(email.isEmpty()) {
                emailEdit.error = "Type an email"
                return@OnClickListener  // Return to prevent further execution
            }
            if(password.isNotEmpty() && email.isNotEmpty()){
                LoginUser(email, password)
            }
        })


    }

    private fun LoginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(baseContext, "Login Successful", Toast.LENGTH_LONG).show()
                    val user = auth.currentUser
                    user?.let {
                        // Fetch user role from Firestore
                        fetchUserRoleAndProceed(it.uid)
                    }
                }
                else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    private fun fetchUserRoleAndProceed(uid: String) {
        firestore.collection("ServiceProviders").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    saveUserRoleToPreferences("serviceProvider")
                    navigateToMainActivity()
                } else {
                    firestore.collection("Customers").document(uid).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                saveUserRoleToPreferences("consumer")
                                navigateToMainActivity()
                            } else {
                                // Handle case where user role is not found
                                Toast.makeText(this, "User role not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching user role: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserRoleToPreferences(role: String) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("userRole", role)
        editor.apply()
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showSignUpDialog() {
        // Inflate the custom layout
        val dialogView = layoutInflater.inflate(R.layout.signup_cardview, null)

        // Create the AlertDialog and set the custom layout
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        // Find the buttons in the custom layout
        val serviceProviderButton = dialogView.findViewById<Button>(R.id.btnServiceProviderSignUp)
        val customerButton = dialogView.findViewById<Button>(R.id.btnCustomerSignUp)

        // Set click listeners for the buttons
        serviceProviderButton.setOnClickListener {
            startActivity(Intent(this, ServiceProviderRegisterActivity::class.java))
        }

        customerButton.setOnClickListener {
            startActivity(Intent(this, CustomerRegisterActivity::class.java))
        }

        // Show the dialog
        val dialog = builder.create()
        dialog.show()
    }
}