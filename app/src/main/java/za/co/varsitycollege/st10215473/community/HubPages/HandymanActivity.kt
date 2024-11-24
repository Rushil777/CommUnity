package za.co.varsitycollege.st10215473.community.HubPages

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import za.co.varsitycollege.st10215473.community.R
import za.co.varsitycollege.st10215473.community.ServiceProviderSelection

class HandymanActivity : AppCompatActivity() {
    private lateinit var repairs: ImageButton
    private lateinit var furniture: ImageButton
    private lateinit var painting: ImageButton
    private lateinit var plumbing: ImageButton
    private lateinit var backButton: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_handyman)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repairs = findViewById(R.id.btnGeneralRepairs)
        furniture = findViewById(R.id.btnFurnitureAssembly)
        painting = findViewById(R.id.btnPainting)
        plumbing = findViewById(R.id.btnPlumbing)
        backButton = findViewById(R.id.btnCleaningBackButton)

        backButton.setOnClickListener{
            onBackPressed()
        }

        repairs.setOnClickListener {
            val intent = Intent(this, ServiceProviderSelection::class.java)
            intent.putExtra("selectedSubcategory", "General Repairs")
            startActivity(intent)
        }

        furniture.setOnClickListener{
            val intent = Intent(this, ServiceProviderSelection::class.java)
            intent.putExtra("selectedSubcategory", "Furniture Assembly")
            startActivity(intent)
        }

        painting.setOnClickListener{
            val intent = Intent(this, ServiceProviderSelection::class.java)
            intent.putExtra("selectedSubcategory", "Painting")
            startActivity(intent)
        }

        plumbing.setOnClickListener{
            val intent = Intent(this, ServiceProviderSelection::class.java)
            intent.putExtra("selectedSubcategory", "Plumbing")
            startActivity(intent)
        }

    }
}