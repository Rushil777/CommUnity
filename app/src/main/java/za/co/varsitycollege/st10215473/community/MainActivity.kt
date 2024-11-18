package za.co.varsitycollege.st10215473.community

import ChatFragment
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import za.co.varsitycollege.st10215473.community.FavouriteFragment
import za.co.varsitycollege.st10215473.community.HubFragment
import za.co.varsitycollege.st10215473.community.ProfileFragment
import za.co.varsitycollege.st10215473.community.R

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavBar: ChipNavigationBar
    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        rootView = findViewById(R.id.main) // Root view for layout listener
        bottomNavBar = findViewById(R.id.bottomNav)

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userRole = sharedPreferences.getString("userRole", null)

        setupWindowInsets()
        if (userRole != null) {
            setupBottomNavigation(userRole)
        }
        replaceFragment(ChatFragment())

        observeKeyboardVisibility()

    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupBottomNavigation(userRole: String) {
        if (userRole == "serviceProvider") {
            bottomNavBar.setMenuResource(R.menu.service_bottom_menu)  // Use a different menu for service providers
        } else {
            bottomNavBar.setMenuResource(R.menu.bottom_menu)  // Use default menu for consumers
        }

        bottomNavBar.setOnItemSelectedListener { menuItem ->
            when (menuItem) {
                R.id.hub -> {
                    replaceFragment(HubFragment())
                }
                R.id.chat -> {
                    replaceFragment(ChatFragment())
                }
                R.id.favourites -> replaceFragment(FavouriteFragment())
                R.id.feed -> replaceFragment(FeedFragment())
                R.id.profile -> replaceFragment(ProfileFragment())
                else -> false
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }

    private fun observeKeyboardVisibility() {
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            rootView.getWindowVisibleDisplayFrame(r)
            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - r.bottom

            bottomNavBar.visibility = if (keypadHeight > screenHeight * 0.15) View.GONE else View.VISIBLE
        }
    }

}