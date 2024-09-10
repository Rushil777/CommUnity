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
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var signUpButton: Button
    private lateinit var signInButton: Button
    private lateinit var emailEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
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

    private fun showSignUpDialog() {
        // Create an AlertDialog with options
        val options = arrayOf("Sign up as Service Provider", "Sign up as Customer")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Sign-Up Option")

        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    startActivity(Intent(this, ServiceProviderRegisterActivity::class.java))
                }
                1 -> {
                    startActivity((Intent(this, CustomerRegisterActivity::class.java)))
                }
            }
        }

        builder.show()
    }
}