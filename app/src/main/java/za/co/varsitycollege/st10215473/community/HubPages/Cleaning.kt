package za.co.varsitycollege.st10215473.community.HubPages

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import za.co.varsitycollege.st10215473.community.HubFragment
import za.co.varsitycollege.st10215473.community.MainActivity
import za.co.varsitycollege.st10215473.community.R
import za.co.varsitycollege.st10215473.community.ServiceProviderSelection

class Cleaning : AppCompatActivity() {

    private lateinit var windowCleaningButton: ImageButton
    private lateinit var carpetCleaning: ImageButton
    private lateinit var upholsteryCleaning: ImageButton
    private lateinit var laundry: ImageButton
    private lateinit var backButton: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cleaning)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backButton = findViewById(R.id.btnCleaningBackButton)
        windowCleaningButton = findViewById(R.id.btnWindowCleaning)
        carpetCleaning = findViewById(R.id.btnCarpetCleaning)
        upholsteryCleaning = findViewById(R.id.btnUpholstery)
        laundry = findViewById(R.id.btnLaundry)

        backButton.setOnClickListener{
           onBackPressed()
        }

        windowCleaningButton.setOnClickListener {
            val intent = Intent(this, ServiceProviderSelection::class.java)
            intent.putExtra("selectedSubcategory", "Window Cleaning")
            startActivity(intent)
        }

        carpetCleaning.setOnClickListener{
            val intent = Intent(this, ServiceProviderSelection::class.java)
            intent.putExtra("selectedSubcategory", "Carpet Cleaning")
            startActivity(intent)
        }

        upholsteryCleaning.setOnClickListener{
            val intent = Intent(this, ServiceProviderSelection::class.java)
            intent.putExtra("selectedSubcategory", "Upholstery")
            startActivity(intent)
        }

        laundry.setOnClickListener{
            val intent = Intent(this, ServiceProviderSelection::class.java)
            intent.putExtra("selectedSubcategory", "Laundry")
            startActivity(intent)
        }
    }


}