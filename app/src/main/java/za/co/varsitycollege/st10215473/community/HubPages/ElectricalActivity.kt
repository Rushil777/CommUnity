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

class ElectricalActivity : AppCompatActivity() {
    private lateinit var electricalRepairs: ImageButton
    private lateinit var lighting: ImageButton
    private lateinit var appliance: ImageButton
    private lateinit var solar: ImageButton
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_electrical)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        electricalRepairs = findViewById(R.id.btnElectricalRepairs)
        lighting = findViewById(R.id.btnLightingInstallation)
        appliance = findViewById(R.id.btnApplianceInstallation)
        solar = findViewById(R.id.btnSolarPanel)
        backButton = findViewById(R.id.btnCleaningBackButton)

        backButton.setOnClickListener{
            onBackPressed()
        }

        electricalRepairs.setOnClickListener {
            val intent = Intent(this, ServiceProviderSelection::class.java)
            intent.putExtra("selectedSubcategory", "Electrical Repairs")
            startActivity(intent)
        }

        lighting.setOnClickListener{
            val intent = Intent(this, ServiceProviderSelection::class.java)
            intent.putExtra("selectedSubcategory", "Lighting Installation")
            startActivity(intent)
        }

        appliance.setOnClickListener{
            val intent = Intent(this, ServiceProviderSelection::class.java)
            intent.putExtra("selectedSubcategory", "Appliance Installation")
            startActivity(intent)
        }

        solar.setOnClickListener{
            val intent = Intent(this, ServiceProviderSelection::class.java)
            intent.putExtra("selectedSubcategory", "Solar-Panel Installation")
            startActivity(intent)
        }

    }
}