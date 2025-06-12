package za.co.skyner.estateguard

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import za.co.skyner.estateguard.auth.AuthManager
import za.co.skyner.estateguard.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        // Initialize AuthManager
        authManager = AuthManager(this)

        // For testing: Simulate a user login (remove this in production)
        if (!authManager.isLoggedIn()) {
            authManager.simulateLogin("guard") // or "admin" for admin user
        }

        binding.appBarMain.fab.setOnClickListener { view ->
            // Navigate to incident log for quick incident reporting
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            navController.navigate(R.id.nav_incident_log)
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_clock_in_out, R.id.nav_incident_log, R.id.nav_admin, R.id.nav_profile
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Setup navigation header with user information
        setupNavigationHeader()
    }

    private fun setupNavigationHeader() {
        val headerView = binding.navView.getHeaderView(0)
        val navHeaderTitle = headerView.findViewById<TextView>(R.id.nav_header_title)
        val navHeaderSubtitle = headerView.findViewById<TextView>(R.id.nav_header_subtitle)

        updateNavigationHeader(navHeaderTitle, navHeaderSubtitle)
    }

    private fun updateNavigationHeader(titleView: TextView, subtitleView: TextView) {
        val currentUser = authManager.getCurrentUser()

        if (currentUser != null) {
            // Display user information
            titleView.text = currentUser.name
            subtitleView.text = "ID: ${currentUser.id}"
        } else {
            // Display default information when no user is logged in
            titleView.text = getString(R.string.nav_header_title)
            subtitleView.text = getString(R.string.nav_header_subtitle)
        }
    }

    // Method to refresh navigation header when user logs in/out
    fun refreshNavigationHeader() {
        val headerView = binding.navView.getHeaderView(0)
        val navHeaderTitle = headerView.findViewById<TextView>(R.id.nav_header_title)
        val navHeaderSubtitle = headerView.findViewById<TextView>(R.id.nav_header_subtitle)

        updateNavigationHeader(navHeaderTitle, navHeaderSubtitle)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_switch_user -> {
                // Switch between admin and guard for testing
                val currentUser = authManager.getCurrentUser()
                if (currentUser?.role == za.co.skyner.estateguard.data.model.UserRole.ADMIN) {
                    authManager.simulateLogin("guard")
                } else {
                    authManager.simulateLogin("admin")
                }
                refreshNavigationHeader()
                true
            }
            R.id.action_logout -> {
                authManager.logout()
                refreshNavigationHeader()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}