package za.co.skyner.estateguard

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import za.co.skyner.estateguard.auth.AuthManager
import za.co.skyner.estateguard.auth.FirebaseAuthManager
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

        // Check authentication state
        if (!authManager.isLoggedIn()) {
            // Redirect to login activity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
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
        // Setup role-based top level destinations
        val currentUser = authManager.getCurrentUser()
        val topLevelDestinations = when (currentUser?.role) {
            za.co.skyner.estateguard.data.model.UserRole.ADMIN -> setOf(R.id.nav_admin)
            za.co.skyner.estateguard.data.model.UserRole.SECURITY_GUARD -> setOf(R.id.nav_home, R.id.nav_clock_in_out, R.id.nav_incident_log)
            else -> setOf(R.id.nav_home, R.id.nav_clock_in_out, R.id.nav_incident_log)
        }

        appBarConfiguration = AppBarConfiguration(topLevelDestinations, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Setup navigation header with user information
        setupNavigationHeader()

        // Setup role-based menu visibility
        setupRoleBasedMenu(navView)

        // Handle logout from navigation menu
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    performLogout()
                    true
                }
                else -> {
                    // Let the default navigation handle other items
                    NavigationUI.onNavDestinationSelected(menuItem, navController) || super.onOptionsItemSelected(menuItem)
                }
            }
        }
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
            subtitleView.text = when (currentUser.role) {
                za.co.skyner.estateguard.data.model.UserRole.ADMIN -> "Administrator"
                za.co.skyner.estateguard.data.model.UserRole.SECURITY_GUARD -> "Security Guard"
            }
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

    private fun setupRoleBasedMenu(navView: NavigationView) {
        val currentUser = authManager.getCurrentUser()
        val menu = navView.menu

        when (currentUser?.role) {
            za.co.skyner.estateguard.data.model.UserRole.ADMIN -> {
                // Admin only sees: Admin panel (which is their dashboard), Logout
                menu.findItem(R.id.nav_home)?.isVisible = false
                menu.findItem(R.id.nav_clock_in_out)?.isVisible = false
                menu.findItem(R.id.nav_incident_log)?.isVisible = false
                menu.findItem(R.id.nav_admin)?.isVisible = true

                // Navigate admin directly to admin panel
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigate(R.id.nav_admin)
            }
            za.co.skyner.estateguard.data.model.UserRole.SECURITY_GUARD -> {
                // Guards see: Home, Clock In/Out, Incident Log, Logout
                menu.findItem(R.id.nav_home)?.isVisible = true
                menu.findItem(R.id.nav_clock_in_out)?.isVisible = true
                menu.findItem(R.id.nav_incident_log)?.isVisible = true
                menu.findItem(R.id.nav_admin)?.isVisible = false
            }
            else -> {
                // Default: hide admin functions
                menu.findItem(R.id.nav_admin)?.isVisible = false
            }
        }
    }

    private fun performLogout() {
        try {
            // Sign out from Firebase (this also clears local auth state)
            val firebaseAuthManager = FirebaseAuthManager(this)
            firebaseAuthManager.signOut()

            // Navigate to login activity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            // Finish the current activity
            finish()

        } catch (e: Exception) {
            // Handle error silently or show a toast
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                performLogout()
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