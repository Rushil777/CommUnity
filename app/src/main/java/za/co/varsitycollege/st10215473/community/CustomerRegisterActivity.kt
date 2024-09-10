package za.co.varsitycollege.st10215473.community

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10215473.community.data.Customer

class CustomerRegisterActivity : AppCompatActivity() {

    private lateinit var emailEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var fullName: EditText
    private lateinit var registerButton: Button
    private lateinit var authReg: FirebaseAuth
    private lateinit var firebaseRef: FirebaseFirestore
    private lateinit var confirmPassword: EditText
    private lateinit var openLog: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customer_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fullName = findViewById(R.id.edtFullName)
        emailEdit = findViewById(R.id.edtEmail)
        passwordEdit = findViewById(R.id.edtPassword)
        confirmPassword = findViewById(R.id.edtConfirmPassword)
        registerButton = findViewById(R.id.btnSignUp)
        firebaseRef = FirebaseFirestore.getInstance()
        authReg = FirebaseAuth.getInstance()
        openLog = findViewById(R.id.btnLoginPage)
        
        registerButton.setOnClickListener(){
            val name = fullName.text.toString()
            val email = emailEdit.text.toString()
            val password = passwordEdit.text.toString()
            val confirm = confirmPassword.text.toString()

            if(password.length < 8){
                passwordEdit.setText("")
                passwordEdit.error = "Password must be min 8 characters!"
                confirmPassword.setText("")
            }
            else if(password != confirm){
                passwordEdit.setText("")
                confirmPassword.setText("")
                Toast.makeText(this, "Password does not match!", Toast.LENGTH_SHORT).show()
            }
            else{
                RegisterUser(email, password, name)
            }
        }

        openLog.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        })

    }

    private fun RegisterUser(email: String, password: String, name: String) {
        authReg.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){task ->
                if(task.isSuccessful){
                    val user = authReg.currentUser
                    user?.let{
                        saveUsertoFireStore(it.uid, name, email, "Customer")
                    }
                }else {
                    // Registration failed
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUsertoFireStore(uid: String, name: String, email: String, role: String) {
        val user = Customer(uid, email, name, role, isOnline = false)

        firebaseRef.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save user: $e", Toast.LENGTH_SHORT).show()
            }
    }
}