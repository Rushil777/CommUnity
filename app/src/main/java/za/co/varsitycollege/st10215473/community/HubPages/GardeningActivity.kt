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

class GardeningActivity : AppCompatActivity() {
    private lateinit var mowing: ImageButton
    private lateinit var garden: ImageButton
    private lateinit var tree: ImageButton
    private lateinit var backButton: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gardening)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mowing = findViewById(R.id.btnLawnMoving)
        garden = findViewById(R.id.btnGardenMaintenance)
        tree = findViewById(R.id.btnTreeTrimming)
        backButton = findViewById(R.id.btnCleaningBackButton)

        backButton.setOnClickListener{
            onBackPressed()
        }

        mowing.setOnClickListener {
            val intent = Intent(this, ServiceProviderSelection::class.java)
            intent.putExtra("selectedSubcategory", "Lawn Mowing")
            startActivity(intent)
        }

        garden.setOnClickListener{
            val intent = Intent(this, ServiceProviderSelection::class.java)
            intent.putExtra("selectedSubcategory", "Garden Maintenance")
            startActivity(intent)
        }

        tree.setOnClickListener{
            val intent = Intent(this, ServiceProviderSelection::class.java)
            intent.putExtra("selectedSubcategory", "Tree Trimming")
            startActivity(intent)
        }


    }
}